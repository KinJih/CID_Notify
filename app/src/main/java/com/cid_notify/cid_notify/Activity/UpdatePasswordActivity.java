package com.cid_notify.cid_notify.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cid_notify.cid_notify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A login screen that offers login via email/password.
 */
public class UpdatePasswordActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */

    // UI references.
    private EditText mOriginPasswordView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up the login form.
        mOriginPasswordView = (EditText) findViewById(R.id.origin_password);
        mPasswordView = (EditText) findViewById(R.id.password);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        mOriginPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptUpdate();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptUpdate();
                    return true;
                }
                return false;
            }
        });
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptUpdate();
                    return true;
                }
                return false;
            }
        });

        Button mUpdatePasswordButton = (Button) findViewById(R.id.update_password_button);
        mUpdatePasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUpdate();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptUpdate() {
        // Reset errors.
        mOriginPasswordView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String originPassword = mOriginPasswordView.getText().toString();
        final String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(originPassword) || !isPasswordValid(originPassword)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mOriginPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(confirmPassword) || !confirmPassword.equals(password)) {
            mConfirmPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //if (!password.equals(confirmPassword)) return;
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), originPassword);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        updatePwd(user,password);
                    }else{
                        Toast.makeText(UpdatePasswordActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                }
            });
        }
    }

    private void updatePwd(FirebaseUser user , String password){
        user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UpdatePasswordActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                }else{
                    Toast.makeText(UpdatePasswordActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                }
            }
        });
    }
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        /*} else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }*/
    }
}