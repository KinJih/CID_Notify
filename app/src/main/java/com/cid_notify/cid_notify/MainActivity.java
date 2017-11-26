package com.cid_notify.cid_notify;

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

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.gavin.com.library.listener.GroupListener;
import com.gavin.com.library.StickyDecoration;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mail;
    private RecyclerView mList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    final ArrayList<Record> myDataSet = new ArrayList<>();
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mList = (RecyclerView) findViewById(R.id.my_recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

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

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    mail = user.getEmail();
                    getData();

                }
            }
        };

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String formatToday = df.format(c.getTime());
        c.add(Calendar.DATE, -1);
        final String formatYesterday = df.format(c.getTime());

        StickyDecoration decoration = StickyDecoration.Builder
                .init(new GroupListener() {
                    @Override
                    public String getGroupName(int position) {
                        if (myDataSet.size() > position) {
                            String recordDate = myDataSet.get(position).getDate();
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
    }
    //???
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getData();
    }

    public void getData() {
        Log.d("RDB", "ReadDB");
        mSwipeRefreshLayout.setRefreshing(true);
        //Toast.makeText(MainActivity.this, "loading...", Toast.LENGTH_SHORT).show();
        DatabaseReference reference_contacts = FirebaseDatabase.getInstance().getReference("members");
        reference_contacts.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
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
                myAdapter = new MyAdapter(myDataSet);
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
        mailTextView.setText(mail);

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

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements Filterable {
        private ArrayList<Record> mData;
        private ArrayList<Record> mFilterData;

        public MyAdapter(ArrayList<Record> mData) {
            this.mData = mData;
            mFilterData=mData;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString().toLowerCase();
                    if (charString.isEmpty()) {
                        mFilterData = mData;
                    } else {
                        ArrayList<Record> filteredList = new ArrayList<>();
                        for (Record record : mData) {
                            if (record.getPhoneNum().toLowerCase().contains(charString) || record.getDate().toLowerCase().contains(charString) || record.getNumber_info().toLowerCase().contains(charString)) {
                                filteredList.add(record);
                            }
                        }
                        mFilterData = filteredList;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mFilterData;
                    filterResults.count=mFilterData.size();
                    return filterResults;
                }
                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFilterData=(ArrayList<Record>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextTime;
            public TextView mTextNumber;
            public TextView mTextPerson;

            public ViewHolder(View v) {
                super(v);
                mTextTime = (TextView) v.findViewById(R.id.text_time);
                mTextNumber = (TextView) v.findViewById(R.id.text_phone_number);
                mTextPerson = (TextView) v.findViewById(R.id.text_person);
            }

            public void setValues(Record record) {
                mTextNumber.setText(record.getPhoneNum());
                mTextPerson.setText(record.getNumber_info());
                mTextTime.setText(record.getTime());
            }
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_style, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Record record = mFilterData.get(position);
            holder.setValues(record);
        }

        @Override
        public int getItemCount() {
            return mFilterData==null?0:mFilterData.size();
        }
    }

}

