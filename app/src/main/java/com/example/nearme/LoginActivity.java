package com.example.nearme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    LinearLayout linearLayout;
    EditText etUsername;
    EditText etPassword;
    Button btnLogin;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayout = findViewById(R.id.loginParent);
        setContentView(R.layout.activity_login);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnSignUp);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        //if already logged in
//        if(ParseUser.getCurrentUser() != null) goMainActivity();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = String.valueOf(etUsername.getText());
                String password = String.valueOf(etPassword.getText());

                loginUser(username,password);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = String.valueOf(etUsername.getText());
                String password = String.valueOf(etPassword.getText());

                registerUser(username,password);
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUser(String username, String password) {
        ParseUser user = new ParseUser();

        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    goMainActivity();
                }else{
                    Log.e(TAG,"Registration failed",e);
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    goMainActivity();
                } else {
                    //TODO: Better error handling, informing user what's wrong
                    Log.e(TAG,"Login failed",e);
                }
            }
        });
    }
}