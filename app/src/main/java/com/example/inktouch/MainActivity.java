package com.example.inktouch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.example.inktouch.api.RetrofitClient;
import com.example.inktouch.fragments.HistoryFragment;
import com.example.inktouch.fragments.ProductsFragment;
import com.example.inktouch.fragments.ProfileFragment;
import com.example.inktouch.fragments.TransactionFragment;
import com.example.inktouch.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Set access token for API calls
        RetrofitClient.setAccessToken(sessionManager.getAccessToken());

        initViews();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new ProductsFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure access token is always set when activity resumes
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            String token = sessionManager.getAccessToken();
            if (token != null) {
                RetrofitClient.setAccessToken(token);
            }
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_products) {
                fragment = new ProductsFragment();
            } else if (itemId == R.id.navigation_transaction) {
                fragment = new TransactionFragment();
            } else if (itemId == R.id.navigation_history) {
                fragment = new HistoryFragment();
            } else if (itemId == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void logout() {
        sessionManager.logout();
        navigateToLogin();
    }
}