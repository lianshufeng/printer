package com.github.printer.core.controller;

import com.github.printer.core.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("file")
public class FileController {
    @Autowired
    FileService fileService;

    @RequestMapping("upload")
    public Object uploadingFile(MultipartFile file, String device) {
        Assert.isTrue(!file.isEmpty(), "请选择文件");
        Assert.isTrue(!device.isEmpty(), "请选择打印机设备");
        return Map.of(
                "time", System.currentTimeMillis(),
                "text", fileService.saveFile(file, device)
        );


    }

    @GetMapping("download/{fileName}")
    public void download(HttpServletResponse httpServletResponse, @PathVariable("fileName") String fileName) {
        fileService.export(httpServletResponse, fileName);
    }


}
