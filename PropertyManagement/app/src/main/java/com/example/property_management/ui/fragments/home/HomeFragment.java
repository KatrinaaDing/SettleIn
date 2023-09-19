package com.example.property_management.ui.fragments.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.property_management.databinding.FragmentHomeBinding;
import com.example.property_management.ui.activities.PropertyDetailActivity;
import com.example.property_management.ui.activities.TestActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Button propertyBtn = binding.propertyDetailBtn;
        propertyBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PropertyDetailActivity.class);
            startActivity(intent);
        });

        Button testBtn = binding.testBtn;
        testBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), TestActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}