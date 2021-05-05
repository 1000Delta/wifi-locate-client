package com.example.wifi_locate_client.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class APInfo {

    private String bssid;

    private Integer rssi;
}
