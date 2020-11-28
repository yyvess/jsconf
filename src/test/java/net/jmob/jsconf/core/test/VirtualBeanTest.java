/**
 * Copyright 2013 Yves Galante
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jmob.jsconf.core.test;

import net.jmob.jsconf.core.ConfigurationFactory;
import net.jmob.jsconf.core.impl.VirtualBean;
import net.jmob.jsconf.core.test.bean.ConfigByInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.access.InvocationFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class VirtualBeanTest {

    @Autowired
    @Qualifier("simpleConf")
    private ConfigByInterface conf;
    @Autowired
    @Qualifier("simpleConf2")
    private ConfigByInterface conf2;
    @Autowired
    @Qualifier("simpleConf3")
    private ConfigByInterface conf3;

    @Test
    public void testHashCode() {
        assertEquals(-1831264279, this.conf.hashCode());
        assertEquals(this.conf.hashCode(), this.conf2.hashCode());
        assertEquals(-1828017501, this.conf3.hashCode());
        assertEquals(new VirtualBean(null).hashCode(), new VirtualBean(null).hashCode());
    }

    @Test
    public void testEquals() {
        assertNotEquals(this.conf, "");
        assertNotEquals(this.conf, null);
        assertEquals(this.conf, this.conf);
        assertEquals(new VirtualBean(null), new VirtualBean(null));
        assertEquals(new VirtualBean(new HashMap<String, Object>()), new VirtualBean(new HashMap<String, Object>()));
        assertNotEquals(this.conf, new VirtualBean(null));
        assertNotEquals(new VirtualBean(null), this.conf);
        assertNotSame(this.conf, this.conf2);
        assertEquals(this.conf, this.conf2);
        assertNotEquals(this.conf, this.conf3);
        assertNotEquals(this.conf, this.conf3);
    }

    @Test
    public void testToString() {
        assertEquals("{getUrl=https://localhost/Tic}", this.conf.toString());
    }

    @Test
    public void testGet() {
        assertEquals("https://localhost/Tic", this.conf.getUrl());
        assertEquals("xxx", this.conf3.getX());
    }

    @Test
    public void testSet() {
        assertThrows(InvocationFailureException.class, () -> this.conf3.setX("yyy"));
    }

    @Test
    public void validationSuccess() {
        new AnnotationConfigApplicationContext(ContextConfiguration.class)
                .getBean("simpleConf");
    }

    @Test
    public void validationFailed() {
        assertThrows(BeanCreationException.class, () ->
                new AnnotationConfigApplicationContext(ContextConfigurationValidationFailed.class)
                        .getBean("simpleConf"));
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/test/app_virtual")
                    .withBean("simpleConf", ConfigByInterface.class)
                    .withBean("simpleConf2", ConfigByInterface.class)
                    .withBean("simpleConf3", ConfigByInterface.class);
        }
    }

    static class ContextConfigurationValidationFailed {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/test/app_virtual_failed")
                    .withBean("simpleConf", ConfigByInterface.class);
        }
    }
}
