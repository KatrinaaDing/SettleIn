package com.example.property_management.ui.fragments.base;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

/**
 * Custom AutocompleteSupportFragment to get full address
 */
public class CustomPlaceAutoCompleteFragment extends AutocompleteSupportFragment {
    Place place;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    /**
     * Return address of the selected place
     * @param i The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param i1 The integer result code returned by the child activity
     *                   through its setResult().
     * @param intent An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int i, int i1, Intent intent) {
        super.onActivityResult(i, i1, intent);
        // set full address to search bar
        if (place != null) {
            Log.i("autocomplete-place", "onActivityResult: " + place.getAddress());
            setText(place.getAddress());
        } else {
            Log.i("autocomplete-place", "Place is null");
        }
    }

    /**
     * Set the selected place
     * @param place
     */
    public void setPlace(Place place) {
        this.place = place;
    }
}
