package com.czq.apiclientsdk;

import com.czq.apiclientsdk.client.DayApiClient;
import com.czq.apiclientsdk.client.NameApiClient;
import com.czq.apiclientsdk.client.RandomApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("czq.api")
@ComponentScan
@Data
public class HeartApiClientAutoConfiguration {

    private String accessKey;
    private String secretKey;


    @Bean
    public NameApiClient czqClient(){
        return new NameApiClient(accessKey,secretKey);
    }

    @Bean
    public DayApiClient dayClient(){
        return new DayApiClient(accessKey,secretKey);
    }

    @Bean
    public RandomApiClient randomClient(){
        return new RandomApiClient(accessKey,secretKey);
    }
}
