package com.example.track;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.track.Fragments.GarageFragment;
import com.example.track.Fragments.MapFragment;
import com.example.track.Fragments.ProfileFragment;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    private MeowBottomNavigation bottomNavigation;

    private final static int Map = 1;
    private final static int Garage = 2;
    private final static int Profile = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        bottomNavigation=findViewById(R.id.bottom_nav);
        bottomNavigation.add(new MeowBottomNavigation.Model(Map,R.drawable.location));
        bottomNavigation.add(new MeowBottomNavigation.Model(Garage,R.drawable.garage));
        bottomNavigation.add(new MeowBottomNavigation.Model(Profile,R.drawable.ic_baseline_person_24));

        bottomNavigation.show(Garage,true);

        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                // changing color
                return null;
            }
        });

        bottomNavigation.setOnShowListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                Fragment fragment=null;
                if(model.getId()==1){
                    fragment=new MapFragment();
                } else if (model.getId()==2) {
                    fragment=new GarageFragment();
                }else fragment=new ProfileFragment();

                LoadAndReplaceFragment(fragment);
                return null;
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void LoadAndReplaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container,fragment,null)
                .commit();
    }
}