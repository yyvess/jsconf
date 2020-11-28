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

package net.jmob.jsconf.core.test;

import net.jmob.jsconf.core.ConfigurationFactory;
import net.jmob.jsconf.core.test.bean.MyConfig;
import net.jmob.jsconf.core.test.bean.ServiceSpringBean;
import net.jmob.jsconf.core.test.bean.SimpleBeanSpring;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
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
        assertNotNull(localConfig.getChild());
        assertEquals("Hello World, I a spring bean!", this.service.getValue());
        assertEquals("Hello from Config", localConfig.getValue());
        assertEquals(10, localConfig.getInteger());
        assertNotNull(localConfig.getChild());
        assertNotEquals(localConfig, localConfig.getChild());
        assertEquals("Hello from child", localConfig.getChild().getValue());
        assertEquals("Spring value", localConfig.getChild().getSpringValue());
        assertEquals(5, localConfig.getChild().getInteger());
        assertEquals(1, localConfig.getMap().size());
        assertEquals("{word1=A map value}", localConfig.getMap().toString());
    }

    @Test
    public void testSimpleBeanSpring() {
        assertEquals("I am a child of springOnConf!", this.config.getValue());
        assertEquals("Hello from child", this.config.getChildRef().getValue());
        assertEquals(this.service.getConfig().getChild(), this.config.getChildRef());
    }

    @Configuration
    @ComponentScan(basePackageClasses = {ServiceSpringBean.class})
    static class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/test/app")
                    .withBean("test", MyConfig.class)
                    .withBean("test/child", MyConfig.class, "test-child")
                    .withBean("test_2", SimpleBeanSpring.class);
        }
    }

}
