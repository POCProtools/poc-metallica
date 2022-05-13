package fr.insee.metallica.pocprotools.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.node.ObjectNode;

import feign.FeignException;

@FeignClient(value = "send-mail", url = "${env.urls.send-mail}")
public interface SendMailClient {
    @PostMapping("/send-mail")
    ObjectNode sendMail(@RequestBody ObjectNode context) throws FeignException;
}
