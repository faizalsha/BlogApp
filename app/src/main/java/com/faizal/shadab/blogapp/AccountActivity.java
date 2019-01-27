package com.faizal.shadab.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;
import com.google.firebase.auth.FirebaseAuth;


public class AccountActivity extends AppCompatActivity {

    ImageView circleImageView;
    EditText edtUpdateName;
    Button btnSaveChange;


    FirebaseAuth firebaseAuth;
    Toolbar toolbar;

    private static final String[] PERMISSION_READ_WRITE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
        circleImageView = findViewById(R.id.circleImageView);
        edtUpdateName = findViewById(R.id.edtUpdateName);
        btnSaveChange = findViewById(R.id.btnSaveChange);

        firebaseAuth = FirebaseAuth.getInstance();

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(AccountActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(AccountActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AccountActivity.this, PERMISSION_READ_WRITE, REQUEST_PERMISSION_CODE);
                }else {
                    //Permissions Granted


                }


            }
        });
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
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(AccountActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AccountActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}
