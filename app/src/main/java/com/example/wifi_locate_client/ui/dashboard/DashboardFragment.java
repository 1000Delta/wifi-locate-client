package com.example.wifi_locate_client.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifi_locate_client.R;
import com.example.wifi_locate_client.component.MapRecyclerViewAdapter;
import com.example.wifi_locate_client.utils.MapInfo;
import com.google.android.material.snackbar.Snackbar;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        // 获取 list 对象
        final RecyclerView mapListView = root.findViewById(R.id.recycler_view_map_list);
        // 设置一维布局
        mapListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 设置 adapter
        MapInfo[] mapInfos = new MapInfo[]{
                new MapInfo(1, "test"),
                new MapInfo(2, "none")
        };
        mapListView.setAdapter(new MapRecyclerViewAdapter(mapInfos, v -> Snackbar.make(root, "1", 1000).show()));

        return root;
    }
}
