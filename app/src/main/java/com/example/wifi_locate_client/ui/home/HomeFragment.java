package com.example.wifi_locate_client.ui.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSONObject;
import com.example.wifi_locate_client.R;
import com.example.wifi_locate_client.dto.CollectReqDTO;
import com.example.wifi_locate_client.utils.APInfo;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.WIFI_SERVICE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private OkHttpClient httpClient;
    private SharedPreferences preferences;

    private LiveData<String> host;

    private String prefKeyServerHost;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 设置主 view
        final TextView mainText = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), mainText::setText);

        mainText.setMovementMethod(ScrollingMovementMethod.getInstance());

        // 监听 Host 输入变化并输出到配置中
        final EditText hostEdit = root.findViewById(R.id.edit_server_host);
        hostEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                homeViewModel.setHost(s.toString());
            }
        });

        // 初始化常用变量
        host = homeViewModel.getHost();
        prefKeyServerHost = getString(R.string.pref_server_host);

        // 监听输入框将变化的 IP 输出到标记
        final TextView hostTextView = root.findViewById(R.id.text_server_host_value);
        host.observe(getViewLifecycleOwner(), hostTextView::setText);


        // 启动时加载存储的值
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        homeViewModel.setHost(loadHostValue());

        initWifiScan(root);
        httpClient = new OkHttpClient();

        return root;
    }

    /**
     * 初始化 wifi 扫描
     *
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
                // 发送请求到服务器
                try {
                    postWifiScanResults(wifi);
                } catch (Exception e) {
                    homeViewModel.addText("http post error: " + e.getMessage());
                    System.out.println(e.getMessage());
                }
            }
        }, filter);


        // 按钮触发手动更新
        wifiRefreshBtn.setOnClickListener(v -> {

            // scan wifi
            wifi.startScan();
        });
    }

    /**
     * 打印 WIFI 扫描结果到屏幕
     *
     * @param wifi
     */
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
            homeViewModel.addText("\n****** PLEASE ENABLE WIFI ******\n");
        }
    }

    /**
     * 发送 Wi-Fi 扫描结果到服务器
     *
     * @param wifi
     * @throws Exception
     */
    private void postWifiScanResults(WifiManager wifi) throws Exception {

        if (wifi.isWifiEnabled()) {

            List<ScanResult> scanResults = wifi.getScanResults();

            // 构造请求参数
            List<APInfo> apList = scanResults.stream()
                    .map(ap -> new APInfo(ap.BSSID, ap.level))
                    .collect(Collectors.toList());

            CollectReqDTO reqData = new CollectReqDTO(
                    0,
                    apList
            );

            URL url = new URL(host.getValue() + ":8080" + "/echo");
            RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), JSONObject.toJSONString(reqData));

            Request req = new Request.Builder()
                    .url(url)
                    .post(reqBody)
                    .build();
            sendHTTPReq(req);
        }
    }

    /**
     * 在新线程中发送 HTTP 请求
     *
     * @param req
     */
    private void sendHTTPReq(Request req) {
        new Thread(() -> {
            try {
                Response resp = httpClient.newCall(req).execute();
                if (resp.code() != 200) {
                    throw new Exception("Status error: " + resp.code());
                } else {
                    addMainTextOnUIThread("req success.\n");
                    storeHostValue();
                }
            } catch (Exception e) {
                addMainTextOnUIThread("http thread error:" + e.getMessage());
            }
        }).start();
    }

    /**
     * 回到 UI 线程并追加 mainText
     *
     * @param text
     */
    private void addMainTextOnUIThread(String text) {
        getActivity().runOnUiThread(() -> homeViewModel.addText(text));
    }

    @SuppressLint("CommitPrefEdits")
    private void storeHostValue() {
        preferences.edit().putString(prefKeyServerHost, host.getValue()).apply();
    }

    private String loadHostValue() {
        return preferences.getString(prefKeyServerHost, "http://localhost");
    }
}
