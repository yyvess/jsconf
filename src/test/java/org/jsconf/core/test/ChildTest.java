/**
 * Copyright 2013 Yves Galante
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsconf.core.test;

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

import static org.junit.Assert.*;

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
        assertNotSame(this.myConfig, this.myConfig.getChild());
        assertNotNull(this.myConfig.getChild());
        assertEquals("Tac", this.myConfig.getChild().getValue());
        assertSame(this.myConfigChild, this.myConfig.getChild());
    }

    @Test
    public void testChild2() {
        assertEquals("Tic", this.myConfig2.getValue());
        assertNotSame(this.myConfig2, this.myConfig2.getChild());
        assertNotNull(this.myConfig2.getChild());
        assertEquals("Tac", this.myConfig2.getChild().getValue());
        assertSame(this.myConfigChild2, this.myConfig2.getChild());
    }

    @Configuration
    static class ContextConfiguration01 {
        @Bean(name = "context")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_child")
                    .withBean("myConfig", MyConfig.class)
                    .withBean("myConfig/child", MyConfig.class, "myConfigChild");
        }
    }

    @Configuration
    static class ContextConfiguration02 {
        @Bean(name = "context2")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_child2")
                    .withBean("myConfig", MyConfig.class, "myConfig2")
                    .withBean("myConfig/child", MyConfig.class, "myConfigChild2");
        }
    }
}
