package com.example.backend.application.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Arrays;
import java.util.Base64;

@TestConfiguration
public class PSPTestWebClientConfiguration {

    @Value( "${PSP.toss.url}")
    private String baseUrl;

    @Value( "${PSP.toss.secretKey}")
    private String secretKey;

    public WebClient createTossPaymentWebClient(Pair<String, String> customHeaderKeyValue) {
        String encodedSecretKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", encodedSecretKey))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(customHeaderKeyValue.getFirst(), customHeaderKeyValue.getSecond())
                .clientConnector(reactorClientHttpConnector())
                .codecs(ClientCodecConfigurer::defaultCodecs)
                .build();
    }

    private ClientHttpConnector reactorClientHttpConnector() {
        ConnectionProvider provider = ConnectionProvider
                .builder("toss-payment")
                .build();

        return new ReactorClientHttpConnector(HttpClient.create(provider));
    }
}
