package com.br.infnet.pb_barpesujo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PbBarPeSujoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PbBarPeSujoApplication.class, args);
    }

}
