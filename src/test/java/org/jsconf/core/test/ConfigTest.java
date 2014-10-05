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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jsconf/core/test/applicationContext.xml" })
public class ConfigTest {

	@Autowired()
	@Qualifier(value = "test")
	private MyConfigInterface v;

	@Autowired()
	@Qualifier(value = "standardSpringBean")
	private SpringBean springBean;

	@Autowired()
	private ConfigurationFactory beanFactory;

	@Test
	public void test() throws URISyntaxException, InterruptedException {
		testLoad();
		testLoadReload();
	}

	public void testLoad() {

		Assert.assertEquals("Hello World, I a spring bean!", this.springBean.getValue());
		Assert.assertEquals("Hello", this.v.getValue());
		Assert.assertEquals("Hello", this.v.getValue());
		Assert.assertEquals(10, this.v.getAInt());
		Assert.assertNotNull(this.v.getAChild());
		Assert.assertNotEquals(this.v, this.v.getAChild());
		Assert.assertEquals(5, this.v.getAChild().getAInt());

		Assert.assertEquals("Hello World, I a spring bean!", this.v.getValueSpring());
		Assert.assertEquals("Hello World, I am your father !", this.v.getValueSpringConfigured());

		Assert.assertEquals("World", this.v.getAChild().getValue());
		Assert.assertEquals("World", this.v.getAChild().getValue());
		Assert.assertEquals("World", this.springBean.getChild().getAChild().getValue());
		Assert.assertEquals("Hello", this.springBean.getChild().getValue());
		// Same instance
		Assert.assertTrue(this.v == this.springBean.getChild());

		Assert.assertEquals(3, this.v.getAMap().size());
		Assert.assertEquals("{word1=Hello, word2=from, word3=map}", this.v.getAMap().toString());

		System.out.println(this.v.getValue());
		System.out.println(this.v.getAChild().getValue());
		System.out.println(this.springBean.getValue());
	}

	public void testLoadReload() throws URISyntaxException, InterruptedException {
		this.beanFactory.setConfiguration("org/jsconf/core/test/app_reload");
		this.beanFactory.reload();
		Assert.assertEquals("Hello World, I a spring bean!", this.springBean.getValue());
		Assert.assertEquals("Hello", this.v.getValue());
		Assert.assertEquals(10, this.v.getAInt());
		Assert.assertNotNull(this.v.getAChild());
		Assert.assertNotEquals(this.v, this.v.getAChild());
		Assert.assertEquals(5, this.v.getAChild().getAInt());

		Assert.assertEquals("Hello World, I a spring bean!", this.v.getValueSpring());
		Assert.assertEquals("Hello World, I am your father 2!", this.v.getValueSpringConfigured());

		Assert.assertEquals("World 2", this.v.getAChild().getValue());
		Assert.assertEquals("World 2", this.v.getAChild().getValue());
		Assert.assertEquals("World 2", this.springBean.getChild().getAChild().getValue());
		Assert.assertEquals("Hello", this.springBean.getChild().getValue());
		// Same instance
		Assert.assertTrue(this.v == this.springBean.getChild());

		Assert.assertEquals(3, this.v.getAMap().size());
		Assert.assertEquals("{word1=Hello, word2=from, word3=map 2}", this.v.getAMap().toString());

		System.out.println(this.v.getValue());
		System.out.println(this.v.getAChild().getValue());
		System.out.println(this.springBean.getValue());
	}

}
