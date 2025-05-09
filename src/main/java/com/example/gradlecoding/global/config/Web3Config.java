package com.example.gradlecoding.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3Config {

    @Value("${web3.rpc-url}")
    private String rpcUrl;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Value("${wallet.private-key}")
    private String privateKey;

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

}

