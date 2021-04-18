package com.example.wifi_locate_client.ui.home;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.wifi_locate_client.R;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        initWifiScan(root);

        return root;
    }

    /**
     * 初始化 wifi 扫描
     * @param root Activity 根对象，用于定位view
     */
    private void initWifiScan(View root) {
        WifiManager wifi = (WifiManager) root.getContext().getSystemService(WIFI_SERVICE);
        final Button wifiRefreshBtn = root.findViewById(R.id.btn_wifi_refresh);

        wifiRefreshBtn.setOnClickListener(v -> {

            System.out.println("click btn");
            if (wifi.isWifiEnabled()) {
                // scan wifi
                wifi.startScan();
                List<ScanResult> scanList = wifi.getScanResults();
                if (scanList.size() > 0) {

                    homeViewModel.appendText("\nScan result:");
                    scanList.forEach(res -> homeViewModel.appendText(String.format(
                            "\nBSSID: %1$s\nSSID: %2$s\nfrequency: %3$s\nlevel: %4$s\n",
                            res.BSSID, res.SSID, res.frequency, res.level
                    )));
                } else {
                    homeViewModel.appendText("\nNo scan result.");
                }
            } else {
                homeViewModel.appendText("****** PLEASE ENABLE WIFI ******\n");
            }
        });
    }
}
