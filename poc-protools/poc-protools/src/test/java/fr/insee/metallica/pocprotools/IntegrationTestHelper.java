package fr.insee.metallica.pocprotools;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class IntegrationTestHelper {
	@Value("${files.mail-sender-files}")
	private String mailLog;
	
	public void waitForUserInMail(int nbSeconds, String user) throws Throwable {
		waitFor(nbSeconds, () -> matchAll(Set.of(user)));
	}

	public void waitForUsersInMail(int nbSeconds, Collection<String> users) throws Throwable {
		waitFor(nbSeconds, () -> matchAll(users));
	}

	private boolean matchAll(Collection<String> users) throws IOException {
		for (var username : users) {
			var count = StringUtils.countMatches(FileUtils.readFileToString(new File(this.mailLog)), username);
			assert count < 2;
			if (count != 1) {
				return false;
			}
		}
		return true;
	}

	public static interface TestOnce {
		boolean test() throws Throwable;
	}
	
	public void waitFor(int nbSeconds, TestOnce test) throws Throwable {
		for(int i = 0; i < nbSeconds; i++) {
			if (test.test()) return;
			Thread.sleep(1000);
		}
		assert false;
	}
}
