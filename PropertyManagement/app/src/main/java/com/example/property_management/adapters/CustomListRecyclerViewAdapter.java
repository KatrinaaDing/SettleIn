package com.example.property_management.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.property_management.R;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.DeleteInterestedFacilityCallback;
import com.example.property_management.ui.fragments.base.BasicSnackbar;

import java.util.ArrayList;
import java.util.EventListener;

public class CustomListRecyclerViewAdapter extends RecyclerView.Adapter<CustomListRecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> interests;
    private ArrayList<String> propertyIds;

    private Boolean isFacility;

    FirebaseUserRepository userRepository = new FirebaseUserRepository();

    public CustomListRecyclerViewAdapter(ArrayList<String> interests, ArrayList<String> propertyIds, Boolean isFacility) {
        this.interests = interests;
        this.propertyIds = propertyIds;
        this.isFacility = isFacility;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_interest_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.interest.setText(interests.get(position));

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove interested facility/location name from data
                String facilityToDelete = interests.get(position);
                interests.remove(position);
                notifyItemRemoved(position);

                // remove interested facility/location for all properties in firebase
                if (propertyIds != null && propertyIds.size() > 0) {
                    // Create an ArrayList to store the keys
                    deleteInterest(userRepository, propertyIds, isFacility, facilityToDelete);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return interests.size();
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

    public void updateData(ArrayList<String> interests) {
        this.interests = interests;
        notifyDataSetChanged();
    }

    public void setPropertyIds(ArrayList<String> propertyIds) {
        this.propertyIds = propertyIds;
    }

    // params: propertyIds: all the property ids of the user
    // isFacility: true if the interest is a facility, false if it is a location
    // interestedList: the list of interested facilities or locations before the delete
    // interest_: the facility or location to be deleted
    public void deleteInterest(FirebaseUserRepository userRepository, ArrayList<String> propertyIds, Boolean isFacility,String interest_) {
        userRepository.deleteInterestedFacilityLocation(propertyIds, isFacility, interests, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // redirect to main activity on success
                // do more actions
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg;
//                new BasicSnackbar(getActivity().findViewById(android.R.id.content), errorMsg, "error");
                Log.e("add-property-failure", msg);
            }
        });
    }
}

