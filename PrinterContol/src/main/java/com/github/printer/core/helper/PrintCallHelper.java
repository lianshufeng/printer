package com.github.printer.core.helper;

import com.github.javaparser.resolution.types.ResolvedUnionType;
import com.github.microservice.core.util.net.HttpClient;
import com.github.printer.core.conf.MQTTConf;
import com.github.printer.core.conf.PrinterServerConf;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 打印机助手
 */
@Slf4j
@Component
public class PrintCallHelper {

    @Autowired
    private PrinterServerConf printerServerConf;


    private final static File PrinterTmpFile = new File(System.getProperty("user.dir") + "/printer_tmp/");


    @Autowired
    private MQTTConf mqttConf;

    /**
     * 打印文件
     *
     * @param fileName
     */
    public void printFile(String fileName) {
        log.info("download : {}", fileName);
        File file = downloadFile(fileName);
        callPrint(file);

    }


    /**
     * linux 调用打印机
     *
     * @param file
     * @return
     */
    @SneakyThrows
    private void callPrint(File file) {
        final String cmd = String.format("lpr -P %s %s", this.mqttConf.getDeviceName(), file.getAbsolutePath());
        log.info("print : {}", cmd);
        Runtime.getRuntime().exec(cmd);
    }


    @SneakyThrows
    private File downloadFile(String fileName) {
        String url = String.format("%s/download/%s", printerServerConf.getHostUrl(), fileName);
        log.info("download - {}", url);
        File file = new File(PrinterTmpFile.getAbsolutePath() + "/" + fileName);
        FileUtils.writeByteArrayToFile(file, new HttpClient().get(url));
        return file;
    }


}
