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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.test.bean.MyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ChildTest {

    @Autowired
    private MyConfig myConfig;

    @Autowired
    private MyConfig myConfigChild;

    @Autowired
    private MyConfig myConfig2;

    @Autowired
    private MyConfig myConfigChild2;

    @Test
    public void testChild() {
        assertNotSame(this.myConfig, this.myConfig2);
        assertNotSame(this.myConfigChild, this.myConfigChild2);
    }

    @Test
    public void testChild1() {
        assertEquals("Tic", this.myConfig.getValue());
        assertNotSame(this.myConfig, this.myConfig.getAChild());
        assertNotNull(this.myConfig.getAChild());
        assertEquals("Tac", this.myConfig.getAChild().getValue());
        assertSame(this.myConfigChild, this.myConfig.getAChild());
    }

    @Test
    public void testChild2() {
        assertEquals("Tic", this.myConfig2.getValue());
        assertNotSame(this.myConfig2, this.myConfig2.getAChild());
        assertNotNull(this.myConfig2.getAChild());
        assertEquals("Tac", this.myConfig2.getAChild().getValue());
        assertSame(this.myConfigChild2, this.myConfig2.getAChild());
    }

    @Configuration
    static class ContextConfiguration01 {
        @Bean(name = "context")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_child");
        }
    }

    @Configuration
    static class ContextConfiguration02 {
        @Bean(name = "context2")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_child2")
                    .withBean("myConfig", MyConfig.class, "myConfig2")
                    .withBean("myConfig/aChild", MyConfig.class, "myConfigChild2");
        }
    }
}
