package com.github.printer.core.service;

import com.github.microservice.core.util.JsonUtil;
import com.github.printer.core.conf.MQTTConf;
import com.github.printer.core.helper.PrintCallHelper;
import groovy.transform.AutoImplement;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Component
public class MQTTConnect implements ApplicationRunner {

    @Autowired
    private MQTTConf mqttConf;

    @Autowired
    private PrintCallHelper printCallHelper;

    //尝试次数
    private int tryCount = 0;

    private MQTT mqtt;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        connec();
    }

    /**
     * 接受到数据
     */
    @SneakyThrows
    private void receive(UTF8Buffer destination, Buffer body, Runnable runnable) {
        String destinationName = destination.toString();
        String ret = new String(body.toByteArray(), Charset.forName("UTF-8"));
        log.info("receive : {} - {}", destinationName, ret);
        var content = JsonUtil.toObject(ret, Map.class);
        if (content != null) {
            Optional.ofNullable(content.get("fileName")).ifPresent((it) -> {
                printCallHelper.printFile(String.valueOf(it));
            });
        }
    }


    private void reConnec() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    connec();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timer.cancel();
            }
        }, 3000);

    }

    /**
     * 建立连接
     */
    @SneakyThrows
    private synchronized void connec() {
        MQTTConf.Host[] hosts = mqttConf.getHost();
        MQTTConf.Host host = hosts[tryCount++ % hosts.length];

        mqtt = new MQTT();
        mqtt.setHost(host.getHost(), host.getPort());
        mqtt.setUserName(mqttConf.getUserName());
        mqtt.setPassword(mqttConf.getPassWord());

        final String topicName = mqttConf.getDeviceChannel() + "/" + mqttConf.getDeviceName();


        // 建立连接
        final CallbackConnection connection = mqtt.callbackConnection();
        connection.listener(new Listener() {
            @Override
            public void onConnected() {
                log.info("mqtt: {} ", "onConnected");
            }

            @Override
            public void onDisconnected() {
                log.info("mqtt: {} ", "onConnected");
                reConnec();
            }

            @Override
            public void onPublish(UTF8Buffer destination, Buffer body, Runnable runnable) {
                log.info("mqtt: {} ", "onPublish");
                receive(destination, body, runnable);
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.info("mqtt: {} - {}", "onFailure", throwable.getMessage());
                throwable.printStackTrace();
                reConnec();
            }
        });

        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Topic[] topics = {new Topic(topicName, QoS.AT_LEAST_ONCE)};
                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        log.info("mqtt subscribe : {}", "onSuccess");
                    }

                    public void onFailure(Throwable value) {
                        log.info("mqtt subscribe : {}", "onFailure");
                        value.printStackTrace();
                        reConnec();
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {
                value.printStackTrace();
                reConnec();
            }
        });
    }


}
