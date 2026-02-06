package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.myapplication.fragments.BackupFragment;
import com.example.myapplication.fragments.DashboardFragment;
import com.example.myapplication.fragments.ProductsFragment;
import com.example.myapplication.fragments.TransactionsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private MaterialToolbar toolbar;
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Set up toolbar
        setSupportActionBar(toolbar);

        // Set up bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_products) {
                selectedFragment = new ProductsFragment();
            } else if (itemId == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (itemId == R.id.nav_backup) {
                selectedFragment = new BackupFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Load default fragment (Dashboard)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    @Override
    public void onBackPressed() {
        // If already on dashboard → show exit popup
        if (bottomNavigation.getSelectedItemId() == R.id.nav_dashboard) {
            showExitConfirmationDialog();
        } else {
            // Navigate back to dashboard
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you want to exit Inventory Pro?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    // ------------------------------------------
    // Alternative: Double back press to exit
    // (You can enable this instead of dialog)
    // ------------------------------------------

    /*
    @Override
    public void onBackPressed() {
        // Check if currently on dashboard
        if (bottomNavigation.getSelectedItemId() == R.id.nav_dashboard) {
            // Double back press to exit
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                if (backToast != null) {
                    backToast.cancel();
                }
                super.onBackPressed();
                finish();
                return;
            } else {
                backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        } else {
            // Navigate back to dashboard
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }
    */
}
