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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.impl.VirtualBean;
import org.jsconf.core.test.bean.ConfigBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.access.InvocationFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class VirtualBeanTest {

	@Autowired
	@Qualifier("simpleConf")
	private ConfigBean conf;
	@Autowired
	@Qualifier("simpleConf2")
	private ConfigBean conf2;
	@Autowired
	@Qualifier("simpleConf3")
	private ConfigBean conf3;

	@Test
	public void testHashCode() {
		assertEquals(1445101388, this.conf.hashCode());
		assertEquals(this.conf.hashCode(), this.conf2.hashCode());
		assertEquals(1448348166, this.conf3.hashCode());
		assertEquals(new VirtualBean(null).hashCode(), new VirtualBean(null).hashCode());
	}

	@Test
	public void testEquals() {
		assertNotEquals(this.conf, new String());
		assertNotEquals(this.conf, null);
		assertEquals(this.conf, this.conf);
		assertEquals(new VirtualBean(null), new VirtualBean(null));
		assertEquals(new VirtualBean(new HashMap<String, Object>()), new VirtualBean(new HashMap<String, Object>()));
		assertNotEquals(this.conf, new VirtualBean(null));
		assertNotEquals(new VirtualBean(null), this.conf);
		assertNotSame(this.conf, this.conf2);
		assertNotEquals(this.conf, this.conf3);
		assertEquals(this.conf, this.conf2);
		assertNotEquals(this.conf, this.conf3);
	}

	@Test
	public void testToString() {
		assertEquals("{getUrl=https://localhost/Tic, get@Interface=org.jsconf.core.test.bean.ConfigBean}",
				this.conf.toString());
	}

	@Test
	public void testGet() {
		assertEquals("https://localhost/Tic", this.conf.getUrl());
		assertEquals("xxx", this.conf3.getX());
	}

	@Test(expected = InvocationFailureException.class)
	public void testSet() {
		this.conf3.setX("yyy");
	}

	@Configuration
	static class ContextConfiguration {
		@Bean
		public static ConfigurationFactory configurationFactory() {
			return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_virtual");
		}
	}
}
