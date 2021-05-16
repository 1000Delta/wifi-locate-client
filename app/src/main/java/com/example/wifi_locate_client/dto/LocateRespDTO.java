package com.example.wifi_locate_client.dto;

import com.example.wifi_locate_client.utils.APInfo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class LocateRespDTO {

    private Integer code;

    private String msg;

    private CollectReqDTO.LocationInfoDTO data;
}
