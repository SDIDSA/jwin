package jwin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.junit.Test;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.param.deps.GradleResolver;
import org.luke.jwin.local.Downloader;
import org.luke.jwin.local.managers.GradleManager;

public class GradleTest {

	public File gradleRoot(String projName) {
		String gradlew = "/" + projName + "/gradlew.bat";
		String path = GradleTest.class.getResource(gradlew).getFile();
		File grad = new File(path);
		assertTrue(grad.exists());
		return grad.getParentFile();
	}

	public void testGradleVersionResolution(String projName, String expectedVersion) {
		String version = GradleResolver.getGradleVersion(gradleRoot(projName));
		assertEquals("Unexpected Gradle version for " + projName, expectedVersion, version);
	}

	/**
	 * Tests the Gradle version resolution for different project types.
	 */
	@Test
	public void testGradleVersionResolution() {
		testGradleVersionResolution("groovyGradFx", "gradle_5.0");
		testGradleVersionResolution("kotlinGradFx", "gradle_6.8");
		testGradleVersionResolution("noWrapperProps", null);
	}

	/**
	 * Tests Gradle distribution download
	 * verifying successful download and correct version identification.
	 */
	@Test
	public void testGradleDistributionDownload() {
		String url = GradleResolver.getDistributionUrl(gradleRoot("groovyGradFx"));
		assertEquals("https://services.gradle.org/distributions/gradle-5.0-bin.zip", url);

		Semaphore s = new Semaphore(0);

		File temp = new File("dist_test_" + new Random().nextInt(1000, 9999));
		temp.mkdir();
		Downloader.downloadZipInto(url, dp -> {
			System.out.println("downloading " + dp);
			if (dp >= 1.0) {
				s.release();
			}
		}, _ -> {

		}, temp, f -> GradleManager.versionFromDir(f).getRoot());
		s.acquireUninterruptibly();
		assertEquals("version of the downloaded distribution doesn't match the expected value",
				GradleManager.versionFromDir(temp).getVersion(), "gradle_5.0");
		JwinActions.deleteDir(temp);
	}

	@Test
	public void testTaskAddition() {
		// Create a sample project with a mock build.gradle
		// Call the method that adds the task
		// Use assertions to verify the modified build.gradle
	}

	@Test
	public void testTaskExecution() {
		// Create a sample project with the added task
		// Execute the task and capture its output
		// Use assertions to verify the output
	}

	@Test
	public void testDifferentProjectConfigurations() {
		// Create sample projects with different configurations
		// Execute your code for each project
		// Use assertions to verify correct handling
	}

	@Test
	public void testErrorHandling() {
		// Simulate error conditions
		// Call your code and use assertions to verify proper error handling
	}

}
