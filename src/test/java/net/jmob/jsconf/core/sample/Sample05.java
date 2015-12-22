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

package net.jmob.jsconf.core.sample;

import net.jmob.jsconf.core.ConfigurationFactory;
import net.jmob.jsconf.core.sample.bean.ConfigBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class Sample05 {

    @Autowired
    private ConfigBean conf;

    @Autowired
    private ConfigurationFactory factory;

    @Test
    @Repeat(value = 4)
    public void test() {
        final Object ref = this.conf;
        assertNotNull(this.conf);
        assertEquals("https://localhost/Tic", this.conf.getUrl());
        // Simulates configuration file change
        this.factory.withResourceName("net/jmob/jsconf/core/sample/app_05_2").reload();

        assertTrue(ref == this.conf);
        assertEquals("https://localhost/Tac", this.conf.getUrl());
        this.factory.withResourceName("net/jmob/jsconf/core/sample/app_05").reload();
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/sample/app_05")
                    .withBean("simpleConf", ConfigBean.class, true);
        }
    }
}
