package com.example.property_management.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.property_management.data.DistanceInfo;
import com.example.property_management.data.NewProperty;
import com.example.property_management.data.Property;
import com.example.property_management.data.RoomData;
import com.example.property_management.data.UserProperty;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseFunctionsHelper {

    private FirebaseFunctions mFunctions;

    public FirebaseFunctionsHelper() {
        mFunctions = FirebaseFunctions.getInstance();
//        mFunctions.useEmulator("10.0.2.2", 5001);
    }

    public Task<NewProperty> scrapeProperty(String url) {
        // Create the arguments to the callable function, which is just a single "url" field
        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        return mFunctions
            .getHttpsCallable("scrape_property_v2")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, NewProperty>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public NewProperty then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    Map<String, Object> result;
                    try{
                        result = (Map<String, Object>) task.getResult().getData();
                    } catch (Exception e) {
                        int msgIdx = e.getMessage().indexOf("n:") + 2;
                        String msg = e.getMessage().substring(msgIdx);
                        throw new Exception(msg);
                    }
                    return new NewProperty(
                        (String) result.get("url"),
                        (int) result.get("bedroom_num"),
                        (int) result.get("bathroom_num"),
                        (int) result.get("parking_num"),
                        (String) result.get("address"),
                        Double.NaN,
                        Double.NaN,
                        (int) result.get("price"),
                        (ArrayList<String>) result.get("images")
                    );
                }

            });
    }

    public Task<String> checkPropertyExists(Map<String, String> payLoad) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        payLoad.put("userId", userId);

        return mFunctions
            .getHttpsCallable("check_property_exist")
            .call(payLoad)
            .continueWith(new Continuation<HttpsCallableResult, String>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    Map<String, Object> result;
                    try{
                        result = (Map<String, Object>) task.getResult().getData();
                    } catch (Exception e) {
                        int msgIdx = e.getMessage().indexOf("n:") + 2;
                        String msg = e.getMessage().substring(msgIdx);
                        throw new Exception(msg);
                    }
                    // return property id if exist, null if not exist
                    if ((boolean) result.get("exist")) {
                        return (String) result.get("propertyId");
                    } else {
                        return null;
                    }
                }
            });
    }

    /**
     * get all properties of the current user
     * @return a task that returns an arraylist of properties
     */
    public Task<ArrayList<Property>> getAllProperties() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("userId", userId);

        return mFunctions
            .getHttpsCallable("get_user_properties")
            .call(payLoad)
            .continueWith(new Continuation<HttpsCallableResult, ArrayList<Property>>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public ArrayList<Property> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    ArrayList<Map<String, Object>> result;
                    try{
                        result = (ArrayList<Map<String, Object>>) task.getResult().getData();
                    } catch (Exception e) {
                        int msgIdx = e.getMessage().indexOf("n:") + 2;
                        String msg = e.getMessage().substring(msgIdx);
                        throw new Exception(msg);
                    }
                    // iterate each object in the result and convert to Property object
                    ArrayList<Property> properties = new ArrayList<>();
                    Log.d("get-all-properties", "result: " + result);
                    for (Map<String, Object> propertyObj : result) {
                        Property property = new Property(
                            (String) propertyObj.get("propertyId"),
                            (String) propertyObj.get("href"),
                            (String) propertyObj.get("address"),
                            (Double) propertyObj.get("lat"),
                            (Double) propertyObj.get("lng"),
                            (int) propertyObj.get("numBedrooms"),
                            (int) propertyObj.get("numBathrooms"),
                            (int) propertyObj.get("numParking"),
                            new HashMap<>(),
                            (ArrayList<String>) propertyObj.get("images"),
                            (int) propertyObj.get("price")
                        );
                        property.setInspected((boolean) propertyObj.get("inspected"));
                        properties.add(property);
                    }
                    return properties;
                }
            });
    }


    /**
     * get longitude and latitude of an address
     * @return a task that returns an a HashMap of longitude and latitude
     */
    public Task<Map<String, Double>> getLngLatByAddress(String address) {
        // Create the arguments to the callable function
        Map<String, String> data = new HashMap<>();
        data.put("address", address);

        return mFunctions
            .getHttpsCallable("get_lnglat_by_address")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, Map<String, Double>>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public Map<String, Double> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    Map<String, Object> result;
                    try{
                        result = (Map<String, Object>) task.getResult().getData();
                    } catch (Exception e) {
                        int msgIdx = e.getMessage().indexOf("n:") + 2;
                        String msg = e.getMessage().substring(msgIdx);
                        throw new Exception(msg);
                    }

                    Map<String, Double> res = new HashMap<>();
                    res.put("lng", (Double) result.get("lng"));
                    res.put("lat", (Double) result.get("lat"));
                    return res;
                }
            });
    }


    /**
     * add a new interested facility
     * get the distance info of the nearest facility from each property of the user
     * @return "success" if success, error message if fail
     */
    public Task<String> addInterestedFacility(String userId, String facility) {
        Map<String, String> data = new HashMap<>();
        data.put("userId", userId);
        data.put("facility", facility);
        Log.i("add-interested-facility", "userId: " + userId + ", facility: " + facility);
        return mFunctions
            .getHttpsCallable("add_interested_facility")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, String>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    String result;
                    try {
                        result = (String) task.getResult().getData();
                    } catch (Exception e) {
                        int msgIdx = e.getMessage().indexOf("n:") + 2;
                        String msg = e.getMessage().substring(msgIdx);
                        throw new Exception(msg);
                    }

                    return result;
                }
            });
    }


    /**
     * add a new interested location
     * get the distance info of the interested location from each property of the user
     * @return "success" if success, error message if fail
     */
    public Task<String> addInterestedLocation(String userId, String location) {
        Map<String, String> data = new HashMap<>();
        data.put("userId", userId);
        data.put("location", location);
        Log.i("add-interested-location", "userId: " + userId + ", location: " + location);
        return mFunctions
            .getHttpsCallable("add_interested_location")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, String>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    String result;
                    try {
                        result = (String) task.getResult().getData();
                    } catch (Exception e) {
                        int msgIdx = e.getMessage().indexOf("n:") + 2;
                        String msg = e.getMessage().substring(msgIdx);
                        throw new Exception(msg);
                    }

                    return result;
                }


            });
    }


    /**
     * get a user's shortlisted property by property id
     * @param propertyId the property id
     * @return
     */
    public Task<Map<String, Object>> getPropertyById(String propertyId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // create the arguments to the callable function
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("propertyId", propertyId);

        return mFunctions
            .getHttpsCallable("get_property_by_id")
            .call(payload)
            .continueWith((Continuation<HttpsCallableResult, Map<String, Object>>) task -> {
                Map<String, Object> result;

                // obtain results from the task
                try{
                    result = (Map<String, Object>) task.getResult().getData();
                } catch (Exception e) {
                    int msgIdx = e.getMessage().indexOf("n:") + 2;
                    String msg = e.getMessage().substring(msgIdx);
                    throw new Exception(msg);
                }

                //Log.d("result set get by getPropertyById function",result.toString());
                //Log.d("InspectedData get by getPropertyById function",result.keySet().toString());
                //Log.d("roomNames get by getPropertyById function",((ArrayList<String>)result.get("roomNames")).toString());
                //Log.d("roomNames class get by getPropertyById function",((ArrayList<String>)result.get("roomNames")).getClass().toString());
                // get result and create Property object
                Property propertyData = new Property(
                        (String) result.get("propertyId"),
                        (String) result.get("href"),
                        (String) result.get("address"),
                        (Double) result.get("lat"),
                        (Double) result.get("lng"),
                        (int) result.get("numBedrooms"),
                        (int) result.get("numBathrooms"),
                        (int) result.get("numParking"),
                        getRoomsData(result, "propertyData"),
                        (ArrayList<String>) result.get("images"),
                        (int) result.get("price")
                );
                // get result and create userProperty object
                boolean inspected = result.get("inspected") == null
                        ? false
                        : (boolean) result.get("inspected");
                // parse date in certain format
                String inspectionDate = result.get("inspectionDate") == null
                        ? ""
                        : (String) result.get("inspectionDate");
                // parse time
                String inspectionTime = result.get("inspectionTime") == null
                        ? ""
                        : (String) result.get("inspectionTime");

                UserProperty userPropertyData = new UserProperty(
                        (String) result.get("propertyId"),
                        inspected,
                        inspectionDate,
                        inspectionTime,
                        (String) result.get("notes"),
                        getPropertyDistancesData(result),
                        getRoomsData(result, "inspectedData"),
                        (int) result.get("price"),
                        new Date(((Double) result.get("createdAt")).longValue()),
                        (ArrayList<String>)result.get("roomNames")
                );

                Map<String, Object> res = new HashMap<>();
                res.put("propertyData", propertyData);
                res.put("userPropertyData", userPropertyData);
                return res;
            });
    }

    /**
     * Map the propertyData or inspectedData from firebase function results to
     * HashMap<String, RoomData>
     * @param result the result from firebase function
     * @param key the key of the "propertyData" or "inspectedData"
     * @return a HashMap<String, RoomData> where String is the room name and RoomData is the room
     *        data object
     */
    private HashMap<String, RoomData> getRoomsData(Map<String, Object> result, String key) {
        // convert propertyData for each room to HashMap<String, RoomData>
        HashMap<String, RoomData> propertyRoomsData = new HashMap<>();
        Log.d("whether getRoomdata excuted",String.valueOf(result.get(key) != null));
        if (result.get(key) != null) {
            Map<String, Object> roomsData = (Map<String, Object>) result.get(key);
            Log.d("roomData in getRoomsData function",roomsData.toString());
            // for each room, create RoomData object
            for (String roomName : roomsData.keySet()) {
                Map<String, Object> entry = (Map<String, Object>) roomsData.get(roomName);
                float brightness = entry.get("brightness")  == null
                        ? 0
                        : ((Double) entry.get("brightness")).floatValue();
                float noise = entry.get("noise") == null
                        ? 0
                        : ((Double) entry.get("noise")).floatValue();
                String windowOrientation = entry.get("windowOrientation") == null
                        ? null
                        : (String) entry.get("windowOrientation");
                ArrayList<String> images = (ArrayList<String>) entry.get("images");

                RoomData singleRoomData = new RoomData(brightness, noise, windowOrientation, images);
                propertyRoomsData.put(roomName, singleRoomData);
            }
        }
        return propertyRoomsData;
    }

    /**
     * Map the distances object from firebase function results to HashMap<String, DistanceInfo>
     * @param result the result from firebase function
     * @return a HashMap<String, DistanceInfo> where String is the insterested facilities/location
     *        and DistanceInfo is the distance information object
     */
    private HashMap<String, DistanceInfo> getPropertyDistancesData(Map<String, Object> result) {
        HashMap<String, DistanceInfo> distances = new HashMap<>();
        if (result.get("distances") != null) {
            Map<String, Object> distancesData = (Map<String, Object>) result.get("distances");
            for (String key : distancesData.keySet()) {
                Map<String, Object> entry = (Map<String, Object>) distancesData.get(key);
                String address = (String) entry.get("address");
                String name = (String) entry.get("name");
                String distance = (String) entry.get("distance");
                String driving = (String) entry.get("driving");
                String transit = (String) entry.get("transit");
                String walking = (String) entry.get("walking");
                distances.put(key, new DistanceInfo(
                        address,
                        name,
                        distance,
                        driving,
                        transit,
                        walking));
            }
        }
        return distances;
    }
}