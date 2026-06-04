package com.supplysense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class EventProcessorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventProcessorServiceApplication.class, args);
    }
}
