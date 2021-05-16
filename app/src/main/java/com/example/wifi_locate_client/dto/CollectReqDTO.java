package com.example.wifi_locate_client.dto;

import com.example.wifi_locate_client.utils.APInfo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class CollectReqDTO {

    private Integer mapID;

    private List<APInfo> apList;

    private LocationInfoDTO location;

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    public static class LocationInfoDTO {
        private Double x;
        private Double y;
    }
}
