package com.example.property_management.api;

import androidx.annotation.NonNull;

import com.example.property_management.data.NewProperty;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

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
                        (int) result.get("price")
                    );
                }

            });
    }
}
