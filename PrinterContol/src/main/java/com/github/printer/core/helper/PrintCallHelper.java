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

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
//        final String cmd = String.format("lpr -P %s %s", this.mqttConf.getDeviceName(), file.getAbsolutePath());
//        log.info("print : {}", cmd);
//        Runtime.getRuntime().exec(cmd);

        log.info("开始打印。。。。。。。。。。。。。。。。。。。。");
        PrinterJob job = getPrintServiceByName("x00000001");
        FileInputStream fileInputStream = new FileInputStream(file);
        SimpleDoc simpleDoc = new SimpleDoc(fileInputStream, DocFlavor.INPUT_STREAM.PNG, null);
        DocPrintJob printJob = job.getPrintService().createPrintJob();

        HashPrintRequestAttributeSet attribute = new HashPrintRequestAttributeSet();
        attribute.add(MediaSizeName.ISO_A4);
        printJob.print(simpleDoc,attribute);


    }


    @SneakyThrows
    private File downloadFile(String fileName) {
        String url = String.format("%s/file/download/%s", printerServerConf.getHostUrl(), fileName);
        log.info("download - {}", url);
        File file = new File(PrinterTmpFile.getAbsolutePath() + "/" + fileName);
        FileUtils.writeByteArrayToFile(file, new HttpClient().get(url));
        return file;
    }


    public PrinterJob getPrintServiceByName(String printerName) throws Exception {
        log.info("查找打印机：{}",printerName);
        PrinterJob job = PrinterJob.getPrinterJob();
        // 遍历查询打印机名称
        boolean flag = false;
        for (PrintService ps : PrinterJob.lookupPrintServices()) {

            String psName = ps.toString();
            // 选用指定打印机，需要精确查询打印机就用equals，模糊查询用contains
            if (psName.contains(printerName)) {
                flag = true;
                job.setPrintService(ps);
                break;
            }
        }
        if (!flag) {
            throw new RuntimeException("打印失败，未找到名称为" + printerName + "的打印机，请检查。");
        }
        return job;
    }



}
