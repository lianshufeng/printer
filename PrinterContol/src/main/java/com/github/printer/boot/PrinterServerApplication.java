package com.github.printer.boot;

import com.github.microservice.core.boot.ApplicationBootSuper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("com.github.printer.core")
public class PrinterServerApplication extends ApplicationBootSuper {

    public static void main(String[] args) {
        SpringApplication.run(PrinterServerApplication.class, args);
    }

}
