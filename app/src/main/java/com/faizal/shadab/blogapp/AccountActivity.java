package com.faizal.shadab.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


public class AccountActivity extends AppCompatActivity {

    //UI components
    ImageView circleImageView;
    EditText edtUpdateName;
    Button btnSaveChange;
    ProgressBar progressBar;
    Toolbar toolbar;

    Uri imageUrifromGallery = null;
    Uri croppedImageUri = null;

    //Firebase instances
    FirebaseAuth firebaseAuth;
    StorageReference firebaseStorageReference;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private String user_uid;
    ValueEventListener valueEventListener;
    ValueEventListener valueEventListenerForName;

    //CONSTANT FIELDS
    private static final String[] PERMISSION_READ_WRITE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int READ_WRITE_PERMISSION_CODE = 10;
    private static final int PHOTO_PICKER_INTENT_CODE = 15;

    private Uri uploadedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //UI Initialization
        toolbar = findViewById(R.id.toolbar);
        circleImageView = findViewById(R.id.circleImageView);
        edtUpdateName = findViewById(R.id.edtUpdateName);
        btnSaveChange = findViewById(R.id.btnSaveChange);
        progressBar = findViewById(R.id.progressBar_upload_picture);

        //creating ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
        setTheme(R.style.ThemeOverlay_AppCompat_Dark);

        //Initializing firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user_uid = firebaseAuth.getUid();
        databaseReference = firebaseDatabase.getReference().child("users").child(user_uid);

        valueEventListenerForName = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userName = dataSnapshot.getValue().toString();
                    edtUpdateName.setText(userName);
                    databaseReference.child("name").removeEventListener(valueEventListenerForName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.child("name").addListenerForSingleValueEvent(valueEventListenerForName);

        //ValueEventListener for root/users/user_id/image_url
        progressBar.setVisibility(View.VISIBLE);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getValue().toString();
                    Glide.with(AccountActivity.this)
                            .load(uid)
                            .into(circleImageView);
                    databaseReference.removeEventListener(valueEventListener);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(AccountActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        /*attaching listener for reading data only once.
        When the user enter here it will check if there is any profile picture chosen(uploaded) by the user if yes it will load
        the image to the image view from the FirebaseDatabase*/
        databaseReference.child("image_url").addListenerForSingleValueEvent(valueEventListener);


        btnSaveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUrifromGallery != null && !TextUtils.isEmpty(edtUpdateName.getText().toString())){
                    uploadImage();
                    databaseReference.child("name").setValue(edtUpdateName.getText().toString().trim());
                }
                else{
                    Toast.makeText(AccountActivity.this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Setting OnClickListener on the ImageView(profile image)
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ask for permission if
                // --android version is above marshmallow &&
                // --permission is not granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(AccountActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(AccountActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AccountActivity.this, PERMISSION_READ_WRITE, READ_WRITE_PERMISSION_CODE);
                }else {
                    // Permissions already Granted
                    chosePhoto();
                    // result will be published in onActivityResult
                }
            }
        });
    }

    private void chosePhoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PHOTO_PICKER_INTENT_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out:
                SignOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SignOut() {
        firebaseAuth.signOut();
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_WRITE_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(AccountActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            chosePhoto();
        } else {
            Toast.makeText(AccountActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_PICKER_INTENT_CODE && resultCode == RESULT_OK){
            imageUrifromGallery = data.getData();
            CropImage.activity(imageUrifromGallery).setAspectRatio(1,1)
                    .start(AccountActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                croppedImageUri = result.getUri();
                circleImageView.setImageURI(croppedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(AccountActivity.this,"Error:" + result.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Upload the profile image with the name of @user_uid
    //in storage/users/user_uid.jpg
    private void uploadImage(){
        progressBar.setVisibility(View.VISIBLE);

        final StorageReference uploadedImagePath = firebaseStorageReference.child("profile").child(user_uid + ".jpg");
        uploadedImagePath.putFile(croppedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AccountActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                    //Getting download url to retrieve image later
                    uploadedImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            uploadedImageUri = uri;
                            //Saving profile image url to the database


                            databaseReference.child("image_url").setValue(uploadedImageUri.toString());


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AccountActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(AccountActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}
