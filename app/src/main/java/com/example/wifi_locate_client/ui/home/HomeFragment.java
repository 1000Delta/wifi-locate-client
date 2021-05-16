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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.wifi_locate_client.R;
import com.example.wifi_locate_client.dto.CollectReqDTO;
import com.example.wifi_locate_client.dto.LocateReqDTO;
import com.example.wifi_locate_client.dto.LocateRespDTO;
import com.example.wifi_locate_client.utils.APInfo;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private LiveData<Double> locX;
    private LiveData<Double> locY;

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
        locX = homeViewModel.getX();
        locY = homeViewModel.getY();
        prefKeyServerHost = getString(R.string.pref_server_host);

        // 监听输入框将变化的 IP 输出到标记
        final TextView hostTextView = root.findViewById(R.id.text_server_host_value);
        host.observe(getViewLifecycleOwner(), hostTextView::setText);

        // 启动时加载存储的值
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        homeViewModel.setHost(loadHostValue());

        // 记录坐标
        final TextView locXTextView = root.findViewById(R.id.edit_loc_x);
        locXTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    homeViewModel.setX(Double.parseDouble(s.toString()));
                } catch (NumberFormatException e) {
                    homeViewModel.setX(0.0);
                }
            }
        });
        final TextView locYTextView = root.findViewById(R.id.edit_loc_y);
        locYTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    homeViewModel.setY(Double.parseDouble(s.toString()));
                } catch (NumberFormatException e) {
                    homeViewModel.setY(0.0);
                }
            }
        });


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

        final Button wifiCollectBtn = root.findViewById(R.id.btn_wifi_collect);
        final Button wifiLocateBtn = root.findViewById(R.id.btn_wifi_locate);

        WifiManager wifi = (WifiManager) root.getContext().getSystemService(WIFI_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        // 按钮触发手动更新并标记事件处理
        wifiCollectBtn.setOnClickListener(v -> {

            // scan wifi
            wifi.startScan();

            getActivity().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    printWifiScanResults(wifi);
                    // 发送请求到服务器
                    try {
                        collectWifiScanResults(wifi);
                    } catch (Exception e) {
                        homeViewModel.addText("http post error: " + e.getMessage());
                        System.out.println(e.getMessage());
                    }
                }
            }, filter);
        });

        wifiLocateBtn.setOnClickListener(v -> {

            // scan wifi
            wifi.startScan();

            getActivity().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    printWifiScanResults(wifi);
                    // 发送请求到服务器
                    try {
                        locateWifiScanResults(wifi);
                    } catch (Exception e) {
                        homeViewModel.addText("http post error: " + e.getMessage());
                        System.out.println(e.getMessage());
                    }
                }
            }, filter);
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
                homeViewModel.addText(String.format(Locale.CHINA, "timestamp: %s\n", new Date(System.currentTimeMillis()).toString()));
                scanResults.forEach(res -> homeViewModel.addText(String.format(
                        "%1$s: %2$s\n",
                        res.BSSID, res.level
                )));
            } else {
                homeViewModel.addText("\nNo scan result.");
            }
        } else {
            homeViewModel.addText("\n****** PLEASE ENABLE WIFI ******\n");
        }
    }

    /**
     * 上报 Wi-Fi 扫描结果和坐标到服务器
     *
     * @param wifi
     * @throws Exception
     */
    private void collectWifiScanResults(WifiManager wifi) throws Exception {

        if (wifi.isWifiEnabled()) {

            List<ScanResult> scanResults = wifi.getScanResults();

            // 构造请求参数
            List<APInfo> apList = scanResults.stream()
                    .map(ap -> new APInfo(ap.BSSID, ap.level))
                    .collect(Collectors.toList());

            CollectReqDTO reqData = new CollectReqDTO(
                    2,
                    apList,
                    new CollectReqDTO.LocationInfoDTO(locX.getValue(), locY.getValue())
            );

            URL url = new URL(host.getValue() + ":8080" + "/map/collect");
            RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), JSONObject.toJSONString(reqData));

            Request req = new Request.Builder()
                    .url(url)
                    .post(reqBody)
                    .build();
            sendHTTPReq(req);
        }
    }

    /**
     * 根据 Wi-Fi 扫描结果定位坐标
     *
     * @param wifi
     * @throws Exception
     */
    @SuppressLint("DefaultLocale")
    private void locateWifiScanResults(WifiManager wifi) throws Exception {

        if (wifi.isWifiEnabled()) {

            List<ScanResult> scanResults = wifi.getScanResults();

            // 构造请求参数
            List<APInfo> apList = scanResults.stream()
                    .map(ap -> new APInfo(ap.BSSID, ap.level))
                    .collect(Collectors.toList());

            LocateReqDTO reqData = new LocateReqDTO(
                    2,
                    apList
            );

            URL url = new URL(host.getValue() + ":8080" + "/locate");
            RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), JSONObject.toJSONString(reqData));

            Request req = new Request.Builder()
                    .url(url)
                    .post(reqBody)
                    .build();

            // 新线程发送请求
            new Thread(() -> {
                try {
                    Response resp = httpClient.newCall(req).execute();
                    if (resp.code() != 200) {
                        throw new Exception("Status error: " + resp.code());
                    } else {
                        LocateRespDTO data = JSONObject.parseObject(resp.body().string(), LocateRespDTO.class);
                        addMainTextOnUIThread(String.format("location: {%f, %f}\n",
                                data.getData().getX(), data.getData().getY()));
//                        addMainTextOnUIThread();
                        storeHostValue();
                    }
                } catch (Exception e) {
                    addMainTextOnUIThread("http thread error:" + e.toString());
                }
            }).start();
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
                addMainTextOnUIThread("http thread error:" + e.toString());
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
