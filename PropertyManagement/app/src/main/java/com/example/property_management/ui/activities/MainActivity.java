package com.example.property_management.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.property_management.R;
import com.example.property_management.api.FirebaseAuthHelper;
import com.example.property_management.api.FirebaseUserRepository;
import com.example.property_management.callbacks.GetAllUserPropertiesCallback;
import com.example.property_management.callbacks.UpdateUserCallback;
import com.example.property_management.data.Property;
import com.google.android.material.bottomappbar.BottomAppBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.property_management.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        // =================================== Components ======================================
        // Bottom navigation bar
        BottomAppBar navView = findViewById(R.id.bottomAppBar);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_profile, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);

        // floating activity button (plus button)
        FloatingActionButton fab = findViewById(R.id.addProperty);
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);

        // =================================== Listeners =======================================
        // go to "add property page" when click on plus button
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPropertyActivity.class);
            startActivity(intent);
        });
        bottomAppBar.setNavigationOnClickListener(view -> {
            // can only navigate from other activity to home page
            if (navController.getCurrentDestination().getId() != R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
            }
        });
        // navigate to home page when click on home icon or profile page for profile icon
        bottomAppBar.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            int currentItem = navController.getCurrentDestination().getId();
            // can only navigate from other activity to the page
            if (id == R.id.navigation_home && currentItem != R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
                return true;
            } else if (id == R.id.navigation_profile && currentItem != R.id.navigation_profile) {
                navController.navigate(R.id.navigation_profile);
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
        protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper(this);
        if (!firebaseAuthHelper.isUserSignedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}