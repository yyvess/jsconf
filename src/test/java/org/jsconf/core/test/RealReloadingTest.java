package org.jsconf.core.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.sample.bean.ConfigBean;
import org.junit.Assert;
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

	private static File createTempFile;

	@Autowired
	private ConfigBean conf;

	@Autowired
	private ConfigurationFactory factory;

	@BeforeClass
	public static void init() throws IOException, InterruptedException {
		String prefix = Long.toString(System.nanoTime());
		createTempFile = File.createTempFile(prefix, "_app.conf");
		createTempFile.deleteOnExit();
		write(TIC);
	}

	@Test
	@Repeat
	public void test() throws IOException, InterruptedException {
		final Object ref = this.conf;
		Assert.assertNotNull(this.conf);
		Assert.assertEquals(TIC, this.conf.getUrl());

		write(TAC);

		Assert.assertTrue(ref == this.conf);
		Assert.assertEquals(TAC, this.conf.getUrl());
	}

	private static void write(String value) throws IOException, InterruptedException {
		createTempFile.delete();
		createTempFile.createNewFile();
		try (FileWriter fw = new FileWriter(createTempFile)) {
			fw.append("simpleConf : {  url : \"" + value + "\" }");
		}
		Thread.sleep(1000);
	}

	@Configuration
	static class ContextConfiguration {
		@Bean
		public ConfigurationFactory configurationFactory() {
			return new ConfigurationFactory().withResourceName(createTempFile.getName())//
					.withBean("simpleConf", ConfigBean.class, null, true);
		}
	}
}
