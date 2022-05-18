package fr.insee.metallica.pocprotools;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import feign.FeignException;
import fr.insee.metallica.pocprotools.controller.StartWorkflowController;

@SpringBootTest
//@Disabled
class IntegrationTests {
	@Autowired
	private ProtoolsClient protoolsClient;	
	
	@Autowired
	private IntegrationTestHelper integrationTestHelper;
	
	@Test
	void testNominal() throws Throwable {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("test-" + RandomStringUtils.randomAlphabetic(10));
		protoolsClient.startWorkflow(dto);
	
		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
	}

	@Test
	void testDoublon() throws IOException {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("test-" + RandomStringUtils.randomAlphabetic(10));

		protoolsClient.startWorkflow(dto);
		var exceptionCatched = false;
		try {
			protoolsClient.startWorkflow(dto);
		} catch (FeignException e) {
			exceptionCatched = true;
		}
	
		assert exceptionCatched;
	}

	@Test
	void testNoUsername() throws IOException {
		var dto = new StartWorkflowController.UsernameDto();
		var exceptionCatched = false;
		try {
			protoolsClient.startWorkflow(dto);
		} catch (FeignException e) {
			exceptionCatched = true;
		}
	
		assert exceptionCatched;
	}

}
