package com.cid_notify.cid_notify.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cid_notify.cid_notify.Model.AdminData;
import com.cid_notify.cid_notify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResetSecondPasswordActivity extends AppCompatActivity {

    // UI references.
    private EditText mCellView;
    private EditText mBirthView;
    private EditText mNewPwdView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_second_password);
        // Set up the login form.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mCellView = (EditText) findViewById(R.id.cellphone);
        mBirthView = (EditText) findViewById(R.id.ur_birth);
        mNewPwdView = (EditText) findViewById(R.id.new_sec_pwd);
        mNewPwdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    check();
                    return true;
                }
                return false;
            }
        });

        Button mResetButton = (Button) findViewById(R.id.btn_reset_sec);
        mResetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void check() {

        // Reset errors.
        mCellView.setError(null);
        mBirthView.setError(null);
        mNewPwdView.setError(null);

        // Store values at the time of the login attempt.
        final String cellphone = mCellView.getText().toString();
        final String birthday = mBirthView.getText().toString();
        final String newPwd = mNewPwdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(newPwd)) {
            mNewPwdView.setError(getString(R.string.error_field_required));
            focusView = mNewPwdView;
            cancel = true;
        }else if(!isPasswordValid(newPwd)){
            mBirthView.setError(getString(R.string.error_invalid_password));
            focusView = mNewPwdView;
            cancel = true;
        }

        if (TextUtils.isEmpty(birthday)) {
            mBirthView.setError(getString(R.string.error_field_required));
            focusView = mBirthView;
            cancel = true;
        }else if(!isBirthdayValid(birthday)){
            mBirthView.setError(getString(R.string.error_invalid_birthday));
            focusView = mBirthView;
            cancel = true;
        }

        if (TextUtils.isEmpty(cellphone)) {
            mCellView.setError(getString(R.string.error_field_required));
            focusView = mCellView;
            cancel = true;
        } else if (!isCellphoneValid(cellphone)) {
            mCellView.setError(getString(R.string.error_invalid_cellphone));
            focusView = mCellView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
            reference_contacts.child("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final AdminData Data = dataSnapshot.getValue(AdminData.class);
                    if (cellphone.equals(Data.getcellphone()) && birthday.equals(Data.getbirthday())){
                        reference_contacts.child("Admin").setValue(new AdminData(newPwd,cellphone,birthday));
                        Toast.makeText(ResetSecondPasswordActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(ResetSecondPasswordActivity.this, "This is incorrect!", Toast.LENGTH_SHORT).show();
                    }
                    //showProgress(false);
                    reference_contacts.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ResetSecondPasswordActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private  void resetDialog(final DatabaseReference reference_contacts,final String cellphone,final String birthday){
        final View editDialog = LayoutInflater.from(ResetSecondPasswordActivity.this).inflate(R.layout.second_password_dialog, null);
        new AlertDialog.Builder(ResetSecondPasswordActivity.this)
                .setTitle("Reset")
                .setView(editDialog)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) editDialog.findViewById(R.id.second_password_text);
                        final String secPwd = editText.getText().toString();
                        if(TextUtils.isEmpty(secPwd)){
                            Toast.makeText(getApplicationContext(), R.string.error_field_required, Toast.LENGTH_SHORT).show();
                        } else {

                        }
                    }
                });
    }
    private boolean isCellphoneValid(String cell) {
        return cell.length() == 10;
    }

    private boolean isBirthdayValid(String birth) {
        return birth.length() == 8;
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
    }


    }




