package fr.insee.metallica.pocpasswordgenerator.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.insee.metallica.outbox.aspect.TransactionalCommand;
import fr.insee.metallica.pocpasswordgenerator.domain.PasswordHash;
import fr.insee.metallica.pocpasswordgenerator.networkmesser.NetworkMesser;
import fr.insee.metallica.pocpasswordgenerator.service.PasswordHashService;

@RestController
public class PasswordController {
	static public class UsernameDto {
		@NotEmpty
		private String username;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}
	
	static public class PasswordDto {
		private String password;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
	
	@Autowired
	private PasswordHashService passwordHashService;
	
	@PostMapping(path = "/generate-password")
	@NetworkMesser("controller")
	public PasswordDto generatePassword(@RequestBody @Valid UsernameDto dto) throws InterruptedException {
		var username = dto.getUsername();
		
		var password = new PasswordDto(); 
		password.setPassword(RandomStringUtils.randomAlphanumeric(25));
		try {
			passwordHashService.hashAndSave(username, password.getPassword());
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
		}
	
		return password;
	}
	
	@PostMapping(path = "/generate-password-with-command")
	@NetworkMesser("controller")
	@TransactionalCommand
	public PasswordDto generatePasswordWithCommand(@RequestBody @Valid UsernameDto dto) throws InterruptedException {
		var username = dto.getUsername();
		
		var password = new PasswordDto(); 
		password.setPassword(RandomStringUtils.randomAlphanumeric(25));
		try {
			passwordHashService.hashAndSave(username, password.getPassword());
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
		}
	
		return password;
	}
	
	@PostMapping(path = "/all")
	public List<PasswordHash> getAll() {
		return passwordHashService.findAll();
	}
	
	@PostMapping(path = "/test-future")
	public CompletableFuture<String> testFuture() {
		return new CompletableFuture<String>();
	}
}
