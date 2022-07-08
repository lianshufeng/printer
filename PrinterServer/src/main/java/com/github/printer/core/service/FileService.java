package com.github.printer.core.service;

import com.github.microservice.components.activemq.client.MQClient;
import com.github.printer.core.conf.TopicConf;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    private final static File PrinterTmpFile = new File(System.getProperty("user.dir") + "/printer_tmp/");


    @Autowired
    MQClient mq;

    @Autowired
    private TopicConf topicConf;


    private Timer timer = new Timer();


    @SneakyThrows
    public Object saveFile(MultipartFile files, String device) {
        if (!PrinterTmpFile.exists()) {
            PrinterTmpFile.mkdirs();
        }

        final String topic = String.format("%s.%s", topicConf.getTopic(), device);
        String suffixName = FilenameUtils.getExtension(files.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + suffixName;
        File saveFile = new File(PrinterTmpFile.getAbsolutePath() + "/" + fileName);
        @Cleanup InputStream inputStream = files.getInputStream();
        @Cleanup OutputStream outputStream = new FileOutputStream(saveFile);
        StreamUtils.copy(inputStream, outputStream);


        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (saveFile.exists()) {
                    log.info("删除过期文件 : {}", saveFile.getAbsolutePath());
                    saveFile.delete();
                }
            }
        }, 1000L * 60 * 30);

        Object ret = Map.of(
                "createTime", System.currentTimeMillis(),
                "fileName", fileName
        );

        mq.sendObject(topic, ret);
        return ret;
    }


    @SneakyThrows
    public void export(HttpServletResponse response, String fileName) {
        File file = new File(PrinterTmpFile.getAbsolutePath() + "/" + fileName);
        Assert.isTrue(file.exists(), "文件不存在");
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        response.addHeader("Content-Disposition", "filename=" + fileName);
        @Cleanup OutputStream outputStream = response.getOutputStream();
        StreamUtils.copy(fileInputStream, outputStream);
        outputStream.flush();
    }


}
