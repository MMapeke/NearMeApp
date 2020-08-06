package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Class/Activity responsible for handling User login/registration
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";


    private ViewGroup mSceneRoot;
    private Scene mSceneA;
    private Scene mSceneB;

    private TextInputLayout mEditUser;
    private TextInputLayout mEditPass;
    private Button mBtnLogin;
    private Button mBtnRegister;
    private Button mBtnSubmit;
    private Boolean mCreatingNewAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //if already logged in
        if (ParseUser.getCurrentUser() != null) goLocationActivity();

        mBtnLogin = findViewById(R.id.login_btnLogin);
        mBtnRegister = findViewById(R.id.login_btnRegister);

        // Create the scene root for the scenes in this app
        mSceneRoot = (ViewGroup) findViewById(R.id.login_scene_root);

        // Create the scenes
        mSceneA = Scene.getSceneForLayout(mSceneRoot, R.layout.login_scene_a, this);
        mSceneB = Scene.getSceneForLayout(mSceneRoot, R.layout.login_scene_b, this);


        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCreatingNewAccount = false;
                goToInfo();
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCreatingNewAccount = true;
                goToInfo();
            }
        });


    }

    private void goToInfo() {
        Transition testTransition = new Fade();
        testTransition.setDuration(650);
        TransitionManager.go(mSceneB, testTransition);

        mEditUser = findViewById(R.id.etUsername);
        mEditPass = findViewById(R.id.etPassword);
        mBtnSubmit = findViewById(R.id.btnUserSubmit);
        mBtnSubmit.setClickable(true);

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = String.valueOf(mEditUser.getEditText().getText());
                String password = String.valueOf(mEditPass.getEditText().getText());

                mBtnSubmit.setClickable(false);
                if (mCreatingNewAccount) {
                    registerUser(username, password);
                } else {
                    loginUser(username, password);
                }
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
                    mBtnSubmit.setClickable(true);
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
                    mBtnSubmit.setClickable(true);
                    Log.e(TAG, "Login failed", e);
                    String errorMsg = e.getLocalizedMessage();
                    errorMsg = errorMsg.substring(0, 1).toUpperCase() + errorMsg.substring(1);
                    Snackbar.make(mSceneRoot, errorMsg, Snackbar.LENGTH_SHORT)
                            .show();
                    mBtnLogin.setEnabled(true);
                    mBtnRegister.setEnabled(true);
                }
            }
        });
    }
}