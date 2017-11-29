package com.cid_notify.cid_notify;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements Filterable{
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
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mFilterData==null?0:mFilterData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextTime;
        private TextView mTextNumber;
        private TextView mTextPerson;

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
}