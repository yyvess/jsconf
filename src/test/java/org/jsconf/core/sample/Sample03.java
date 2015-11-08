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
 */

package org.jsconf.core.sample;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.sample.bean.ConfigBean;
import org.jsconf.core.sample.bean.SpringBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class Sample03 {

    @Autowired
    @Qualifier("node")
    private SpringBean node1;

    @Autowired
    @Qualifier("Node_02")
    private SpringBean node2;

    @Test
    public void test() {
        assertNotNull(this.node1);
        assertNotNull(this.node2);
        assertNotSame(this.node1, this.node2);
        assertEquals("Yves", this.node1.getName());
        assertEquals("Joe", this.node2.getName());
        assertNotNull(this.node1.getChild());
        assertNotNull(this.node2.getChild());
        assertNotSame(this.node1.getChild(), this.node2.getChild());
        assertEquals(15, this.node1.getChild().getPort());
        assertEquals(25, this.node2.getChild().getPort());
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("org/jsconf/core/sample/app_03.conf")
                    .withBean("node", SpringBean.class)
                    .withBean("node/child", ConfigBean.class)
                    .withBean("node_2", SpringBean.class, "Node_02")
                    .withBean("node_2/child", ConfigBean.class);
        }
    }
}
