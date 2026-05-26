package com.darkyellowcat.catvaultbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("com.darkyellowcat.catvaultbackend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CatVaultBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatVaultBackendApplication.class, args);
    }

}
