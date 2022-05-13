package fr.insee.metallica.mock;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockTestController {
	static public class TestBody {
		public String value;

		public TestBody() {
		}
		
		public TestBody(String value) {
			this.value = value;
		}
	}
	
	@PostMapping(path = "/test-post")
	public TestBody generatePassword(@RequestBody TestBody dto) throws InterruptedException {
		dto.value = dto.value + "done";
		return dto;
	}
}
