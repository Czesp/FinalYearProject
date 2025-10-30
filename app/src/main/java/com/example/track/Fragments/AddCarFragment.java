package com.example.track.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.track.Model.busModel;
import com.example.track.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.github.rupinderjeet.kprogresshud.KProgressHUD;

public class AddCarFragment extends Fragment {

    private static final String TAG = "AddCarFragment";
    private static final int REQUEST_LOCATION = 1;

    private SwitchMaterial getLocation;
    private MaterialCardView selectPhoto;
    private Uri ImageUri;
    private Bitmap bitmap;
    private ImageView ImgView;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private StorageReference refStorage;
    private String PhotoUrl, CurrentUserID, DocId;
    private String BusLatitude = "", BusLongitude = "", address = "";
    private EditText edName, edRoute, edNum;
    private TextView busLocation;            // text to show address
    private AppCompatButton UploadButton;
    private LocationManager locationManager;
    KProgressHUD ProgressHUD;

    // Fused location client (more reliable than getLastKnownLocation)
    private FusedLocationProviderClient fusedLocationClient;

    public AddCarFragment() {
        // required empty constructor
    }

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.add_vehicle, container, false);

        // --- views ---
        selectPhoto = view.findViewById(R.id.SelectPhoto);
        ImgView = view.findViewById(R.id.BusPhoto);
        UploadButton = view.findViewById(R.id.uploadButton);
        edName = view.findViewById(R.id.BusName);
        edRoute = view.findViewById(R.id.BusRoute);
        edNum = view.findViewById(R.id.BusNum);
        busLocation = view.findViewById(R.id.busLocation);            // IMPORTANT: use TextView variable
        getLocation = view.findViewById(R.id.getLocation);

        // --- firebase ---
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        refStorage = storage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            CurrentUserID = firebaseAuth.getCurrentUser().getUid();
        } else {
            CurrentUserID = "unknown";
        }

        // fused location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // --- click handlers ---
        selectPhoto.setOnClickListener(v -> {
            CheckStoragePermission();
        });

        UploadButton.setOnClickListener(v -> {
            // show location warning if switch is on but no coords
            if (getLocation.isChecked() && (BusLatitude.isEmpty() || BusLongitude.isEmpty())) {
                // try to get location now
                requestLocationAndThenUpload();
            } else {
                // proceed
                UploadImage();
            }
        });

        getLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // request/check permissions then fetch location
                CheckedLocationPermission();
            } else {
                // clear cached coords & address
                BusLatitude = "";
                BusLongitude = "";
                address = "";
                busLocation.setText("Location");
            }
        });

        return view;
    }

    private void ProgressBar(){
        if (getContext() == null) return;
        ProgressHUD = KProgressHUD.create(requireContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setMaxProgress(100)
                .setBackgroundColor(R.color.primary_blue)
                .show();
        ProgressHUD.setProgress(90);
    }
    private void safeDismissHud(){
        if (ProgressHUD != null && ProgressHUD.isShowing()) ProgressHUD.dismiss();
    }

    // --- Location permission & fetch ---
    private void CheckedLocationPermission() {
        // ensure location provider exists
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
            return;
        }
        // check runtime permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            getCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        try {
            // Using fusedLocationClient for a better last location result
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    setLocationFromLocationObj(location);
                } else {
                    // fallback: try to request a single current location
                    // This simple fallback opens Settings prompt to enable GPS if necessary
                    try {
                        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null) {
                            setLocationFromLocationObj(loc);
                        } else {
                            busLocation.setText("Unable to get location");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        busLocation.setText("Unable to get location");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "fused getLastLocation failed: " + e.getMessage());
                busLocation.setText("Unable to get location");
            });
        } catch (Exception e){
            e.printStackTrace();
            busLocation.setText("Location error");
        }
    }

    private void setLocationFromLocationObj(Location location){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        BusLatitude = String.valueOf(latitude);
        BusLongitude = String.valueOf(longitude);
        // get human address
        getAddressFromLatLong(requireContext(), latitude, longitude);
    }

    public void getAddressFromLatLong(Context context, double LATITUDE, double LONGITUDE){
        try{
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(LATITUDE,LONGITUDE,1);
            if(addressList != null && addressList.size()>0){
                address = addressList.get(0).getAddressLine(0);
            } else {
                address = "Lat: " + LATITUDE + ", Lon: " + LONGITUDE;
            }
            if (busLocation != null) {
                busLocation.setText(address);
                busLocation.setSelected(true);
            }
        }catch(Exception e){
            e.printStackTrace();
            if (busLocation != null) busLocation.setText("Address lookup failed");
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Enable GPS").setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    // If switch is on but Upload pressed, try to request location and then upload
    private void requestLocationAndThenUpload(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            // after permission result, user will need to press Upload again (or you can implement a flow to continue)
            Toast.makeText(requireContext(), "Please grant location permission and press Upload again", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressBar();
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                setLocationFromLocationObj(location);
            }
            // proceed to upload even if location null (it will upload empty coords)
            UploadImage();
        }).addOnFailureListener(e -> {
            safeDismissHud();
            Toast.makeText(requireContext(), "Could not get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // --- storage permission & image pick ---
    private void CheckStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }else{
                PickImage();
            }
        }else{
            PickImage();
        }
    }

    private void PickImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // fix; previously "image/" which is broad
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launcher.launch(intent);
    }

    ActivityResultLauncher<Intent> launcher
            = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if(data!=null && data.getData()!=null){
                        ImageUri = data.getData();
                        try{
                            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), ImageUri);
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    if(ImageUri!=null){
                        ImgView.setImageBitmap(bitmap);
                    }
                }
            }
    );

    // --- Image upload to Firebase Storage ---
    private void UploadImage(){
        // show progress
        ProgressBar();

        // validations
        if (TextUtils.isEmpty(edName.getText().toString().trim()) ||
                TextUtils.isEmpty(edRoute.getText().toString().trim()) ||
                TextUtils.isEmpty(edNum.getText().toString().trim())) {
            safeDismissHud();
            Toast.makeText(getContext(),"Please enter all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        if(ImageUri != null){
            // use timestamp for unique file name
            final StorageReference myRef = refStorage.child("photo/" + System.currentTimeMillis() + "_" + ImageUri.getLastPathSegment());
            myRef.putFile(ImageUri)
                    .addOnSuccessListener(taskSnapshot -> myRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                if(uri!=null){
                                    PhotoUrl=uri.toString();
                                    BusInfo(); // now write Firestore
                                } else {
                                    safeDismissHud();
                                    Toast.makeText(getContext(),"Upload failed (no url)",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                safeDismissHud();
                                Toast.makeText(getContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                    )
                    .addOnFailureListener(e -> {
                        safeDismissHud();
                        Toast.makeText(getContext(), "Storage upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            // no image selected: proceed to writing doc (PhotoUrl will be empty)
            BusInfo();
        }
    }

    // --- write bus info to Firestore ---
    private void BusInfo(){

        String name = edName.getText().toString().trim();
        String num  = edNum.getText().toString().trim();
        String route =  edRoute.getText().toString().trim();

        if(TextUtils.isEmpty(name)|| TextUtils.isEmpty(num)|| TextUtils.isEmpty(route)){
            safeDismissHud();
            Toast.makeText(getContext(),"Please enter all the fields",Toast.LENGTH_SHORT).show();
            return;
        }

        // create new document with autogenerated id
        DocumentReference documentReference = firestore.collection("BusInfo").document();
        DocId = documentReference.getId();

        // build model
        double lat = 0.0, lng = 0.0;
        try {
            if (!BusLatitude.isEmpty()) lat = Double.parseDouble(BusLatitude);
            if (!BusLongitude.isEmpty()) lng = Double.parseDouble(BusLongitude);
        } catch (NumberFormatException ignored) {}

        busModel busModel = new busModel(name, num, route, lng, lat, "", PhotoUrl, CurrentUserID);

        busModel.setBusDocID(DocId);

        // write
        documentReference.set(busModel, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    safeDismissHud();
                    Toast.makeText(getContext(),"Upload Successful",Toast.LENGTH_SHORT).show();

                    // clear fields
                    edName.setText("");
                    edRoute.setText("");
                    edNum.setText("");
                    ImgView.setImageResource(R.drawable.camera); // reset preview (camera icon)
                    ImageUri = null;
                    PhotoUrl = "";
                    BusLatitude = "";
                    BusLongitude = "";
                    address = "";
                    busLocation.setText("Location");
                })
                .addOnFailureListener(e -> {
                    safeDismissHud();
                    Toast.makeText(getContext(), "Firestore write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // --- handle runtime permission results ---
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission required to use this feature", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            // storage result (from CheckStoragePermission)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PickImage();
            } else {
                Toast.makeText(requireContext(), "Storage permission required to pick images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current text of the EditTexts
        outState.putString("busName", edName.getText().toString());
        outState.putString("busRoute", edRoute.getText().toString());
        outState.putString("busNum", edNum.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore saved text if available
        if (savedInstanceState != null) {
            edName.setText(savedInstanceState.getString("busName"));
            edRoute.setText(savedInstanceState.getString("busRoute"));
            edNum.setText(savedInstanceState.getString("busNum"));
        }
    }

}
