package com.example.property_management.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Helpers {

    /**
     * Close the keyboard
     * @param activity the activity that is currently open
     */
    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Open the location in Google Map
     * @param address the address of the location
     * @param context the context of the activity
     */
    public static void openInGoogleMap(String address, Context context) {
        //reference: https://developers.google.com/maps/documentation/urls/android-intents
        // view location on map
        String uriString = "geo:0,0?q=" + Uri.encode(address);
        Uri uri = Uri.parse(uriString);
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        // If Google Maps app is installed, open it. Else, redirect to web version.
        try {
            Log.d("property-view-on-map", "URI: " + uriString);
            context.startActivity(mapIntent);

        } catch (ActivityNotFoundException e) {
            // Google Maps app is not installed, redirect to web version
            Uri webUri = Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(address));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            context.startActivity(webIntent);
        }
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
