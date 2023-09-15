# The Cloud Functions for Firebase SDK to create Cloud Functions and set up triggers.
from firebase_functions import firestore_fn, https_fn
# The Firebase Admin SDK to access Cloud Firestore.
from firebase_admin import initialize_app, firestore
from typing import Any
import google.cloud.firestore

# libraries
import random

initialize_app()

@https_fn.on_request()
def hello_world(req: https_fn.Request) -> https_fn.Response:
    """Take the text parameter passed to this HTTP endpoint and insert it into
    a new document in the messages collection."""
    return https_fn.Response(f"hello world")
  
@https_fn.on_call()
def scrape_property(req: https_fn.CallableRequest) -> Any:
    """Scrape a rental advertisement and return the data"""
    try:
        # parameters passed from the client.
        url = req.data["url"]
    except (ValueError, KeyError):
        # Throwing an HttpsError so that the client gets the error details.
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message=(
                'The function must be called with an arguments`, "url", which must be string.'
            ),
        )
    # [END v2addHttpsError]

    # [START v2returnAddData]
    property = {
        "url": url,
        "bedroom_num": random.randrange(1, 5),
        "bathroom_num": random.randrange(1,3),
        "car_num": random.randrange(1,2),
        "address": '16 Charming Street Hampton East VIC 3188',
        "lat": -37.840935,
        "lng": 144.946457
    }
    return property
  
