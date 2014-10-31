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
import static org.junit.Assert.assertNotNull;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.test.bean.MyConfig;
import org.jsconf.core.test.bean.ServiceSpringBean;
import org.jsconf.core.test.bean.SimpleBeanSpring;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ConfigTest {

    @Autowired()
    @Qualifier(value = "test_2")
    private SimpleBeanSpring config;

    @Autowired()
    private ServiceSpringBean service;

    @Test
    public void testServiceSpringBean() {
        MyConfig localConfig = this.service.getConfig();
        assertNotNull(localConfig.getAChild());
        assertEquals("Hello World, I a spring bean!", this.service.getValue());
        assertEquals("Hello from Config", localConfig.getValue());
        assertEquals(10, localConfig.getAInt());
        assertNotNull(localConfig.getAChild());
        assertNotEquals(localConfig, localConfig.getAChild());
        assertEquals("Hello from child", localConfig.getAChild().getValue());
        assertEquals("Spring value", localConfig.getAChild().getSpringValue());
        assertEquals(5, localConfig.getAChild().getAInt());
        assertEquals(1, localConfig.getAMap().size());
        assertEquals("{word1=A map value}", localConfig.getAMap().toString());
    }

    @Test
    public void testSimpleBeanSpring() {
        assertEquals("I am a child of springOnConf!", this.config.getValue());
        assertEquals("Hello from child", this.config.getChildRef().getValue());
        assertEquals(this.service.getConfig().getAChild(), this.config.getChildRef());
    }

    @Configuration
    @ComponentScan(basePackageClasses = { ServiceSpringBean.class })
    static class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app").withDefinition(true);
        }
    }

}
