/**
 * Copyright 2013 Yves Galante
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jsconf.core.test;

import java.net.URISyntaxException;

import org.jsconf.core.ConfigurationFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jsconf/core/test/applicationContext.xml" })
public class ConfigTest {

	@Autowired()
	@Qualifier(value = "test")
	private MyConfigInterface v;

	@Autowired()
	@Qualifier(value = "serviceSpring")
	private ServiceSpringBean service;

	@Autowired()
	private ConfigurationFactory beanFactory;

	@Test
	@Repeat(value = 10)
	public void test() throws URISyntaxException, InterruptedException {
		this.beanFactory.setResourceName("org/jsconf/core/test/app");
		this.beanFactory.reload();
		testLoad();
		this.beanFactory.setResourceName("org/jsconf/core/test/app_reload");
		this.beanFactory.reload();
		testLoadReload();
	}

	public void testLoad() {
		Assert.assertEquals("Hello World, I a spring bean!", this.service.getValue());
		Assert.assertEquals("Hello", this.v.getValue());
		Assert.assertEquals("Hello", this.v.getValue());
		Assert.assertEquals(10, this.v.getAInt());
		Assert.assertNotNull(this.v.getAChild());
		Assert.assertNotEquals(this.v, this.v.getAChild());
		Assert.assertEquals(5, this.v.getAChild().getAInt());

		Assert.assertEquals("Hello World, I a spring bean!", this.v.getValueSpring());
		Assert.assertEquals("I am a child of springOnConf!", this.v.getValueSpringConfigured());

		Assert.assertEquals("World", this.v.getAChild().getValue());
		Assert.assertEquals("World", this.v.getAChild().getValue());
		Assert.assertEquals("World", this.service.getChild().getAChild().getValue());
		Assert.assertEquals("Hello", this.service.getChild().getValue());
		// Same instance
		Assert.assertTrue(this.v == this.service.getChild());

		Assert.assertEquals(1, this.v.getAMap().size());
		Assert.assertEquals("{word1=Hello}", this.v.getAMap().toString());
	}

	public void testLoadReload() throws URISyntaxException, InterruptedException {
		Assert.assertEquals("Hello World, I a spring bean!", this.service.getValue());
		Assert.assertEquals("Hello 2", this.v.getValue());
		Assert.assertEquals(10, this.v.getAInt());
		Assert.assertNotNull(this.v.getAChild());
		Assert.assertNotEquals(this.v, this.v.getAChild());
		Assert.assertEquals(12, this.v.getAChild().getAInt());

		Assert.assertEquals("Hello World, I a spring bean!", this.v.getValueSpring());
		Assert.assertEquals("I am a child of springOnConf version 2!", this.v.getValueSpringConfigured());

		Assert.assertEquals("Hello 2", this.service.getChild().getValue());
		Assert.assertEquals("World 2", this.service.getChild().getAChild().getValue());
		Assert.assertEquals("World 2", this.v.getAChild().getValue());
		Assert.assertEquals("World 2", this.v.getAChild().getValue());
		// Same instance
		Assert.assertTrue(this.v == this.service.getChild());

		Assert.assertEquals(2, this.v.getAMap().size());
		Assert.assertEquals("{word1=Hello, word2=World}", this.v.getAMap().toString());
	}

}
