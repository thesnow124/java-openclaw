package com.openclawlite;

import com.openclawlite.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
// 启动 OpenClaw Lite 控制台应用。
public class Application {
  // 以配置好的 Spring 上下文启动应用。
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
