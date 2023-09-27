# The Cloud Functions for Firebase SDK to create Cloud Functions and set up triggers.
from firebase_functions import firestore_fn, https_fn
# The Firebase Admin SDK to access Cloud Firestore.
from firebase_admin import initialize_app, firestore
from typing import Any
import google.cloud.firestore

# libraries
import re
from bs4 import BeautifulSoup
import requests
import json

initialize_app()


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
    url = url

    try:
        res = requests.get(url, headers = headers)
        res.encoding='UTF-8'
        soup=BeautifulSoup(res.text,'lxml')
        return soup

    except Exception as e:
        raise Exception("Failed to scrape url: ", str(e))


#Scrape the property information on the domain website based on the URL.
#Input: URL(str)
#Output: URL (str), price (int), bed_num (int), parking_num (int), address (str),imgs_url (list of str)
def scrape_domain(url):
    html_soup = getHtml(url)

    address = html_soup.find('h1',{'class': 'css-164r41r'}).text

    price_str = html_soup.find('div',{'data-testid': 'listing-details__summary-title'}).text
    price_patterns = re.compile(r'\$(\d{2,4}) per week')
    price_match = price_patterns.search(price_str)
    price = int(price_match.group(1)) if price_match else 0

    beds_baths_parkings_str = html_soup.find('div',{'class':'css-1dtnjt5'}).text
    patterns = {
        'bed': re.compile(r'(\d|−) Bed'),
        'bath': re.compile(r'(\d|−) Bath'),
        'parking': re.compile(r'(\d|−) Parking')
    }
    bed_count, bath_count, parking_count = None, None, None
    for feature, pattern in patterns.items():
        match = pattern.search(beds_baths_parkings_str)
        if match:
            value = match.group(1)
            if feature == 'bed':
                bed_num = 0 if value == '−' else int(value)
            elif feature == 'bath':
                bath_num = 0 if value == '−' else int(value)
            elif feature == 'parking':
                parking_num = 0 if value == '−' else int(value)

    imgs_url = html_soup.find('img',{'class': 'css-bh4wo8'}).get('src')

    return url, price, bed_num, bath_num, parking_num, address, [imgs_url]


#Scrape the property information on the raywhite website based on the URL.
#Input: URL(str)
#Output: URL (str), price (int), bed_num (int), parking_num (int), address (str),imgs_url (list of str)
def scrape_raywhite(url):
    html_soup = getHtml(url)
    
    address = html_soup.find('h1',{'class': 'banner-basic__title'}).text.replace("\n", ", ").strip(", ").strip()

    price_str = html_soup.find('div',{'class': 'property-detail__banner__side__price'}).text.strip()
    match = re.search(r'\$(\d+)/week', price_str)
    price = int(match.group(1)) if match else 0

    beds_baths_parkings_str = html_soup.find('div',{'class':'property-meta'}).text
    beds_baths_parkings_num = re.findall(r'\d+', beds_baths_parkings_str)
    beds_baths_parkings_count= {'bed_num': -1, 'bath_num': -1, 'car_num': -1}
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

    return url, price, beds_baths_parkings_count['bed_num'], beds_baths_parkings_count['bath_num'], beds_baths_parkings_count['car_num'], imgs_url


def create_error_response(error_code:int, message: str) -> https_fn.Response:
    """Create an error response with the given error code and message."""
    return https_fn.Response(
        json.dumps({"error": {"code": error_code, "message": message}}),
        status=error_code,
        headers={"Content-Type": "application/json"},
    )

#Determine the website to scrape based on the URL format.
#If the website does not come from domain or raywhite, temporarily return a default value.
@https_fn.on_request()
def scrape_property_restful(req: https_fn.Request) -> https_fn.Response:
    """Scrape a rental advertisement and return the data"""
    try:
        # parameters passed from the client.
        # url = req.data["url"]
        url = req.args.get("url")
        
    except (ValueError, KeyError):
        # # Throwing an HttpsError so that the client gets the error details.
        # raise https_fn.HttpsError(
        #     code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
        #     message=(
        #         'The function must be called with an parameter, "url", which must be string.'
        #     ),
        # )
        return create_error_response(
            400,
            "The function must be called with an parameter, 'url', which must be string.",
        )
        
    try: 
        if "domain" in url:
            href, price, bedroom_num, bathroom_num, parking_num, address, images = scrape_domain(url)
        elif "raywhite" in url:
            href, price, bedroom_num, bathroom_num, parking_num, address, images = scrape_raywhite(url)
        else:
            # raise https_fn.HttpsError(
            #     code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            #     message=(
            #         'Your URL is not from domain or raywhite.'
            #     ),
            # )
            return create_error_response(
                404,
                "We only support URL from from Domain or Raywhite.",
            )
    except Exception as e:
        # return error when address not found
        # raise https_fn.HttpsError
        #     code=https_fn.FunctionsErrorCode.INTERNAL,
        #     message=(str(e)),
        # )
        return create_error_response(
            500,
            str(e),
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
    return_data = json.dumps({ "property": property}) 
    return https_fn.Response(return_data, status=200, headers={"Content-Type": "application/json"})
    