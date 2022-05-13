package fr.insee.metallica.pocprotools;

import java.util.HashSet;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.insee.metallica.pocprotools.controller.StartWorkflowController;

@SpringBootTest
@Disabled
class IntegrationToMakeWorkTests {
	@Autowired
	private ProtoolsClient protoolsClient;	
	
	@Autowired
	private IntegrationTestHelper integrationTestHelper;
	
	@Test
	void testTimeoutPretransaction() throws Throwable {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("timeout-pretransaction-" + RandomStringUtils.randomAlphabetic(10));
		protoolsClient.startWorkflow(dto);

		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
		// check for duplicate
		Thread.sleep(10000);
		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
	}

	@Test
	void testTimeoutTransaction() throws Throwable {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("timeout-transaction-" + RandomStringUtils.randomAlphabetic(10));
		protoolsClient.startWorkflow(dto);

		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
		// check for duplicate
		Thread.sleep(10000);
		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
	}

	@Test
	void testTimeoutPostTransaction() throws Throwable {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("timeout-posttransaction-" + RandomStringUtils.randomAlphabetic(10));
		protoolsClient.startWorkflow(dto);

		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
		// check for duplicate
		Thread.sleep(10000);
		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
	}

	@Test
	void testUnavailableOnce() throws Throwable {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("unavailable-" + RandomStringUtils.randomAlphabetic(10));
		protoolsClient.startWorkflow(dto);

		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
		// check for duplicate
		Thread.sleep(10000);
		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
	}
	
	@Test
	void testUnknownHostOnce() throws Throwable {
		var dto = new StartWorkflowController.UsernameDto();
		dto.setUsername("unknownhost-" + RandomStringUtils.randomAlphabetic(10));
		protoolsClient.startWorkflow(dto);

		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
		// check for duplicate
		Thread.sleep(10000);
		integrationTestHelper.waitForUserInMail(10, dto.getUsername());
	}
	
	@Test
	void testCharge() throws Throwable {
		var users = new HashSet<String>();
		for (int i = 0 ; i < 1000; i++) {
			var dto = new StartWorkflowController.UsernameDto();
			dto.setUsername("charge-" + RandomStringUtils.randomAlphabetic(10));
			users.add(dto.getUsername());
			new Thread(() -> protoolsClient.startWorkflowAsync(dto)).start();
			
			if (i % 10 == 0)
				Thread.sleep(10);
		}
		integrationTestHelper.waitForUsersInMail(60, users);
		// check for duplicate
		Thread.sleep(10000);
		integrationTestHelper.waitForUsersInMail(10, users);
	}
}
