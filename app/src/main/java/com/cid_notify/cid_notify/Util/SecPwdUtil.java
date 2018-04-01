package com.cid_notify.cid_notify.Util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cid_notify.cid_notify.Activity.BlackListActivity;
import com.cid_notify.cid_notify.Activity.DevicesActivity;
import com.cid_notify.cid_notify.Activity.ResetSecondPasswordActivity;
import com.cid_notify.cid_notify.Model.AdminData;
import com.cid_notify.cid_notify.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SecPwdUtil {

    public static void checkSecPwd(final Context context, final FirebaseUser user,final int flag){
        final View editDialog = LayoutInflater.from(context).inflate(R.layout.second_password_dialog, null);
        new AlertDialog.Builder(context)
                .setTitle("Second password")
                .setView(editDialog)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) editDialog.findViewById(R.id.second_password_text);
                        final String secPwd = editText.getText().toString();
                        if(TextUtils.isEmpty(secPwd)){
                            Toast.makeText(context, R.string.error_field_required, Toast.LENGTH_SHORT).show();
                        } else {
                            final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
                            reference_contacts.child("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    AdminData adminData = dataSnapshot.getValue(AdminData.class);
                                    String pwdSha=EncryptUtil.pwd2sha(secPwd,adminData.getcellphone(),adminData.getbirthday());
                                    if (pwdSha.equals(adminData.getsecondPassword())){
                                        switch (flag){
                                            case 0:context.startActivity(new Intent(new Intent(context, BlackListActivity.class)));break;
                                            case 1: context.startActivity(new Intent(new Intent(context, DevicesActivity.class)));break;
                                            case 2: context.startActivity(new Intent(new Intent(context, ResetSecondPasswordActivity.class)));break;
                                        }
                                    }else{
                                        Toast.makeText(context, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                                    }
                                    reference_contacts.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton("Forget", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(context, ResetSecondPasswordActivity.class));

                    }
                })
                .show();
    }
}
