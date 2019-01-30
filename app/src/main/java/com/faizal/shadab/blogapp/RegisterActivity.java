package com.faizal.shadab.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    //UI components
    EditText edtRegisterEmail;
    EditText edtRegisterPassword;
    EditText edtRegisterConPassword;
    Button btnCreateAccount;
    Button btnRegisterLogin;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtRegisterConPassword = findViewById(R.id.edtRegisterConPassword);
        btnCreateAccount = findViewById(R.id.btnLogin);
        btnRegisterLogin = findViewById(R.id.btnRegisterLogin);
        progressBar = findViewById(R.id.progress_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        btnRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLoginActivity();
                finish();
            }
        });



        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        edtRegisterConPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                    signUpUser();
                }
                return false;
            }
        });

    }

    private void signUpUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = edtRegisterEmail.getText().toString();
        String password = edtRegisterPassword.getText().toString();
        String confirmPassword = edtRegisterConPassword.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            if (!password.equals(confirmPassword)){
                Toast.makeText(this, "Password and Confirm password does not match", Toast.LENGTH_SHORT).show();
            } else{
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this, "Signed Up", Toast.LENGTH_SHORT).show();
                                    sendToProfile();
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "error:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        }else{
            Toast.makeText(this, "Error: Empty Fields", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void rootLayoutIsTapped(View view){
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken() , 0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void sendToProfile(){
        Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
        startActivity(intent);
    }
}
