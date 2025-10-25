package com.example.track.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.track.R;
import com.google.android.material.card.MaterialCardView;

public class AddCarFragment extends Fragment {

    private MaterialCardView selectPhoto;
    private Uri ImageUri;
    private Bitmap bitmap;
    public AddCarFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_vehicle, container, false);
        selectPhoto = view.findViewById(R.id.BusPhoto);
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImage();
            }
        });
        return view;
    }

    private void PickImage() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
    }

    ActivityResultLauncher<Intent> launcher
            =registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
            result -> {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            if(data!=null && data.getData()!=null){
                                ImageUri = data.getData();
                                //conberting image into bitmap
                            }
                        }
            }
    );
}
