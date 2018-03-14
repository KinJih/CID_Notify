package com.cid_notify.cid_notify.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import com.cid_notify.cid_notify.Model.AdminData;
import com.cid_notify.cid_notify.Model.Record;
import com.cid_notify.cid_notify.R;
import com.cid_notify.cid_notify.Util.DensityUtil;
import com.cid_notify.cid_notify.Util.EncryptUtil;
import com.cid_notify.cid_notify.Util.MyAdapter;
import com.cid_notify.cid_notify.Util.MyFirebaseInstanceIdService;
import com.cid_notify.cid_notify.Util.RecyclerItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.gavin.com.library.listener.GroupListener;
import com.gavin.com.library.StickyDecoration;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Record> myDataSet = new ArrayList<>();
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cid_notify.cid_notify.R.layout.activity_main);
        RecyclerView  mList = (RecyclerView) findViewById(R.id.my_recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mList.hasFixedSize();
        mList.setNestedScrollingEnabled(true);
        mList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        myAdapter = new MyAdapter(myDataSet);
        mList.setAdapter(myAdapter);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    getData();
                    final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
                    reference_contacts.child("Telephone").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            TextView teleTextView = (TextView) findViewById(R.id.telephoneText);
                            teleTextView.setText(String.valueOf(dataSnapshot.getValue()));
                            reference_contacts.removeEventListener(this);
                        }
                    });
                }
            }
        };

        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        final String formatToday = df.format(c.getTime());
        c.add(Calendar.DATE, -1);
        final String formatYesterday = df.format(c.getTime());

        StickyDecoration decoration = StickyDecoration.Builder
                .init(new GroupListener() {
                    @Override
                    public String getGroupName(int position) {
                        if (myAdapter.getmFilterData().size() > position) {
                            String recordDate = myAdapter.getmFilterData().get(position).getDate();
                            return recordDate.equals(formatToday) ? getString(R.string.today) : recordDate.equals(formatYesterday) ? getString(R.string.yesterday) : recordDate;
                        }
                        return null;
                    }
                })
                .setGroupBackground(getResources().getColor(R.color.lightGray))
                .setGroupTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .setGroupHeight(DensityUtil.dip2px(this, 35))
                .setGroupTextSize(DensityUtil.sp2px(this, 20))
                .build();
        mList.addItemDecoration(decoration);
        mList.addOnItemTouchListener(new RecyclerItemClickListener(MainActivity.this, mList ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        /*Uri uri = Uri.parse("https://whoscall.com/zh-TW/tw/"+myDataSet.get(position).getPhoneNum());
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(MainActivity.this, uri);*/
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("PhoneNumber",myDataSet.get(position).getPhoneNum());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.action_copy_to_clipboard, Toast.LENGTH_SHORT).show();
                    }
                })
        );
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getData();
    }

    public void getData() {
        Log.d("RDB", "ReadDB");
        mSwipeRefreshLayout.setRefreshing(true);
        final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
        reference_contacts.child("Records").orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myDataSet.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Record record = ds.getValue(Record.class);
                    myDataSet.add(record);
                }
                Collections.reverse(myDataSet);
                myAdapter.notifyDataSetChanged();
                reference_contacts.removeEventListener(this);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        TextView mailTextView = (TextView) findViewById(R.id.emailText);
        mailTextView.setText(user==null?"":user.getEmail());

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (myAdapter!=null)myAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, DevicesActivity.class));
        } else if (id == R.id.nav_admin) {
            final View editDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.second_password_dailog, null);
            new AlertDialog.Builder(MainActivity.this)
                    .setView(editDialog)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText editText = (EditText) editDialog.findViewById(R.id.second_password_text);
                            final String secPwd = editText.getText().toString();
                            if(TextUtils.isEmpty(secPwd)){
                                Toast.makeText(getApplicationContext(), R.string.error_field_required, Toast.LENGTH_SHORT).show();
                            } else {
                                final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
                                reference_contacts.child("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        AdminData adminData = dataSnapshot.getValue(AdminData.class);
                                        String pwdSha=EncryptUtil.pwd2sha(secPwd,adminData.getcellphone(),adminData.getbirthday());
                                        if (pwdSha.equals(adminData.getsecondPassword())){
                                            startActivity(new Intent(MainActivity.this, UpdatePasswordActivity.class));
                                        }else{
                                            Toast.makeText(MainActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                                        }
                                        reference_contacts.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
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
                            startActivity(new Intent(MainActivity.this, ResetSecondPasswordActivity.class));
                        }
                    })
                    .show();
        } else if (id == R.id.nav_logout) {
            final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
            reference_contacts.child("Devices").child(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).removeValue();
            mAuth.signOut();
        } else if (id == R.id.nav_about_page) {
            startActivity(new Intent(MainActivity.this, MyAboutPage.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        //CustomTabsClient.connectAndInitialize(this, "com.android.chrome");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
