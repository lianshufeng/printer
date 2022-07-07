package com.github.printer.core.config;

import com.github.microservice.components.activemq.config.MQConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import(MQConfig.class)
public class MQServerConfig {

}
