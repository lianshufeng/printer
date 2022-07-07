package com.github.printer.core.service;

import com.github.microservice.components.activemq.client.MQClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileService {
    @Value("${printer.topic}")
    private  String topic;

    private final String path = System.getProperty("user.dir") + File.separator + "file";

    @Autowired
    MQClient mq;
    public String saveFile(MultipartFile files) {

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        String OriginalFilename = files.getOriginalFilename();
        String suffixName = OriginalFilename.substring(OriginalFilename.lastIndexOf("."));

        String fileName= UUID.randomUUID() + suffixName;
        String filePath = path + File.separator +fileName;


        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            FileCopyUtils.copy(files.getInputStream(), outputStream);
        } catch (Exception e) {
            return "上传失败";
        }
        mq.sendObject(topic,fileName);
        return "打印中，请等待";


    }


    @SneakyThrows
    public void export(HttpServletResponse response, String fileName) {
        File file = new File(path + File.separator + fileName);
        Assert.isTrue(file.exists(),"文件不存在");
        FileInputStream fileInputStream = new FileInputStream(file);
//        response.setContentType("application/msexcel");
        fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
        response.addHeader("Content-Disposition", "filename=" + fileName);
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(fileInputStream.readAllBytes());
        outputStream.close();


    }


}
