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
	public void test() throws IOException, InterruptedException {
		final Object ref = this.conf;
		Assert.assertNotNull(this.conf);
		
		Assert.assertEquals(null, this.conf.getUrl());
		write(TIC);
		Assert.assertEquals(TIC, this.conf.getUrl());
		write(TAC);
		Assert.assertEquals(TAC, this.conf.getUrl());
		
		Assert.assertTrue(ref == this.conf);
	}

	private static void write(String value) throws IOException, InterruptedException {
		try (FileWriter fw = new FileWriter(tempFile)) {
			fw.append("simpleConf : {  url : \"" + value + "\" }");
		}
		Thread.sleep(1000);
	}

	@Configuration
	static class ContextConfiguration {
		@Bean
		public ConfigurationFactory configurationFactory() {
			return new ConfigurationFactory().withResourceName(tempFile.getName())//
					.withBean("simpleConf", ConfigBean.class, null, true);
		}
	}
}
