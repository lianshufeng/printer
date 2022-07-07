package com.github.printer.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "mqtt")
public class MQTTConf {

    private Host[] host;
    private String userName;
    private String passWord;

    //管道明
    private String deviceChannel = "test";

    //设备名
    private String deviceName = "";


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Host {
        private String host;
        private int port;
    }


}
