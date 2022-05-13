package fr.insee.metallica.pocprotools.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.node.ObjectNode;

import feign.FeignException;

@FeignClient(value = "password-generator", url = "${command.services.password-generator}")
public interface PasswordGenerateClient {
    @PostMapping(value = "/generate-password")
    ObjectNode generatePassword(@RequestBody ObjectNode context) throws FeignException;
}
