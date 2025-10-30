package com.example.track;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.track.Fragments.AddCarFragment;
import com.example.track.Fragments.GarageFragment;
import com.example.track.Fragments.MapFragment;
import com.example.track.Fragments.ProfileFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private MeowBottomNavigation bottomNavigation;

    private static final int MAP_ID = 1;
    private static final int GARAGE_ID = 2;
    private static final int PROFILE_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_nav);

        // Add menu items
        bottomNavigation.add(new MeowBottomNavigation.Model(MAP_ID, R.drawable.location));
        bottomNavigation.add(new MeowBottomNavigation.Model(GARAGE_ID, R.drawable.garage));
        bottomNavigation.add(new MeowBottomNavigation.Model(PROFILE_ID, R.drawable.ic_baseline_person_24));

        // Show Garage fragment by default
        bottomNavigation.show(GARAGE_ID, true);

        // Handle menu clicks (optional styling logic)
        bottomNavigation.setOnClickMenuListener(model -> null);

        // Handle fragment switching
        bottomNavigation.setOnShowListener(model -> {
            Fragment topFragment = getSupportFragmentManager().findFragmentById(R.id.container);

            // If AddCarFragment is visible, don't replace it
            if (topFragment instanceof AddCarFragment) return null;

            Fragment fragment = null;
            switch (model.getId()) {
                case MAP_ID:
                    fragment = new MapFragment();
                    break;
                case GARAGE_ID:
                    fragment = new GarageFragment();
                    break;
                case PROFILE_ID:
                    fragment = new ProfileFragment();
                    break;
            }

            if (fragment != null) LoadAndReplaceFragment(fragment);

            return null;
        });

        // Handle system window insets (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Floating Action Button (Add Vehicle)
        FloatingActionButton addVehicleButton = findViewById(R.id.fabAdd);
        addVehicleButton.setOnClickListener(v -> {
            // Check if AddCarFragment already exists
            Fragment existing = getSupportFragmentManager().findFragmentByTag("AddCarFragment");
            if (existing == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, new AddCarFragment(), "AddCarFragment")
                        .addToBackStack(null)
                        .commit();
            } else {
                // Show the existing fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(existing)
                        .commit();
            }
        });
    }

    private void LoadAndReplaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
