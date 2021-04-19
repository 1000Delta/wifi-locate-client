package com.example.wifi_locate_client.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.wifi_locate_client.R;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        initWifiScan(root);

        return root;
    }

    /**
     * 初始化 wifi 扫描
     * @param root Activity 根对象，用于定位view
     */
    private void initWifiScan(View root) {

        final Button wifiRefreshBtn = root.findViewById(R.id.btn_wifi_refresh);

        WifiManager wifi = (WifiManager) root.getContext().getSystemService(WIFI_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                printWifiScanResults(wifi);
            }
        }, filter);


        // 按钮触发手动更新
        wifiRefreshBtn.setOnClickListener(v -> {

            // scan wifi
            wifi.startScan();
        });
    }

    private void printWifiScanResults(WifiManager wifi) {

        if (wifi.isWifiEnabled()) {

            List<ScanResult> scanResults = wifi.getScanResults();
            if (scanResults.size() > 0) {
                homeViewModel.setText("Scan result:\n");
                homeViewModel.addText("timestamp:" + System.nanoTime());
                scanResults.forEach(res -> homeViewModel.addText(String.format(
                        "\nBSSID: %1$s\nSSID: %2$s\nfrequency: %3$s\nlevel: %4$s\n",
                        res.BSSID, res.SSID, res.frequency, res.level
                )));
            } else {
                homeViewModel.addText("\nNo scan result.");
            }
        } else {
            homeViewModel.addText("****** PLEASE ENABLE WIFI ******\n");
        }
    }
}
