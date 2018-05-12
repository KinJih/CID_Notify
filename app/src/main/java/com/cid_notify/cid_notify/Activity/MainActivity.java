package com.cid_notify.cid_notify.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    private Map<String, String> map;
    private int permission;
    private SharedPreferences pref;
    private NavigationView navigationView;
    private DatabaseReference reference_contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView  mList = (RecyclerView) findViewById(R.id.my_recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

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
                    reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
                    if (permission != PackageManager.PERMISSION_GRANTED) {//未取得權限，向使用者要求允許權限
                        ActivityCompat.requestPermissions( MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
                    }else{//已有權限，可進行檔案存取
                        readContacts();
                        getData();
                        reference_contacts.child("Telephone").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                TextView teleTextView = (TextView) findViewById(R.id.telephoneText);
                                teleTextView.setText(String.valueOf(dataSnapshot.getValue()));
                                TextView mailTextView = (TextView) findViewById(R.id.emailText);
                                mailTextView.setText(user.getEmail());
                                reference_contacts.removeEventListener(this);
                            }
                        });
                    }
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        pref = getSharedPreferences("Settings", MODE_PRIVATE);
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
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" +myDataSet.get(position).getPhoneNum());
                intent.setData(data);
                startActivity(intent);
                }

            @Override
            public void onLongItemClick(View view, final int position) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Record record = myAdapter.getmFilterData().get(position);
                                reference_contacts.child("Records").child(record.getDate()+" "+record.getFullTime()).removeValue();
                                myAdapter.deleteRecord(record);
                            }
                        }).setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }));

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //取得聯絡人權限，進行存取
                readContacts();
                getData();
            } else {
                //使用者拒絕權限，顯示對話框告知
                new AlertDialog.Builder(this)
                        .setMessage(R.string.contact_permission)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        }
    }

    private void readContacts(){
        Cursor contacts_name = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);
        map = new HashMap<>();
        while (contacts_name.moveToNext()) {
            String phoneNumber = "";
            long id = contacts_name.getLong(
                    contacts_name.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor contacts_number = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + "=" + Long.toString(id),
                    null,
                    null);

            while (contacts_number.moveToNext()) {
                phoneNumber = contacts_number
                        .getString(contacts_number.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            contacts_number.close();
            String name = contacts_name.getString(contacts_name
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if(!phoneNumber.equals(""))map.put(phoneNumber.replace(" ","").replace("-",""), name);
        }
    }

    private void getData() {
        Log.d("RDB", "ReadDB");
        mSwipeRefreshLayout.setRefreshing(true);
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
                    if(map.get(record.getPhoneNum())!=null)record.setNumber_info((map.get(record.getPhoneNum())));
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

        navigationView.getMenu().findItem(R.id.nav_notification).setTitle(getResources().getString(
                R.string.notification_of_off,pref.getBoolean("Notification",true)?getString(R.string.on):getString(R.string.off)));

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setMaxWidth(Integer.MAX_VALUE);
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
                myAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id==R.id.nav_blacklist){
            startActivity(new Intent(new Intent(MainActivity.this, BlackListActivity.class)));
        }else if(id==R.id.nav_notification){
            boolean noti = pref.getBoolean("Notification",true);
            if (noti){
                Toast.makeText(MainActivity.this,R.string.off,Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this,R.string.on,Toast.LENGTH_SHORT).show();
            }
            pref.edit().putBoolean("Notification",!noti).apply();
            navigationView.getMenu().findItem(R.id.nav_notification).setTitle(getResources().getString(
                    R.string.notification_of_off,!noti?getString(R.string.on):getString(R.string.off)));
        }else if (id == R.id.nav_device) {
            startActivity(new Intent(new Intent(MainActivity.this, DevicesActivity.class)));
        }else if (id == R.id.nav_pwd) {
            final View editDialog = LayoutInflater.from(this).inflate(R.layout.second_password_dialog, null);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.second_password)
                    .setView(editDialog)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText editText = (EditText) editDialog.findViewById(R.id.second_password_text);
                            final String secPwd = editText.getText().toString();
                            if(TextUtils.isEmpty(secPwd)){
                                Toast.makeText(MainActivity.this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
                            } else {
                                final DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
                                reference_contacts.child("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        AdminData adminData = dataSnapshot.getValue(AdminData.class);
                                        String pwdSha= EncryptUtil.pwd2sha(secPwd,adminData.getcellphone(),adminData.getbirthday());
                                        if (pwdSha.equals(adminData.getsecondPassword())){
                                            startActivity(new Intent(MainActivity.this,UpdatePasswordActivity.class));
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
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.forgot, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, ResetSecondPasswordActivity.class));

                        }
                    })
                    .show();
        }else if (id == R.id.nav_logout) {
            reference_contacts.child("Devices").child(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).removeValue();
            mAuth.signOut();
        }else if (id == R.id.nav_about_page) {
            startActivity(new Intent(MainActivity.this, MyAboutPage.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user!=null){
            reference_contacts.child("Devices").child(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("isBlock").exists()) {
                        reference_contacts.child("Devices").child(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).removeValue();
                        mAuth.signOut();
                        reference_contacts.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
