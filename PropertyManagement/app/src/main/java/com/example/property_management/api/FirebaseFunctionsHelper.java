package com.example.property_management.api;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class FirebaseFunctionsHelper {

    private FirebaseFunctions mFunctions;

    public FirebaseFunctionsHelper() {
        mFunctions = FirebaseFunctions.getInstance();
//        mFunctions.useEmulator("10.0.2.2", 5001);
    }

    public Task<Object> scrapeProperty(String url) {
        // Create the arguments to the callable function, which is just a single "url" field
        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        return mFunctions
            .getHttpsCallable("scrape_property")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, Object>() {
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                @Override
                public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    Object result = (Object) task.getResult().getData();
                    return result;
                }
            });
    }
}
