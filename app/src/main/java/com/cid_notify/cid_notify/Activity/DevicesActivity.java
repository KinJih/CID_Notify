package com.cid_notify.cid_notify.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cid_notify.cid_notify.Model.Device;
import com.cid_notify.cid_notify.R;
import com.cid_notify.cid_notify.Util.RecyclerItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class DevicesActivity extends AppCompatActivity {

    private ArrayList<Device> devices = new ArrayList<>();
    private DeviceAdapter adapter;
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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.device_list);
        recyclerView.hasFixedSize();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new DeviceAdapter(devices,DevicesActivity.this);
        recyclerView.setAdapter(adapter);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
        reference_contacts.child("Devices").orderByChild("Last_Login").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DevicesActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //devices.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Device device = ds.getValue(Device.class);
                        devices.add(device);
                    }
                adapter.notifyDataSetChanged();
                reference_contacts.removeEventListener(this);
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(DevicesActivity.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                new AlertDialog.Builder(DevicesActivity.this)
                        .setTitle(R.string.logout_this_device)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reference_contacts.child("Devices").child(devices.get(position).getsid()).child("isBlock").setValue(true);
                                devices.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton(R.string.no,null).show();
            }
            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }
}
 class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private ArrayList<Device> mDataset;
    private Context context;
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mModel;
        private TextView mTime;
        private ImageView mImageView;
        public ViewHolder(View v) {
            super(v);
            mModel = (TextView)v.findViewById(R.id.text_model);
            mTime = (TextView)v.findViewById(R.id.text_login_time);
            mImageView = (ImageView)v.findViewById(R.id.device_image);
        }
        public void setValue(Device device){
            mModel.setText(context.getString(R.string.device_model,device.getModel()));
            mTime.setText(context.getString(R.string.device_login_time ,device.getLogin_Time()));
            mImageView.setImageResource(R.drawable.ic_mobile);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DeviceAdapter(ArrayList<Device> myDataset,Context context) {
        mDataset = myDataset;
        this.context=context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = mDataset.get(position);
        holder.setValue(device);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}