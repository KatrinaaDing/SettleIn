package com.example.property_management.api;

import androidx.annotation.NonNull;

import com.example.property_management.data.NewProperty;
import com.example.property_management.data.Property;
import com.example.property_management.data.RoomData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
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
                        0,
                        0,
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
                        for (Map<String, Object> propertyObj : result) {
                            Property property = new Property(
                                    (String) propertyObj.get("propertyId"),
                                    (String) propertyObj.get("href"),
                                    (String) propertyObj.get("address"),
                                    (float) ((Double) propertyObj.get("lat")).doubleValue(),
                                    (float) ((Double) propertyObj.get("lng")).doubleValue(),
                                    (int) propertyObj.get("numBedrooms"),
                                    (int) propertyObj.get("numBathrooms"),
                                    (int) propertyObj.get("numParking"),
                                    new HashMap<>(),
                                    (ArrayList<String>) propertyObj.get("images"),
                                    (int) propertyObj.get("price")
                            );
                            properties.add(property);
                        }
                        return properties;
                    }
                });
    }
}
