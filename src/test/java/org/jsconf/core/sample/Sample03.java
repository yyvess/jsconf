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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jsconf/core/sample/applicationContext_03.xml" })
public class Sample03 {

	@Autowired
	@Qualifier("node")
	private NodeObject node1;

	@Autowired
	@Qualifier("Node_02")
	private NodeObject node2;

	@Test
	public void test() {
		Assert.assertNotNull(this.node1);
		Assert.assertNotNull(this.node2);
		Assert.assertNotEquals(this.node1, this.node2);
		Assert.assertEquals("yves", this.node1.getName());
		Assert.assertEquals("Joe", this.node2.getName());
		Assert.assertNotNull(this.node1.getChild());
		Assert.assertNotNull(this.node2.getChild());
		Assert.assertNotEquals(this.node1.getChild(), this.node2.getChild());
		Assert.assertEquals(15, this.node1.getChild().getVint());
		Assert.assertEquals(25, this.node2.getChild().getVint());
	}
}
