package com.example.android.capstone_project.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.capstone_project.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationAdapter extends BaseAdapter {

    private Cursor mCursor;
    private final Context mContext;
    private int mSelection = 999;

    @Nullable
    @BindView(R.id.nav_list_tv)
    TextView navTv;

    private final OnClickHandler mClickHandler;

    public interface OnClickHandler{
        void onSourceItemClicked(String source, String category, View view, int position);
    }

    public NavigationAdapter(Context context, OnClickHandler clickHandler){
        mContext = context;
        mClickHandler = clickHandler;
    }

    public void setSelectedItem(int position){
        mSelection = position;
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
    }

    @Override
    public int getCount() {
        if(null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        mCursor.moveToPosition(position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.nav_list_item, null);
        if(mSelection == position){
            view.setBackgroundColor(mContext.getResources().getColor(R.color.color_light_blue));
        }
        ButterKnife.bind(this, view);
        final String source = mCursor.getString(0);
        final String category = mCursor.getString(1);
        navTv.setText(source);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickHandler.onSourceItemClicked(source, category, view, position);
                setSelectedItem(position);
                notifyDataSetChanged();
            }
        });
        return view;
    }
}
