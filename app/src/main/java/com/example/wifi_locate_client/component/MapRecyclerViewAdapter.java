package com.example.wifi_locate_client.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifi_locate_client.R;
import com.example.wifi_locate_client.utils.MapInfo;

import lombok.Getter;

public class MapRecyclerViewAdapter extends RecyclerView.Adapter<MapRecyclerViewAdapter.ViewHolder> {

    private MapInfo[] localMapList;

    private final View.OnClickListener onClickListener;

    @Getter
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(onClickListener);
            textView = view.findViewById(view.getId());
        }
    }

    public MapRecyclerViewAdapter(MapInfo[] mapList, View.OnClickListener l) {
        localMapList = mapList;
        onClickListener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_dashboard, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextView().setText(localMapList[position].getName());
    }

    @Override
    public int getItemCount() {
        return localMapList.length;
    }
}
