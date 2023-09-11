package com.example.property_management.ui.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.databinding.FragmentProfileBinding;
import com.example.property_management.ui.activities.LoginActivity;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AppCompatActivity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = (AppCompatActivity) getActivity();

        final TextView textView = binding.textProfile;
        profileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // ========================= Components ==========================
        Button logoutBtn = binding.logoutBtn;
        TextView userEmail = binding.userEmail;
        TextView userId = binding.userId;

        // ========================= Listeners ==========================
        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserInfo();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void logout() {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(activity);
        firebaseAuthHelper.signout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void getUserInfo() {
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(activity);
        FirebaseUser user = firebaseAuthHelper.getCurrentUser();
        assert user != null;
        binding.userEmail.setText("User Email: " + user.getEmail());
        binding.userId.setText("User ID: " + user.getUid());

    }
}