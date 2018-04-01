package com.cid_notify.cid_notify.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.cid_notify.cid_notify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BlackListActivity extends AppCompatActivity {
    private ArrayList<String[]> blackList = new ArrayList<>();
    private BlackListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab_add);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View editDialog = LayoutInflater.from(BlackListActivity.this).inflate(R.layout.black_list_dialog, null);
                new AlertDialog.Builder(BlackListActivity.this)
                        .setTitle("Block this number")
                        .setView(editDialog)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = (EditText) editDialog.findViewById(R.id.black_list_text);
                                final String number = editText.getText().toString();
                                if (TextUtils.isEmpty(number)) {
                                    Toast.makeText(BlackListActivity.this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
                                } else {
                                    String pushKey = reference_contacts.child("BlackList").push().getKey();
                                    reference_contacts.child("BlackList").child(pushKey).child("number").setValue(number);
                                    String[] map = {pushKey, number};
                                    Log.d("pushkey", pushKey);
                                    blackList.add(map);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Switch sw = (Switch) findViewById(R.id.auto_block_on_off);
        reference_contacts.child("AutoBlock").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sw.setChecked((boolean) dataSnapshot.getValue());
                reference_contacts.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BlackListActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }

        });
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reference_contacts.child("AutoBlock").setValue(isChecked);
            }
        });


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.block_list);
        recyclerView.hasFixedSize();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new BlackListAdapter(blackList,reference_contacts,BlackListActivity.this);
        recyclerView.setAdapter(adapter);
        reference_contacts.child("BlackList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BlackListActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String[] map = {ds.getKey(), ds.child("number").getValue().toString()};
                    blackList.add(map);
                }
                adapter.notifyDataSetChanged();
                reference_contacts.removeEventListener(this);
            }
        });
    }
}
    class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder> {
        private ArrayList<String[]> mDataset;
        private DatabaseReference myRef;
        private Context context;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView mBlockNumber;
            private ImageButton mEdit;
            private ImageButton mDelete;
            public ViewHolder(View v) {
                super(v);
                mBlockNumber = (TextView)v.findViewById(R.id.text_block_number);
                mEdit = (ImageButton) v.findViewById(R.id.img_btn_edit);
                mDelete = (ImageButton) v.findViewById(R.id.img_btn_del);
            }
        }

        public void addOne(String[] map){
            mDataset.add(map);
            notifyDataSetChanged();
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public BlackListAdapter(ArrayList<String[]> myDataset, DatabaseReference myRef, Context context) {
            mDataset = myDataset;
            this.myRef=myRef;
            this.context=context;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public BlackListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_black_list, parent, false);
            return new BlackListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(BlackListAdapter.ViewHolder holder, final int position) {
            holder.mBlockNumber.setText(mDataset.get(position)[1]);
            holder.mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View editDialog = LayoutInflater.from(context).inflate(R.layout.black_list_dialog, null);
                    new AlertDialog.Builder(context)
                            .setTitle("Block this number")
                            .setView(editDialog)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText editText = (EditText) editDialog.findViewById(R.id.black_list_text);
                                    final String number = editText.getText().toString();
                                    if(TextUtils.isEmpty(number)) {
                                        Toast.makeText(context, R.string.error_field_required, Toast.LENGTH_SHORT).show();
                                    }else{
                                       myRef.child("BlackList").child(mDataset.get(position)[0]).child("number").setValue(number);
                                        String[] map={mDataset.get(position).toString(),number};
                                        mDataset.set(position,map);
                                        notifyDataSetChanged();
                                    }
                                }
                            }).setNegativeButton("Cancel",null).show();
                }
            });
            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    myRef.child("BlackList").child(mDataset.get(position)[0]).removeValue();
                                    mDataset.remove(position);
                                    notifyDataSetChanged();
                                }
                            }).setNegativeButton("No",null).show();
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
