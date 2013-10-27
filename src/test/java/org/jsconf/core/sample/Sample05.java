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
package org.jsconf.core.sample;

import org.jsconf.core.ConfigurationFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jsconf/core/sample/applicationContext_05.xml" })
public class Sample05 {

	@Autowired
	private ConfInterface conf;

	@Autowired
	private ConfigurationFactory factory;

	@Test
	@Repeat(value = 4)
	public void test() {
		final Object ref = this.conf;
		Assert.assertNotNull(this.conf);
		Assert.assertEquals("Tic", this.conf.getVstring());
		this.factory.setConfName("org/jsconf/core/sample/conf_05_2");
		this.factory.reload();
		Assert.assertTrue(ref == this.conf);
		Assert.assertEquals("Tac", this.conf.getVstring());
		this.factory.setConfName("org/jsconf/core/sample/conf_05");
		this.factory.reload();
	}
}