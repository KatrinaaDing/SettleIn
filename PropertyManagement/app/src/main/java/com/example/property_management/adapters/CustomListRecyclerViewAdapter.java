package com.example.property_management.adapters;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.DeleteInterestedFacilityCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.User;
import com.example.property_management.ui.activities.MainActivity;
import com.example.property_management.ui.fragments.base.BasicSnackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomListRecyclerViewAdapter extends RecyclerView.Adapter<CustomListRecyclerViewAdapter.ViewHolder> {

    private User user;

    View view;

    private Boolean isFacility;

    FirebaseUserRepository userRepository = new FirebaseUserRepository();

    public CustomListRecyclerViewAdapter(User user, Boolean isFacility) {
        this.user = user;
        this.isFacility = isFacility;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_interest_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ArrayList<String> interests = getInterests();
        holder.interest.setText(interests.get(position));

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove interested facility/location name from data
                String interestToDelete = interests.get(position);

                // remove interested facility/location for all properties in firebase
                ArrayList<String> propertyIds = getPropertyIds();
                if (propertyIds != null && propertyIds.size() > 0) {
                    // Create an ArrayList to store the keys
                    if (isFacility) {
                        deleteInterestedFacility(interestToDelete, position);
                    } else {
                        deleteInterestedLocation(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return getInterests().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView interest;
        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            interest = itemView.findViewById(R.id.interestTxt);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

    private ArrayList<String> getInterests() {
        if (isFacility) {
            return user.getInterestedFacilities();
        } else {
            return user.getLocationNames();
        }
    }

    private ArrayList<String> getInterestedLocations() {
        return user.getInterestedLocations();
    }

    private ArrayList<String> getPropertyIds() {
        return new ArrayList<>(user.getProperties().keySet());
    }

    // delete interested facility/location
    public void deleteInterestedFacility(String interest_, int position) {
        ArrayList<String> interestsCopy = new ArrayList<>(getInterests());
        interestsCopy.remove(position);
        userRepository.deleteInterestedFacilityLocation(getPropertyIds(), isFacility, interestsCopy, null, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // redirect to main activity on success
                // do more actions
                getInterests().remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getInterests().size());
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }

    public void deleteInterestedLocation(int position) {
        String interest_ = getInterestedLocations().get(position);
        ArrayList<String> interestsCopy = new ArrayList<>(getInterests());
        interestsCopy.remove(position);

        ArrayList<String> interestAddressesCopy = new ArrayList<>(getInterestedLocations());
        interestAddressesCopy.remove(position);

        userRepository.deleteInterestedFacilityLocation(getPropertyIds(), isFacility, interestAddressesCopy, interestsCopy, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // redirect to main activity on success
                // do more actions
                getInterests().remove(position);
                getInterestedLocations().remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getInterests().size());
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }

    // add new facility
    public void addNewInterestedLocation(String interestAddress, String interestName) {
        // get interests names
        ArrayList<String> newInterestNames = new ArrayList<>(getInterests());
        // get interests addresses
        ArrayList<String> newInterestAddresses = new ArrayList<>(getInterestedLocations());
        newInterestAddresses.add(interestAddress);
        newInterestNames.add(interestName);
        HashMap<String, Object> updateDatePayload = new HashMap<>();

        updateDatePayload.put("interestedLocations", newInterestAddresses);
        updateDatePayload.put("locationNames", newInterestNames);


        // add new interest to firebase
        userRepository.updateUserFields(user.getUserId(), updateDatePayload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                // add new facility to local data
                getInterests().add(interestName);
                getInterestedLocations().add(interestAddress);
                notifyItemInserted(getInterests().size() - 1);
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-interest-failure", msg);
            }
        });
    }

    public void addNewInterestedFacility(String facilityToAdd) {
        // copy interested facilities
        ArrayList<String> newInterests = new ArrayList<>(getInterests());
        // prepare payload
        newInterests.add(facilityToAdd);
        HashMap<String, Object> updateDatePayload = new HashMap<>();
        updateDatePayload.put("interestedFacilities", newInterests);


        // add new interest to firebase
        userRepository.updateUserFields(user.getUserId(), updateDatePayload, new UpdateUserCallback() {
            @Override
            public void onSuccess(String msg) {
                // add new facility to local data
                getInterests().add(facilityToAdd);
                notifyItemInserted(getInterests().size() - 1);
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-interest-failure", msg);
            }
        });
    }


}

