package com.example.daria;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@SpringBootApplication
public class DariaApplication {
    private static final String API_URL = "http://91.241.64.178:7081/api/users";

    public static void main(String[] args) {
        SpringApplication.run(DariaApplication.class, args);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<User[]> responseEntity = restTemplate.getForEntity(API_URL, User[].class);

        log.debug("1. Get users");
        log.debug("Response status: {}", responseEntity.getStatusCode());
        log.debug("Users list: {}", Arrays.asList(responseEntity.getBody()));
        log.debug("Response headers: {}", responseEntity.getHeaders());
        log.debug("");

        String sessionId = Optional.ofNullable(responseEntity.getHeaders().get("Set-Cookie"))
                .map(list -> list.get(0))
                .orElseThrow(() -> new RuntimeException("Not received session id!"));

        log.debug("Session id: {}", sessionId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", sessionId);

        User userToBeCreated = User.builder()
                .id(3)
                .name("James")
                .lastName("Brown")
                .age(ThreadLocalRandom.current().nextInt(1, 100))
                .build();
        HttpEntity<User> createUserRequestBody = new HttpEntity<>(userToBeCreated, headers);
        ResponseEntity<String> response2 = restTemplate.postForEntity(API_URL, createUserRequestBody, String.class);

        log.debug("2. Create user");
        log.debug("Response status: {}", response2.getStatusCode());
        log.debug("Response body: {}", response2.getBody());
        log.debug("");

        String answer = response2.getBody();

        User updateUser = userToBeCreated;
        updateUser.setName("Thomas");
        updateUser.setLastName("Shelby");
        HttpEntity<User> updateUserRequestBody = new HttpEntity<>(updateUser, headers);

        ResponseEntity<String> response3 =
                restTemplate.exchange(API_URL, HttpMethod.PUT, updateUserRequestBody, String.class);
        log.debug("3. Update user");
        log.debug("Response status: {}", response3.getStatusCode());
        log.debug("Response body: {}", response3.getBody());
        log.debug("");

        answer += response3.getBody();

        HttpEntity<Void> delete = new HttpEntity<>(headers);
        ResponseEntity<String> response4 =
                restTemplate.exchange(API_URL + "/3", HttpMethod.DELETE, delete, String.class);
        log.debug("4. Delete user");
        log.debug("Response status: {}", response4.getStatusCode());
        log.debug("Response body: {}", response4.getBody());
        log.debug("");

        answer += response4.getBody();

        log.info("Answer: {}", answer);
    }

}
