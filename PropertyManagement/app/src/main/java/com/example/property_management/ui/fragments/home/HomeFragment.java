package com.example.property_management.ui.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.property_management.adapters.PropertyCardAdapter;
import com.example.property_management.api.FirebaseFunctionsHelper;
import com.example.property_management.api.FirebasePropertyRepository;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.data.Property;
import com.example.property_management.databinding.FragmentHomeBinding;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.activities.TestActivity;
import com.example.property_management.ui.fragments.base.BasicSnackbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private ArrayList<Property> allProperties;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

//        Button testBtn = binding.testBtn;
//        testBtn.setOnClickListener(view -> {
//            Intent intent = new Intent(getActivity(), TestActivity.class);
//            startActivity(intent);
//        });

        getAllProperties(this.getContext());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Get all properties from the db and display them in the recycler view
     * @param context the context of the fragment
     */
    private void getAllProperties(Context context) {
//        FirebaseUserRepository db = new FirebaseUserRepository();
//        db.getAllUserProperties(new GetAllUserPropertiesCallback() {
//            @Override
//            public void onSuccess(ArrayList<Property> properties) {
//                if (properties.isEmpty()) {
//                    binding.hint.setVisibility(View.VISIBLE);
//                }
//                RecyclerView propertiesRecyclerView = binding.propertiesRecyclerView;
//                propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//                RecyclerView.Adapter propertyCardAdapter = new PropertyCardAdapter(properties);
//                propertiesRecyclerView.setAdapter(propertyCardAdapter);
//
//                allProperties = properties;
//            }
//
//            @Override
//            public void onError(String msg) {
//                new BasicSnackbar(getView(), msg, "error", Snackbar.LENGTH_LONG);
//            }
//        });
        FirebaseFunctionsHelper firebaseFunctionsHelper = new FirebaseFunctionsHelper();
        firebaseFunctionsHelper.getAllProperties()
                .addOnSuccessListener(result -> {
                    ArrayList<Property> properties = (ArrayList<Property>) result;
                    if (properties.isEmpty()) {
                        binding.hint.setVisibility(View.VISIBLE);
                    }
                    RecyclerView propertiesRecyclerView = binding.propertiesRecyclerView;
                    propertiesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                    RecyclerView.Adapter propertyCardAdapter = new PropertyCardAdapter(properties);
                    propertiesRecyclerView.setAdapter(propertyCardAdapter);

                    allProperties = properties;
                })
                .addOnFailureListener(e -> {
                    Log.e("get-all-properties", e.getMessage());
                    new BasicSnackbar(getView(), e.getMessage(), "error", Snackbar.LENGTH_LONG);
                });
    }

    public ArrayList<Property> getAllProperties() {
        return allProperties;
    }
}