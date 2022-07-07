package com.github.printer.core.controller;

import com.github.printer.core.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("file")
public class FileController {
    @Autowired
    FileService fileService;
    @RequestMapping("uploading")
    public String uploadingFile(MultipartFile file,String device){
        Assert.isTrue(!file.isEmpty(),"请选择文件");
        Assert.isTrue(!device.isEmpty(),"请选择打印机设备");
        return fileService.saveFile(file,device);


    }

    @GetMapping("export")
    public ResponseEntity<InputStreamResource> export(HttpServletResponse httpServletResponse,String fileName){

        fileService.export(httpServletResponse,fileName);
        return null;
    }



}
