package com.faizal.shadab.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //UI componenets
    TextView txtLoginSignup;
    EditText edtLoginEmail;
    EditText edtLoginPassword;
    Button btnLogin;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtLoginSignup = findViewById(R.id.txtLoginSignup);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progress_login);
        firebaseAuth = FirebaseAuth.getInstance();

        edtLoginPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    signIn();
                }
                return false;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        txtLoginSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signIn() {

        String email = edtLoginEmail.getText().toString();
        String password = edtLoginPassword.getText().toString();
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                        sendToProfile();
                        finish();
                    }else {
                        Toast.makeText(LoginActivity.this, "Error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });

        }else {
            Toast.makeText(this, "Error: Empty Fields", Toast.LENGTH_SHORT).show();
        }

    }

    public void rootLayoutIsTapped(View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendToProfile(){
        Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
        startActivity(intent);
    }

}
