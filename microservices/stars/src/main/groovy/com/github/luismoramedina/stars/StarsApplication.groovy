package com.github.luismoramedina.stars

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.opentracing.ScopeManager
import io.opentracing.Span
import io.opentracing.SpanContext
import io.opentracing.Tracer
import io.opentracing.noop.NoopTracer
import io.opentracing.propagation.Format
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
@Slf4j
class StarsApplication {

    static void main(String[] args) {
        SpringApplication.run StarsApplication, args
    }

    @RequestMapping(value = "/stars/{id}", method = RequestMethod.GET)
    Star star(@PathVariable Integer id, @RequestHeader HttpHeaders httpHeaders) {

        log.info "New request!"
        httpHeaders.each { k, v -> log.info "header: ${k}  ->  ${v}" }
        showSecurityContext(httpHeaders.toSingleValueMap()["sec-istio-auth-userinfo"])
        def star = new Star()
        star.number = 5
        star.id = id
        log.info "Sending response!"
        star
    }

    private void showSecurityContext(String securityContextString) {
        if (securityContextString?.trim()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper()
                String decoded = new String(Base64.getDecoder().decode(securityContextString))
                HashMap empMap = objectMapper.readValue(
                        decoded, HashMap.class)
                log.info("user in security context: {}", empMap.get("sub"))
            } catch (IOException e) {
                log.error("Error parsing user", e)
            }
        }
    }

}
