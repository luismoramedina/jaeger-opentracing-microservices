package com.github.luismoramedina.books;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/books")
@Slf4j
public class BooksApplication {

    @Autowired
    RestTemplate restTemplate;

    @Value("${istio.security-context-header:sec-istio-auth-userinfo}")
    String secContextHeader;

    @Value("${stars.service.uri}")
    private String starsUrl;

    @Value("${covers.service.uri}")
    private String coversUrl;

    public static void main(String[] args) {
        SpringApplication.run(BooksApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Book> books(@RequestHeader HttpHeaders httpHeaders) {
        log.info("New request!");
        Map<String, String> headerMap = httpHeaders.toSingleValueMap();
        headerMap.keySet().forEach(key -> {
            String value = headerMap.get(key);
            log.info("header: " + key + "->" + value);
        });

        showSecurityContext(headerMap.get(secContextHeader));

        log.info("Before calling " + starsUrl);

        Star stars = restTemplate.getForObject(starsUrl, Star.class, 1);
        Cover[] covers = restTemplate.getForObject(coversUrl, Cover[].class, 1);

        ArrayList<Book> books = new ArrayList<>();
        Book endersGame = new Book();
        endersGame.id = 1;
        endersGame.author = "orson scott card";
        endersGame.title = "Enders game";
        endersGame.year = "1985";
        endersGame.stars = stars.number;
        endersGame.covers = Arrays.stream(covers).map(o -> o.url).toArray(String[]::new);

        books.add(endersGame);
        log.info("Sending response!");
        return books;
    }

    private void showSecurityContext(String securityContextString)  {
        if (!StringUtils.isEmpty(securityContextString)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String decoded = new String(java.util.Base64.getDecoder().decode(securityContextString));
                HashMap empMap = objectMapper.readValue(
                        decoded, HashMap.class);
                log.info("user in security context: {}", empMap.get("sub"));
            } catch (IOException e) {
                log.error("Error parsing user", e);
            }
        }
    }

    @RequestMapping(value = "/books-no-dep", method = RequestMethod.GET)
    public List<Book> booksNoDep(@RequestHeader HttpHeaders httpHeaders) {


        log.info("New request!");
        Map<String, String> headerMap = httpHeaders.toSingleValueMap();
        headerMap.keySet().forEach(key -> {
            String value = headerMap.get(key);
            log.info("header: " + key + "->" + value);
        });

        showSecurityContext(headerMap.get(secContextHeader));

        ArrayList<Book> books = new ArrayList<>();
        Book endersGame = new Book();
        endersGame.id = 1;
        endersGame.author = "orson scott card";
        endersGame.title = "Enders game";
        endersGame.year = "1985";
        endersGame.stars = 0;
        books.add(endersGame);
        log.info("Sending response!");

        return books;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Book newBook(@RequestBody Book book) {
        log.info("new book: " + book);
        return book;
    }
}
