# The Cloud Functions for Firebase SDK to create Cloud Functions and set up triggers.
from firebase_functions import firestore_fn, https_fn
# The Firebase Admin SDK to access Cloud Firestore.
from firebase_admin import initialize_app, firestore
from typing import Any
import os
import google.cloud.firestore
from urllib.parse import quote

# libraries
import re
from bs4 import BeautifulSoup
import requests
import json

initialize_app()

DEFAULT_AMENITIES_NUM = 0
NEARBY_URL = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?'
DISTANCE_URL = 'https://maps.googleapis.com/maps/api/distancematrix/json?'

@https_fn.on_request()
def hello_world(req: https_fn.Request) -> https_fn.Response:
    """Take the text parameter passed to this HTTP endpoint and insert it into
    a new document in the messages collection."""
    return https_fn.Response(f"hello world")

#Obtain webpage HTML content based on URL
#Input: url (str)
#Output: BeautifulSoup instance contains the html
def getHtml(url):
    headers = {
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36'
    }
    res = requests.get(url, headers = headers)

    if res.status_code != 200:
        return None

    res.encoding='UTF-8'
    soup=BeautifulSoup(res.text,'lxml')
    return soup

#Scrape the property information on the domain website based on the URL.
#Input: URL(str)
#Output: URL (str), price (int), bed_num (int), parking_num (int), address (str),imgs_url (list of str)
def scrape_domain(url):
    try:
        html_soup = getHtml(url)

        #if failed to scrape, return the default value
        if html_soup is None:
            raise Exception()

        address = html_soup.find('h1',{'class': 'css-164r41r'}).text
        price_str = html_soup.find('div',{'data-testid': 'listing-details__summary-title'})
        if price_str is None:
            price_str = html_soup.find('span',{'data-testid': 'listing-details__listing-summary-title-name'})
        # code reference: https://stackoverflow.com/questions/11339210/how-to-get-integer-values-from-a-string-in-python
        price = re.search(r'(\d+,?)+', price_str.text).group()
        price = DEFAULT_AMENITIES_NUM if price == None else int(price.replace(',', ''))

        beds_baths_parkings_str = html_soup.find('div',{'class':'css-1dtnjt5'})
        if beds_baths_parkings_str is None:
            beds_baths_parkings_str = html_soup.find('div',{'class':'css-18biwo'})
        patterns = {
            'bed': re.compile(r'(\d|−) Bed'),
            'bath': re.compile(r'(\d|−) Bath'),
            'parking': re.compile(r'(\d|−) Parking')
        }
        for feature, pattern in patterns.items():
            match = pattern.search(beds_baths_parkings_str.text)
            if match:
                value = match.group(1)
                if feature == 'bed':
                    bed_num = DEFAULT_AMENITIES_NUM if value == '−' else int(value)
                elif feature == 'bath':
                    bath_num = DEFAULT_AMENITIES_NUM if value == '−' else int(value)
                elif feature == 'parking':
                    parking_num = DEFAULT_AMENITIES_NUM if value == '−' else int(value)

        imgs_url = html_soup.find('img',{'class': 'css-bh4wo8'}).get('src')
    except:
        raise Exception("Failed to scrape url. Please insert data manually.")

    return url, price, bed_num, bath_num, parking_num, address, [imgs_url]


#Scrape the property information on the raywhite website based on the URL.
#Input: URL(str)
#Output: URL (str), price (int), bed_num (int), parking_num (int), address (str),imgs_url (list of str)
def scrape_raywhite(url):
    try:
        html_soup = getHtml(url)

        #if failed to scrape, return the default value
        if html_soup is None:
            raise Exception()

        address = html_soup.find('h1',{'class': 'banner-basic__title'}).text.replace("\n", ", ").strip(", ").strip()

        price_str = html_soup.find('div',{'class': 'property-detail__banner__side__price'}).text.strip()
        price = re.search(r'(\d+,?)+', price_str).group()
        price = DEFAULT_AMENITIES_NUM if price == None else int(price.replace(',', ''))

        beds_baths_parkings_str = html_soup.find('div',{'class':'property-meta'}).text
        beds_baths_parkings_num = re.findall(r'\d+', beds_baths_parkings_str)
        # define default value
        beds_baths_parkings_count= {'bed_num': DEFAULT_AMENITIES_NUM, 'bath_num': DEFAULT_AMENITIES_NUM, 'car_num': DEFAULT_AMENITIES_NUM}
        classes_to_find = [
            ('icon icon-solid-bed', 'bed_num'),
            ('icon icon-solid-bath', 'bath_num'),
            ('icon icon-solid-car', 'car_num'),
        ]
        for class_to_find, key in classes_to_find:
            if html_soup.find('span', class_=class_to_find):
                beds_baths_parkings_count[key] = int(beds_baths_parkings_num.pop(0))

        imgs = html_soup.find_all('img', src=re.compile(r'width=1024.*height=720'))
        imgs_url = [img['src'] for img in imgs if 'src' in img.attrs]
    except:
        raise Exception("Failed to scrape url. Please insert data manually.")


    return url, price, beds_baths_parkings_count['bed_num'], beds_baths_parkings_count['bath_num'], beds_baths_parkings_count['car_num'],address,imgs_url


def create_error_response(error_code:int, message: str) -> https_fn.Response:
    """Create an error response with the given error code and message."""
    return https_fn.Response(
        json.dumps({"error": {"code": error_code, "message": message}}),
        status=error_code,
        headers={"Content-Type": "application/json"},
    )

#Determine the website to scrape based on the URL format.
#If the website does not come from domain or raywhite, temporarily return default value or raise error.
@https_fn.on_call()
# def scrape_property_restful(req: https_fn.Request) -> https_fn.Response:
def scrape_property_v2(req: https_fn.Request) -> Any:
    """Scrape a rental advertisement and return the data"""
    try:
        # parameters passed from the client.
        url = req.data["url"]
        
    except (ValueError, KeyError):
        # Throwing an HttpsError so that the client gets the error details.
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                'The function must be cadlled with an parameter, "url", which must be string.'
            ),
        )
        
    # return error if url is not from unsupported website
    if "domain" not in url and "raywhite" not in url:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=(
                'We only support url from Domain and Raywhite.'
            ),
        )
        
    # scrape url
    try: 
        if "domain" in url:
            href, price, bedroom_num, bathroom_num, parking_num, address, images = scrape_domain(url)
        elif "raywhite" in url:
            href, price, bedroom_num, bathroom_num, parking_num, address, images = scrape_raywhite(url)
        else:
            raise Exception("We only support url from Domain and Raywhite.")
    
    except Exception as e:
        # return error when address not found
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=(str(e)),
        )
    # [END v2addHttpsError]

    # [START v2returnAddData]
    property = {
        "url": href,                              #str
        "price": price,                          #int
        "bedroom_num": bedroom_num,              #int
        "bathroom_num": bathroom_num,            #int
        "parking_num": parking_num,              #int
        "address": address,                      #str
        "images": images,                        #a list of str
    }
    return property

# a restful api version of scrape_property_v2
# @https_fn.on_request()
# def scrape_property_restful(req: https_fn.Request) -> https_fn.Response:
#     """Scrape a rental advertisement and return the data"""
#     url = req.args.get("url")
#     if url is None:
#         return create_error_response(
#             400,
#             "The function must be called with an parameter, 'url', which must be string.",
#         )
        
#     try: 
#         if "domain" in url:
#             href, price, bedroom_num, bathroom_num, parking_num, address, images = scrape_domain(url)
#         elif "raywhite" in url:
#             href, price, bedroom_num, bathroom_num, parking_num, address, images = scrape_raywhite(url)
#         else:
#             return create_error_response(
#                 404,
#                 "We only support URL from from Domain or Raywhite.",
#             )
#     except Exception as e:
#         # return error when address not found
#         return create_error_response(
#             500,
#             str(e),
#         )
#     # [END v2addHttpsError]

#     # [START v2returnAddData]
#     property = {
#         "url": href,                             #str
#         "price": price,                          #int
#         "bedroom_num": bedroom_num,              #int
#         "bathroom_num": bathroom_num,            #int
#         "parking_num": parking_num,              #int
#         "address": address,                      #str
#         "images": images,                        #a list of str
#     }
#     return_data = json.dumps({ "property": property }) 
#     return https_fn.Response(return_data, status=200, headers={"Content-Type": "application/json"})


"""
    get longtitude and latitude of the input address
    Input: address (str)
    Output: {"coordinate": {"lat": LAT, "lng": LNG}}
"""
@https_fn.on_call(secrets=["MAPS_API_KEY"])
def get_lnglat_by_address(req: https_fn.Request) -> Any:
    # parameters passed from the client.
    try:
        address = req.data["address"]

    except (ValueError, KeyError):
        # Throwing an HttpsError so that the client gets the error details.
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                'The function must be cadlled with an parameter, "address", which must be string.'
            ),
        )
    
    # get lng and lat of the input address from google gedocode api
    r = requests.get('https://maps.googleapis.com/maps/api/geocode/json?address=' + address + '&key=' + os.environ.get("MAPS_API_KEY"))
    r = r.json()
    # if the address is valid, return the coordinate
    status = r["status"]
    if status == "OK":
        return r["results"][0]["geometry"]["location"]
    
    # if error, return the error message
    elif status == "ZERO_RESULTS":
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=("The address is invalid."),
        )

    elif status == "OVER_DAILY_LIMIT" or status == "OVER_QUERY_LIMIT" or status == "REQUEST_DENIED":

        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.PERMISSION_DENIED,
            message=(r["error_message"]),
        )
    
    else:
        # status == "UNKNOWN_ERROR"
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=(r["google map server error, please try again later."]),
        )
    

# a restful api version of scrape_property_v2
# @https_fn.on_request(secrets=["MAPS_API_KEY"])
# def get_lnglat_by_address_restful(req: https_fn.Request) -> https_fn.Response:
#     # parameters passed from the client.
#     address = req.args.get("address")
#     if address is None:
#         return create_error_response(
#             400,
#             "The function must be called with an parameter, 'address', which must be string.",
#         )
    
#     # get lng and lat of the input address from google gedocode api
#     r = requests.get('https://maps.googleapis.com/maps/api/geocode/json?address=' + address + '&key=' + os.environ.get("MAPS_API_KEY"))
#     r = r.json()
#     # if the address is valid, return the coordinate
#     if r["status"] == "OK":
#         return r["results"][0]["geometry"]["location"]
#     # if error, return the error message
#     else:
#         re = {
#             "error": r["error_message"]
#         }
#         return re
    
    
# # get all properties of a user from firestore
# @https_fn.on_request()
# def get_user_properties_rest(req:  https_fn.Request) -> https_fn.Response:
#     user_id = req.args.get("userId")
#     if user_id is None:
#         return create_error_response(
#             400,
#             "The function must be called with an parameter, 'userId', which must be string.",
#         )
#     try:
#         # get user document
#         coll_user = firestore.client().collection(u'users').document(user_id)
#         user = coll_user.get().to_dict()
#         if user is None:
#             return create_error_response(404, "User not found.")
#         # for each propertyId in user document, get the property document
#         if 'properties' not in user:
#             return json.dumps({ "properties": [] })
#         # for each propertyId in user document, get the property document
#         properties = []
#         for property_id in user['properties']:
#             property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
#             property['propertyId'] = property_id
#             property['price'] = user['properties'][property_id]['price']
#             if ('inspected' in user['properties'][property_id]):
#                 property['inspected'] = user['properties'][property_id]['inspected'] 
#             else:
#                 property['inspected'] = False
#             properties.append(property)
#         return_data = json.dumps({ "properties": properties })
#         return https_fn.Response(return_data, status=200, headers={"Content-Type": "application/json"})
#     except Exception as e:
#         return create_error_response(
#             500,
#             str(e),
#         )

def get_user_properties_helper(user_id):
    if user_id is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                "The function must be called with an parameter, 'userId', which must be string."
            ),
        )
    try:
        # get user document
        coll_user = firestore.client().collection(u'users').document(user_id)
        user = coll_user.get().to_dict()
        if user is None:
            raise https_fn.HttpsError(
                code=https_fn.FunctionsErrorCode.NOT_FOUND,
                message=("User not found."),
            )
        # for each propertyId in user document, get the property document
        if 'properties' not in user:
            print("[get-all-properties]", " user ", user_id, " has no properties")
            return []
        properties = []
        for property_id in user['properties']:
            property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
            # add user-side data to property document
            property['propertyId'] = property_id
            property['price'] = user['properties'][property_id]['price']
            if ('inspected' in user['properties'][property_id]):
                property['inspected'] = user['properties'][property_id]['inspected'] 
            else:
                property['inspected'] = False
            properties.append(property)
        print("[get-all-properties]", properties)
        print("[get-all-properties]", " user ", user_id, " has properties")
        return properties
    
    except Exception as e:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=(str(e)),
        )


        
# get all properties of a user from firestore
@https_fn.on_call()
def get_user_properties(req:  https_fn.Request) -> Any:
    user_id = req.data["userId"]
    return get_user_properties_helper(user_id)
    # if user_id is None:
    #     raise https_fn.HttpsError(
    #         code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
    #         message=(
    #             "The function must be called with an parameter, 'userId', which must be string."
    #         ),
    #     )
    # try:
    #     # get user document
    #     coll_user = firestore.client().collection(u'users').document(user_id)
    #     user = coll_user.get().to_dict()
    #     if user is None:
    #         raise https_fn.HttpsError(
    #             code=https_fn.FunctionsErrorCode.NOT_FOUND,
    #             message=("User not found."),
    #         )
    #     # for each propertyId in user document, get the property document
    #     if 'properties' not in user:
    #         print("[get-all-properties]", " user ", user_id, " has no properties")
    #         return []
    #     properties = []
    #     for property_id in user['properties']:
    #         property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
    #         # add user-side data to property document
    #         property['propertyId'] = property_id
    #         property['price'] = user['properties'][property_id]['price']
    #         if ('inspected' in user['properties'][property_id]):
    #             property['inspected'] = user['properties'][property_id]['inspected'] 
    #         else:
    #             property['inspected'] = False
    #         properties.append(property)
    #     print("[get-all-properties]", properties)
    #     print("[get-all-properties]", " user ", user_id, " has properties")
    #     return properties
    
    # except Exception as e:
    #     raise https_fn.HttpsError(
    #         code=https_fn.FunctionsErrorCode.INTERNAL,
    #         message=(str(e)),
    #     )

# # get a property from firestore combined with user's collected data from that property
# @https_fn.on_request()
# def get_property_by_id_rest(req:  https_fn.Request) -> https_fn.Response:
#     # get property and user id
#     property_id = req.args.get("propertyId")
#     user_id = req.args.get("userId")
#     if property_id is None or user_id is None:
#         return create_error_response(
#             400,
#             "The function must be called with two parameters, 'propertyId' and 'userId, which must be string.",
#         )
#     # get property and user document
#     property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
#     if property is None:
#         return create_error_response(404, "Property not found.")
#     user = firestore.client().collection(u'users').document(user_id).get().to_dict()
#     if user is None:
#         return create_error_response(404, "User not found.")
#     try:
#         user_property = user['properties'][property_id]
#     except Exception as e:
#         return create_error_response(404, "The property does not belong to this user.")
    
#     try:
#         user_property.update(property)
#         return_data = json.dumps({ "property": user_property })
#         return https_fn.Response(return_data, status=200, headers={"Content-Type": "application/json"})
#     except Exception as e:
#         return create_error_response(
#             500,
#             str(e),
#         )
        
# get a property from firestore combined with user's collected data from that property
@https_fn.on_call()
def get_property_by_id(req:  https_fn.Request) -> Any:
    # get property and user id
    property_id = req.data["propertyId"]
    user_id = req.data["userId"]
    if property_id is None or user_id is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                "The function must be called with two parameters, 'propertyId' and 'userId, which must be string."
            ),
        )
    # get property and user document
    property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
    if property is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=( "Property not found."),
        )
    user = firestore.client().collection(u'users').document(user_id).get().to_dict()
    if user is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=("User not found."),
        )
    # get user's collected data from that property
    try:
        user_property = user['properties'][property_id]
    except Exception as e:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=("The property does not belong to this user."),
        )
    # combine user's collected data and property document
    # user's property data will override property document data
    try:
        user_property.update(property)
        return user_property
    except Exception as e:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=(str(e)),
        )
        
# @https_fn.on_request()
# def check_property_exist_rest(req:  https_fn.Request) -> https_fn.Response:
#     user_id = req.args.get("userId")
#     address = req.args.get("address")
#     href = req.args.get("href")
#     if user_id is None:
#         return create_error_response(
#             400,
#             "The function must be called with a parameter, 'userId' , which must be string.",
#         )
#     if address is None and href is None:
#         return create_error_response(
#             400,
#             "The function must be called with either 'address' or 'href' , which must be string.",
#         )
#     try:
#         coll_user = firestore.client().collection(u'users').document(user_id)
#         user = coll_user.get().to_dict()
#         if user is None:
#             return create_error_response(404, "User not found.")
#         for property_id in user['properties']:
#             # get property document
#             property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
#             # check if property exist, return propertyId if exist
#             if (address is not None and property['address'] == address) or (href is not None and property['href'] == href):
#                 return_data = json.dumps({"exist": True, "propertyId": property_id})
#                 return https_fn.Response(return_data, status=200, headers={"Content-Type": "application/json"})
#         # property not exist, return false
#         return_data = json.dumps({"exist": False})
#         return https_fn.Response(return_data, status=200, headers={"Content-Type": "application/json"})
#     except Exception as e:
#         return create_error_response(500, str(e))
    
# check if a property exist in a user's collection
@https_fn.on_call()
def check_property_exist(req: https_fn.Request) -> Any:
    # get data from request
    user_id = req.data["userId"]
    address = req.data["address"]
    href = req.data["href"]
    if user_id is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                "The function must be called with a parameter, 'userId' , which must be string."
            ),
        )
    if address is None and href is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                "The function must be called with either 'address' or 'href' , which must be string."
            ),
        )
    # get user document
    coll_user = firestore.client().collection(u'users').document(user_id)
    user = coll_user.get().to_dict()
    if user is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.NOT_FOUND,
            message=("User not found."),
        )
    # no property in user document
    if 'properties' not in user:
        return_data = { "exist": False }
        return return_data
    try:
        for property_id in user['properties']:
            # get property document
            property = firestore.client().collection(u'properties').document(property_id).get().to_dict()
            # check if property exist, return propertyId if exist
            if (address is not None and address != '' and property['address'] == address) or (href is not None and href != '' and property['href'] == href):
                return_data = { "exist": True, "propertyId": property_id }
                return return_data
        # property not exist, return false
        return_data = { "exist": False }
        return return_data
    except Exception as e:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=(str(e)),
        )


# get the closest facility address of a property winthin 5km
def get_nearby(facility, lat, lng):
    r = requests.get(NEARBY_URL + 'keyword=' + facility +
                    '&location=' + str(lat) + '%2C' + str(lng) +
                    '&rankby=distance' +
                    # '&radius=' + str(radius) +
                    '&key=' + os.environ.get("MAPS_API_KEY"))
                        
    # json method of response object
    # return json format result
    r = r.json()
    status = r["status"]
    if status == "OK":
        return r["results"][0]["name"]+ ", " + r["results"][0]["vicinity"]
    
    # no interested facility within 5km from the property
    elif status == "ZERO_RESULTS":
        return None
    else:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.PERMISSION_DENIED,
            message=(r["error_message"]),
        )


"""
    multiple properties, one interested location addresses
    input: 
    origin: property address, 
    destinations: a list of interested facilities/location specific addresses, 
    interests: a list of interested facilities general keyword (e.g. coles, kfc)
"""
def update_distance2(origins, propertyIds, destination, user_ref):
    # get distance info from google distance matrix api
    re= {}
    # encode destination address
    encoded_destination = quote(destination)

    # get distance info from google distance matrix api for each mode
    for mode in ['driving', 'transit', 'walking']:
        r = requests.get(DISTANCE_URL + 'origins=' + "|".join(origins) +
                        '&destinations=' + destination +
                        '&mode=' + mode +
                        '&key=' + os.environ.get("MAPS_API_KEY"))
                            
        # json method of response object
        x = r.json()
        print(x)

        # check if the destination in returned json is valid and clear
        if x["destination_addresses"][0] == '':
            raise https_fn.HttpsError(
                code=https_fn.FunctionsErrorCode.NOT_FOUND,
                message=("The address is invalid."),
            )
        
        if x["status"] != 'OK':
            raise https_fn.HttpsError(
                code=https_fn.FunctionsErrorCode.NOT_FOUND,
                message=("The address is invalid."),
            )
        
        # Loop through the properties
        for i in range(len(propertyIds)):
            
            if x["rows"][i]["elements"][0]["status"] == 'ZERO_RESULTS':
                print(f"The address is invalid, or the {destination} is too far from {origins[i]}")
                continue
            
            distance = x["rows"][i]["elements"][0]["distance"]["text"]
            duration = x["rows"][i]["elements"][0]["duration"]["text"]

            # if the destination is not in re, add the distance and travel time of the mode to the destination
            if propertyIds[i] not in re:
                re[propertyIds[i]] = {
                    "address": destination,
                    "distance": distance, 
                    mode: duration
                }
            # if the destination is in re, add travel time of the mode to the destination
            else:
                re[propertyIds[i]][mode] = duration
            print(re)

            # if last mode, update distance info to database
            if mode == 'walking':
                update_data = {}
                try:
                    for key, value in re[propertyIds[i]].items():
                        update_data[f"properties.{propertyIds[i]}.distances.{encoded_destination}.{key}"] = value
                    user_ref.update(update_data)
                except Exception as e:
                    raise https_fn.HttpsError(
                        code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
                        message=("The address is invalid, cannot include special characters"),
                    )


"""
    one property, one or multiple interested facilities/location addresses
    input: 
    origin: property address, 
    destinations: a list of interested facilities/location specific addresses, 
    interests: a list of interested facilities general keyword (e.g. coles, kfc)
"""
def update_distance1(origin, destinations, interests, user_ref, path):
    # get distance info from google distance matrix api
    re= {}
    for mode in ['driving', 'transit', 'walking']:
        r = requests.get(DISTANCE_URL + 'origins=' + origin +
                        '&destinations=' + "|".join(destinations) +
                        '&mode=' + mode +
                        '&key=' + os.environ.get("MAPS_API_KEY"))
                            
        # json method of response object
        x = r.json()
        # print(x)
        
        # Loop through the destinations
        for i in range(len(destinations)):

            # check if the destination in returned json is valid and clear
            if x["destination_addresses"][i] == '':
                raise https_fn.HttpsError(
                    code=https_fn.FunctionsErrorCode.NOT_FOUND,
                    message=("The address is invalid."),
                )
            
            if x["rows"][0]["elements"][i]["status"] == 'ZERO_RESULTS':
                print(f"No {x['destination_addresses'][i]} within 5km from {origin}")
                return


            # address = x["destination_addresses"][i]
            distance = x["rows"][0]["elements"][i]["distance"]["text"]
            duration = x["rows"][0]["elements"][i]["duration"]["text"]

            # if the destination is not in re, add the distance and travel time of the mode to the destination
            if interests[i] not in re:
                re[interests[i]] = {
                    "address": destinations[i],
                    "distance": distance, 
                    mode: duration
                }
            # if the destination is in re, add travel time of the mode to the destination
            else:
                re[interests[i]][mode] = duration

            # if last mode, update distance info to database
            if mode == 'walking':
                update_data = {}
                for key, value in re[interests[i]].items():
                    update_data[f"{path}.{interests[i]}.{key}"] = value
                user_ref.update(update_data)


# add a new interested facility
@https_fn.on_call(secrets=["MAPS_API_KEY"])
def add_interested_facility(req: https_fn.Request) -> Any:
     # parameters passed from the client.
    user_id = req.data["userId"]
    facility = req.data["facility"]
    if user_id is None or facility is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                "The function must be called with a parameter, 'userId', 'facility , which must be string."
            ),
        )
    
    # convert facility to lower case
    lower_facility = facility.lower()
    
    # get user document
    user_ref = firestore.client().collection(u'users').document(user_id)
    user = user_ref.get().to_dict()
    # check duplicate
    if "interestedLocations" in user:
        current_interested_locations = user["interestedLocations"]
        lower_current_interested_locations = [location.lower() for location in current_interested_locations]
        # if the facility is already in the user's interested locations, return error
        if lower_facility in lower_current_interested_locations:
            raise https_fn.HttpsError(
                code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
                message=(
                    "Duplicate interested facility."
                ),
            )
            
    if "interestedFacilities" in user:
        current_interested_facilities = user["interestedFacilities"]
        lower_current_interested_facilities = [facility_.lower() for facility_ in current_interested_facilities]
        # if the facility is already in the user's interested facilities, return error
        if lower_facility in lower_current_interested_facilities:
            raise https_fn.HttpsError(
                code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
                message=(
                    "Duplicate interested facility."
                ),
            )
        # add the facility to the user's interested facilities
        else:
            current_interested_facilities.append(facility)
            user_ref.update({"interestedFacilities": current_interested_facilities})

    if "interestedFacilities" not in user:
        # add the facility to the user's interested facilities
        user_ref.update({"interestedFacilities": [facility]})
    
    # get distance info from all properties to the facility
    # get all properties of the user
    properties = get_user_properties_helper(user_id)
    # if the user has no properties
    if len(properties) == 0:
        return "success"
     
    for property in properties:
        propertyId = property["propertyId"]
        property_address = property["address"]
        lat = property["lat"]
        lng = property["lng"]
        facility_address = get_nearby(facility, lat, lng)
    
        # no interested facility within 5km from the property
        if facility_address is None:
            print(f"No {facility} within 5km from {property_address}")
            continue

        
        # Create the path using dot notation
        path = f'properties.{propertyId}.distances'

        # update distance info from property to the facility
        update_distance1(property_address, [facility_address], [facility], user_ref, path)
        
    return "success"


# add a new interested location
@https_fn.on_request(secrets=["MAPS_API_KEY"])
def add_interested_location_restful(req: https_fn.Request) -> https_fn.Response:
     # parameters passed from the client.
    user_id = req.args.get("userId")
    location = req.args.get("location")
    if user_id is None or location is None:
        return create_error_response(
            400,
            "The function must be called with a parameter, 'userId', 'location' , which must be string.",
        )
    
    # convert facility to lower case
    lower_location = location.lower()
    
    # get user document
    user_ref = firestore.client().collection(u'users').document(user_id)
    user = user_ref.get().to_dict()
    # check duplicate
    if "interestedFacilities" in user:
        current_interested_facilities = user["interestedFacilities"]
        lower_current_interested_facilities = [facility_.lower() for facility_ in current_interested_facilities]
        # if the location is already in the user's interested facilities, return error
        if lower_location in lower_current_interested_facilities:
            return create_error_response(
                400,
                "The location is already in the user's interested facilities.",
            )

    if "interestedLocations" in user:
        current_interested_locations = user["interestedLocations"]
        lower_current_interested_locations = [location.lower() for location in current_interested_locations]
        # if the location is already in the user's interested locations, return error
        if lower_location in lower_current_interested_locations:
            return create_error_response(
                400,
                "The location is already in the user's interested locations.",
            )
        # add the location to the user's interested locations
        else:
            current_interested_locations.append(location)
            user_ref.update({"interestedLocations": current_interested_locations})

    if "interestedLocations" not in user:
        # add the location to the user's interested locations
        user_ref.update({"interestedLocations": [location]})
    
    # get distance info from all properties to the location
    # get all properties of the user
    properties = get_user_properties_helper(user_id)
    # if the user has no properties
    if len(properties) == 0:
        return "success"
    
    propertyIds = [property["propertyId"] for property in properties]
    property_addresses = [property["address"] for property in properties]
    update_distance2(property_addresses, propertyIds, location, user_ref)
    return "success"


# # add a new interested facility
# @https_fn.on_request(secrets=["MAPS_API_KEY"])
# def add_interested_facility_restful(req: https_fn.Request) -> https_fn.Response:
#      # parameters passed from the client.
#     user_id = req.args.get("userId")
#     facility = req.args.get("facility")
#     if user_id is None or facility is None:
#         return create_error_response(
#             400,
#             "The function must be called with a parameter, 'userId', 'facility , which must be string.",
#         )
    
#     # get user document
#     user_ref = firestore.client().collection(u'users').document(user_id)
#     user = user_ref.get().to_dict()
#     # check duplicate
#     if "interestedLocations" in user:
#         current_interested_locations = user["interestedLocations"]
#         # if the facility is already in the user's interested locations, return error
#         if facility in current_interested_locations:
#             return create_error_response(
#                 400,
#                 "The facility is already in the user's interested locations.",
#             )
        
#     if "interestedFacilities" in user:
#         current_interested_facilities = user["interestedFacilities"]
#         # if the facility is already in the user's interested facilities, return error
#         if facility in current_interested_facilities:
#             return create_error_response(
#                 400,
#                 "The facility is already in the user's interested facilities.",
#             )
#         # add the facility to the user's interested facilities
#         else:
#             current_interested_facilities.append(facility)
#             user_ref.update({"interestedFacilities": current_interested_facilities})

#     if "interestedFacilities" not in user:
#         # add the facility to the user's interested facilities
#         user_ref.update({"interestedFacilities": [facility]})
    
#     # get distance info from all properties to the facility
#     # convert facility to lower case
#     facility = facility.lower()
#     # get all properties of the user
#     properties = get_user_properties_helper(user_id)
#     # if the user has no properties
#     if len(properties) == 0:
#         return
     
#     for property in properties:
#         propertyId = property["propertyId"]
#         property_address = property["address"]
#         lat = property["lat"]
#         lng = property["lng"]
#         facility_address = get_nearby(facility, lat, lng)
    
#         # no interested facility within 5km from the property
#         if facility_address is None:
#             print(f"No {facility} within 5km from {property_address}")
#             continue

        
#         # Create the path using dot notation
#         path = f'properties.{propertyId}.distances'

#         # update distance info from property to the facility
#         update_distance1(property_address, [facility_address], [facility], user_ref, path)
        
#     return "success"

        
    
        

        



    

