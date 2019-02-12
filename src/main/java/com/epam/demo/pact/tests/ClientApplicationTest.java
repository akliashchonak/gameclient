package com.epam.demo.pact.tests;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "GameService", port = "8112")
@Slf4j
public class ClientApplicationTest {

    private Map<String, String> headers = MapUtils.putAll(new HashMap<>(), new String[] {
            "Content-Type", "application/json"
    });

    private Logger log = LoggerFactory.getLogger(ClientApplicationTest.class);

    @Pact(consumer = "pactdemoclient")
    public RequestResponsePact playGame(PactDslWithProvider builder) {

        DslPart gameResults = new PactDslJsonBody()
                .stringType("name", "777")
                .integerType("bet", 120)
                .integerType("win", 240) //expects win as an integer
                .stringType("message", "Congratulation! You won $240!")
                .asBody();

        return builder
                .given("I'm playing '777' game with the bet $120")
                .uponReceiving("A request for win for '777' game with the bet $120")
                .path("/game/777/120")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(gameResults)
                .toPact();
    }

    @Test
    public void testGame(MockServer mockServer) throws IOException {
        System.setProperty("pact.rootDir","../pacts");
        Request request = Request.Get(mockServer.getUrl() + "/game/777/120");
        log.info("Request: " + request);
        HttpResponse httpResponse = request.execute().returnResponse();
        log.info("Response: " + httpResponse);
        Assertions.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

}
