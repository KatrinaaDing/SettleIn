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
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.User;
import com.example.property_management.ui.activities.MainActivity;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter for the custom list recycler view.
 * Used for the interested facilities and locations
 */
public class CustomListRecyclerViewAdapter extends RecyclerView.Adapter<CustomListRecyclerViewAdapter.ViewHolder> {
    private User user;
    View view;
    private Boolean isFacility; // true if interested facility, false if interested location
    FirebaseUserRepository userRepository = new FirebaseUserRepository();
    EventListener listener;

    // listener for the event to show or hide no interest placeholder
    public interface EventListener {
        void onEvent(boolean isFacility, boolean ifShowPlaceholder);
    }

    public CustomListRecyclerViewAdapter(User user, Boolean isFacility, EventListener listener) {
        this.user = user;
        this.isFacility = isFacility;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_interest_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // set interested facility/location name to the recycler view
        ArrayList<String> interests = getInterests();
        holder.interest.setText(interests.get(position));

        // set on click listener for delete button
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove interested facility/location name from data
                String interestToDelete = interests.get(position);

                // remove interested facility/location for all properties in firebase
                ArrayList<String> propertyIds = getPropertyIds();
                if (propertyIds != null && propertyIds.size() > 0) {
                    if (isFacility) {
                        deleteInterestedFacility(interestToDelete, position);
                    } else {
                        deleteInterestedLocation(position);
                    }
                }
            }
        });

        // show placeholder if no interested facility, hide otherwise
        if (interests.size() == 0) {
            listener.onEvent(isFacility, true);
        } else {
            listener.onEvent(isFacility, false);
        }
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

    /**
     * Get name of interested facilities/locations
     * @return ArrayList of interested facility/location names
     */
    private ArrayList<String> getInterests() {
        if (isFacility) {
            return user.getInterestedFacilities();
        } else {
            return user.getLocationNames();
        }
    }

    /**
     * Get address of interested locations
     * @return ArrayList of interested location addresses
     */
    private ArrayList<String> getInterestedLocations() {
        return user.getInterestedLocations();
    }

    /**
     * Get all propertyIds of the user
     * @return ArrayList of propertyIds
     */
    private ArrayList<String> getPropertyIds() {
        return new ArrayList<>(user.getProperties().keySet());
    }

    /**
     * Delete interested facility/location
     * @param interest_ facility to delete
     * @param position position of the facility to delete in the recycler view
     */
    public void deleteInterestedFacility(String interest_, int position) {
        ArrayList<String> interestsCopy = new ArrayList<>(getInterests());
        interestsCopy.remove(position);

        // remove interested facility from all properties in firebase
        userRepository.deleteInterestedFacilityLocation(getPropertyIds(), isFacility, interestsCopy, null, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // remove interested facility from local data
                getInterests().remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getInterests().size());

                // show placeholder if no interested facility
                if (getInterests().size() == 0) {
                    listener.onEvent(isFacility, true);
                }
            }

            @Override
            public void onError(String msg) {
                // show error message
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
            }
        });
    }

    /**
     * Delete interested location
     * @param position position of the location to delete in the recycler view
     */
    public void deleteInterestedLocation(int position) {
        String interest_ = getInterestedLocations().get(position);
        ArrayList<String> interestsCopy = new ArrayList<>(getInterests());
        interestsCopy.remove(position);

        ArrayList<String> interestAddressesCopy = new ArrayList<>(getInterestedLocations());
        interestAddressesCopy.remove(position);

        // remove interested location from all properties in firebase
        userRepository.deleteInterestedFacilityLocation(getPropertyIds(), isFacility, interestAddressesCopy, interestsCopy, interest_, new DeleteInterestedFacilityCallback() {
            @Override
            public void onSuccess(String msg) {
                // remove interested location from local data
                getInterests().remove(position);
                getInterestedLocations().remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getInterests().size());

                // show placeholder if no interested location
                if (getInterests().size() == 0) {
                    listener.onEvent(isFacility, true);
                }
            }

            @Override
            public void onError(String msg) {
                // show error message
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
            }
        });
    }

    /**
     * Add new interested location
     * @param interestAddress address of the interested location to add
     * @param interestName name of the interested location to add
     */
    public void addNewInterestedLocation(String interestAddress, String interestName) {
        // get interests names
        ArrayList<String> newInterestNames = new ArrayList<>(getInterests());
        // get interests addresses
        ArrayList<String> newInterestAddresses = new ArrayList<>(getInterestedLocations());
        // prepare payload
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

                // hide placeholder if the first interested location is added
                if (getInterests().size() == 1) {
                    listener.onEvent(isFacility, false);
                }
            }

            @Override
            public void onError(String msg) {
                // show error message
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
            }
        });
    }

    /**
     * Add new interested facility
     * @param facilityToAdd facility to add
     */
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

                // hide placeholder if the first interested facility is added
                if (getInterests().size() == 1) {
                    listener.onEvent(isFacility, false);
                }
            }

            @Override
            public void onError(String msg) {
                String errorMsg = "Error: " + msg + ". Please try again.";
                new BasicSnackbar(((MainActivity) view.getContext()).findViewById(android.R.id.content), errorMsg, "error");
            }
        });
    }


}

