package com.cid_notify.cid_notify;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.gavin.com.library.listener.GroupListener;
import com.gavin.com.library.StickyDecoration;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private RecyclerView mList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Record> myDataSet = new ArrayList<>();
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mList = (RecyclerView) findViewById(R.id.my_recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        setSupportActionBar(toolbar);

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
                }
            }
        };

        final Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String formatToday = df.format(c.getTime());
        c.add(Calendar.DATE, -1);
        final String formatYesterday = df.format(c.getTime());

        StickyDecoration decoration = StickyDecoration.Builder
                .init(new GroupListener() {
                    @Override
                    public String getGroupName(int position) {
                        if (myAdapter.getmFilterData().size() > position) {
                            String recordDate = myAdapter.getmFilterData().get(position).getDate();
                            return recordDate.equals(formatToday) ? "Today" : recordDate.equals(formatYesterday) ? "Yesterday" : recordDate;
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
        mList.addOnItemTouchListener(
                new RecyclerItemClickListener(MainActivity.this, mList ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                        Uri uri = Uri.parse("https://whoscall.com/zh-TW/tw/"+myDataSet.get(position).getPhoneNum());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
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
        //Toast.makeText(MainActivity.this, "loading...", Toast.LENGTH_SHORT).show();
        DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference(user.getUid());
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
                mList.setAdapter(myAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
                // Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
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
        TextView mailTextView = (TextView) findViewById(R.id.textView);
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
            Uri uri = Uri.parse("https://whoscall.com/zh-TW/tw/076011000");

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.nav_update_password) {
            startActivity(new Intent(MainActivity.this, UpdatePasswordActivity.class));
            //finish();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
