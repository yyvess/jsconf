package org.jsconf.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.sample.bean.ConfigBean;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

//Required to put ${java.io.tmpdir} on classpath
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RealReloadingTest {

	private static final String TIC = "Tic";
	private static final String TAC = "Tac";

	private static File tempFile;

	@Autowired
	private ConfigBean conf;

	@Autowired
	private ConfigurationFactory factory;

	@BeforeClass
	public static void init() throws IOException {
		String prefix = Long.toString(System.nanoTime());
		tempFile = File.createTempFile(prefix, "_app.conf");
		tempFile.deleteOnExit();
	}

	@Test
	@Repeat
	public void testRealReloading() throws IOException, InterruptedException {
		final Object ref = this.conf;
		assertNotNull(this.conf);

		assertEquals(null, this.conf.getUrl());
		write(TIC);
		assertEquals(TIC, this.conf.getUrl());
		write(TAC);
		assertEquals(TAC, this.conf.getUrl());
		write(TIC);
		assertEquals(TIC, this.conf.getUrl());

		assertTrue(ref == this.conf);
	}

	private void write(String value) throws IOException, InterruptedException {
		try (FileWriter fw = new FileWriter(tempFile)) {
			fw.append("simpleConf : {  url : \"" + value + "\" }");
		}
		Thread.sleep(1000);
	}

	@Configuration
	static class ContextConfiguration {
		@Bean
		public static ConfigurationFactory configurationFactory() {
			return new ConfigurationFactory().withResourceName(tempFile.getName())//
					.withBean("simpleConf", ConfigBean.class, "myBeanId", true);
		}
	}
}
