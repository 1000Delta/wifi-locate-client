package com.example.wifi_locate_client.ui.map_list;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.wifi_locate_client.R;
import com.example.wifi_locate_client.utils.MapInfo;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link MapInfo}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MapInfoListRecyclerViewAdapter extends RecyclerView.Adapter<MapInfoListRecyclerViewAdapter.ViewHolder> {

    private final List<MapInfo> mValues;
    private final View mLayout;

    public MapInfoListRecyclerViewAdapter(List<MapInfo> items, View layout) {
        mValues = items;
        mLayout = layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_map_info, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.format("%1$d", mValues.get(position).getId()));
        holder.mContentView.setText(mValues.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public MapInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mView.setOnClickListener(v -> openSelectedMap(mItem.getId()));
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void openSelectedMap(int mapID) {
            // TODO 完成地图跳转
            Snackbar.make(mLayout, toString(), 1000).show();
        }
    }
}