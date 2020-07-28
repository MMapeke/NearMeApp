package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Class/Activity responsible for handling User login/registration
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    private LinearLayout mLinearLayout;
    private EditText mEditUsername;
    private EditText mEditPassword;
    private Button mBtnLogin;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLinearLayout = findViewById(R.id.loginParent);
        setContentView(R.layout.activity_login);
        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnRegister = findViewById(R.id.btnSignUp);
        mEditUsername = findViewById(R.id.etUsername);
        mEditPassword = findViewById(R.id.etPassword);

        //if already logged in
        if (ParseUser.getCurrentUser() != null) goLocationActivity();

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = String.valueOf(mEditUsername.getText());
                String password = String.valueOf(mEditPassword.getText());

                mBtnLogin.setEnabled(false);
                mBtnRegister.setEnabled(false);
                loginUser(username, password);
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = String.valueOf(mEditUsername.getText());
                String password = String.valueOf(mEditPassword.getText());

                mBtnRegister.setEnabled(false);
                mBtnLogin.setEnabled(false);
                registerUser(username, password);
            }
        });
    }

    /**
     * Navigates to Location Activity
     */
    private void goLocationActivity() {
        Intent intent = new Intent(LoginActivity.this, GetLocation.class);
        startActivity(intent);
        finish();
    }

    /**
     * Registers User in background
     *
     * @param username - String, representing username
     * @param password - String, representing password
     */
    private void registerUser(String username, String password) {
        ParseUser user = new ParseUser();

        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    goLocationActivity();
                } else {
                    Log.e(TAG, "Registration failed", e);
                    mBtnRegister.setEnabled(true);
                    mBtnLogin.setEnabled(true);
                }
            }
        });
    }

    /**
     * Logs In User
     *
     * @param username - String, representing username
     * @param password - String, representing password
     */
    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    goLocationActivity();
                } else {
                    //TODO: Better error handling, informing user what's wrong
                    Log.e(TAG, "Login failed", e);
                    mBtnLogin.setEnabled(true);
                    mBtnRegister.setEnabled(true);
                }
            }
        });
    }
}