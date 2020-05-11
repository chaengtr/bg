package com.example.myapplication.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.BookingActivity;
import com.example.myapplication.FavorActivity;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.PromotionActivity;
import com.example.myapplication.UsersActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = "TAG";

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    DocumentReference cusRef;

    String cusId = "";
    String storagePath = "customers/";

    TextView tvUsername, tvPoints, tvLogout, tvRedeem, tvFavor, tvBooking;
    CircleImageView civUserImage;
    ProgressDialog progressDialog;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int PICK_GALLERY_REQUEST_CODE = 300;
    private static final int PICK_CAMERA_REQUEST_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];

    Uri uri;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        cusId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        cusRef = firebaseFirestore.collection("customers").document(cusId);

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        tvUsername = (TextView) view.findViewById(R.id.tv_username);
        tvPoints = (TextView) view.findViewById(R.id.tv_points);
        tvLogout = (TextView) view.findViewById(R.id.tv_logout);
        tvRedeem = (TextView) view.findViewById(R.id.tv_redeem);
        tvFavor = (TextView) view.findViewById(R.id.tv_favor);
        tvBooking = (TextView) view.findViewById(R.id.tv_booking);
        civUserImage = (CircleImageView) view.findViewById(R.id.civ_user_image);

        progressDialog = new ProgressDialog(getActivity());

        //check the user is logged in or not
        if (firebaseAuth.getCurrentUser() == null){
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        //get user information for display
        firebaseFirestore.collection("customers")
                .whereEqualTo("email", firebaseUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                String username = document.getString("cus_username");
                                String points = document.getLong("point").intValue() + " คะแนน";
                                String image = document.getString("cus_image");
                                tvUsername.setText(username);
                                tvPoints.setText(points);
                                if (image.equals("")){
                                    Picasso.get()
                                            .load(R.drawable.ic_user_photo)
                                            .placeholder(R.drawable.ic_user_photo)
                                            .into(civUserImage);
                                    Log.d(TAG, "onComplete: "+R.drawable.ic_user_photo);
                                }else {
                                    try {
                                        Picasso.get().load(image).into(civUserImage);
                                    } catch (Exception e) {
                                        Picasso.get()
                                                .load(R.drawable.ic_user_photo)
                                                .placeholder(R.drawable.ic_user_photo)
                                                .into(civUserImage);
                                    }
                                }

                            }
                        }
                    }
                });

        //go to promotion page
        tvRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PromotionActivity.class);
                startActivity(intent);
            }
        });

        //go to favor page
        tvFavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FavorActivity.class);
                startActivity(intent);
            }
        });

        //logout user
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        //go to booking activity;
        tvBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingActivity.class);
                startActivity(intent);
            }
        });

        civUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "checkStoragePermission: "+result);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result1 && result2;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }


    private void showImagePickDialog(){
        String options[] = {"คลังรูปภาพ", "ถ่ายภาพ"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("เลือกรูปภาพ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                        Log.d(TAG, "onClick: "+"0");
                    }else {
                        pickFromGallery();
                    }
                }else if (which == 1){
                    Log.d(TAG, "onClick: "+"1");
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "โปรดอนุญาตให้เข้าถึงกล้องและรูปภาพของคุณ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "โปรดอนุญาตให้เข้าถึงรูปภาพของคุณ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "requestCode: " +resultCode+" "+RESULT_OK);
        if (resultCode == RESULT_OK){
            if (requestCode == PICK_CAMERA_REQUEST_CODE){
                Log.d(TAG, "onActivityResult: "+PICK_CAMERA_REQUEST_CODE);
                uploadUserImage(uri);
            }
            if (requestCode == PICK_GALLERY_REQUEST_CODE){
                Log.d(TAG, "onActivityResult: "+PICK_GALLERY_REQUEST_CODE);
                uri = data.getData();
                uploadUserImage(uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadUserImage(final Uri uri) {
        progressDialog.setMessage("กำลังเปลี่ยนรูปภาพของคุณ...");
        progressDialog.show();
        String filePathAndName = storagePath + firebaseUser.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            final HashMap<String, Object> results = new HashMap<>();
                            results.put("cus_image", downloadUri.toString());
                            cusRef.update(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Picasso.get().load(downloadUri.toString()).into(civUserImage);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "การเปลี่ยนรูปภาพไม่สำเร็จ...", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else {
                            progressDialog.dismiss();
                            Log.d(TAG, "Some error occured");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
        });

    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, PICK_CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_GALLERY_REQUEST_CODE);
        Log.d(TAG, "pickFromGallery: "+PICK_GALLERY_REQUEST_CODE);
    }


}
