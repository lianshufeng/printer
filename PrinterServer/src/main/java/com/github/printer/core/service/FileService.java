package com.github.printer.core.service;

import com.github.microservice.components.activemq.client.MQClient;
import com.github.microservice.core.util.net.HttpClient;
import com.github.microservice.core.util.net.apache.UrlEncodeUtil;
import com.github.printer.core.conf.TopicConf;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class FileService {

    private final static File PrinterTmpFile = new File(System.getProperty("user.dir") + "/printer_tmp/");


    @Autowired
    MQClient mq;

    @Autowired
    private TopicConf topicConf;


    private Timer timer = new Timer();


    private final static String DocApi = "https://doc.api.jpy.wang";

//    @SneakyThrows
//    private InputStream txt2Pdf(InputStream body) {
//        //转换为base64
//        String buffer = "body=" + UrlEncodeUtil.encode(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(body)));
//        return new ByteArrayInputStream(new HttpClient().post(DocApi + "/txt2pdf", buffer.getBytes()));
//    }


    @SneakyThrows
    private InputStream office2Pdf(InputStream body, String sourceName) {
        //转换为base64
        String buffer = "body=" + UrlEncodeUtil.encode(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(body)));
        return new ByteArrayInputStream(new HttpClient().post(DocApi + String.format("/%s2pdf", sourceName), buffer.getBytes()));
    }


    @SneakyThrows
    public Object saveFile(MultipartFile files, String device) {
        if (!PrinterTmpFile.exists()) {
            PrinterTmpFile.mkdirs();
        }

        final String topic = String.format("%s.%s", topicConf.getTopic(), device);
        String suffixName = FilenameUtils.getExtension(files.getOriginalFilename());


        final Set<String> wordExtName = Set.of("txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "png");


        //输入源
        @Cleanup InputStream inputStream = null;
        if ("pdf".equalsIgnoreCase(suffixName)) {
            inputStream = files.getInputStream();
        } else if (wordExtName.contains(suffixName.toLowerCase())) {
            inputStream = office2Pdf(files.getInputStream(), suffixName);
        }
        Assert.notNull(inputStream, "文件不符合格式要求");


        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".pdf";
        File saveFile = new File(PrinterTmpFile.getAbsolutePath() + "/" + fileName);


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
