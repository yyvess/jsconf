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
import net.jmob.jsconf.core.test.bean.ConfigAnnotated;
import net.jmob.jsconf.core.test.bean.MyConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
class AnnotationTest {

    @Autowired
    private ConfigAnnotated conf;

    @Test
    void testMissingAnnotation() {
        assertThrows(BeanInitializationException.class, () ->
                new ConfigurationFactory()
                        .withResourceName("net/jmob/jsconf/core/test/app_annotated")
                        .withBean(MyConfig.class));
    }

    @Test
    void testAnnotated() {
        assertEquals("Test", this.conf.getValue());
    }

    @Configuration
    public static class ContextConfiguration {
        @Bean
        static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/test/app_annotated.conf")
                    .withScanPackage(AnnotationTest.class.getPackage().getName());
        }
    }
}
