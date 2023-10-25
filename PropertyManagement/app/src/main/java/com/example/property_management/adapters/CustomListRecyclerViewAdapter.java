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
import com.example.property_management.data.User;
import com.example.property_management.ui.activities.MainActivity;
import com.example.property_management.ui.fragments.base.BasicSnackbar;

import java.util.ArrayList;
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
                String facilityToDelete = interests.get(position);

                // remove interested facility/location for all properties in firebase
                ArrayList<String> propertyIds = getPropertyIds();
                if (propertyIds != null && propertyIds.size() > 0) {
                    // Create an ArrayList to store the keys
                    deleteInterest(facilityToDelete, position);
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
            return user.getInterestedLocations();
        }
    }

    private ArrayList<String> getPropertyIds() {
        return new ArrayList<>(user.getProperties().keySet());
    }

//    public void updateData(ArrayList<String> interests) {
//        this.interests = interests;
//        notifyDataSetChanged();
//    }

    // delete interested facility/location
    public void deleteInterest(String interest_, int position) {
        ArrayList<String> interestsCopy = new ArrayList<>(getInterests());
        interestsCopy.remove(position);
        userRepository.deleteInterestedFacilityLocation(getPropertyIds(), isFacility, interestsCopy, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // redirect to main activity on success
                // do more actions
                getInterests().remove(position);
                notifyItemRemoved(position);
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((Activity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }

    // add new facility
    public void addNewFacility(String facilityToAdd) {
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();

        // add new facility to firebase
        firebaseFunctionsHelper.addInterestedFacility(user.getUserId(), facilityToAdd)
                .addOnSuccessListener(result -> {
                    if (result.equals("success")) {
//                        new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), "Successfully add new facility", "success");
                        Log.i("add-interested-facility-success", result);

                        // add new facility to local data
                        ArrayList<String> interestedFacilities = getInterests();
                        interestedFacilities.add(facilityToAdd);
                        notifyItemInserted(interestedFacilities.size() - 1);
                    } else {
                        new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), "Error: " + result + " Please try again.", "error");
                        Log.e("add-interested-facility-fail", result);
                    }
                })
                .addOnFailureListener(e -> {
                    // pop error at input box
                    Log.e("add-interested-facility-fail", e.getMessage());
                    new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), "Error: " + e.getMessage() + " Please try again.", "error");
                });
    }

    // add new location
    public void addNewLocation(String locationToAdd) {
        // TODO
        System.out.println("6666666666666666"+locationToAdd);
    }
}

