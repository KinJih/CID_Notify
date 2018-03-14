package com.cid_notify.cid_notify.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cid_notify.cid_notify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DevicesActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        listView = (ListView) findViewById(R.id.device_list);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
        reference_contacts.child("Devices").orderByChild("Last_Login").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DevicesActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String de = "Model: "+ds.child("Model").getValue()+"\nLogin time: "+ds.child("Login_Time").getValue();
                    adapter.insert(de,0);
                }
                reference_contacts.removeEventListener(this);
                // Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
