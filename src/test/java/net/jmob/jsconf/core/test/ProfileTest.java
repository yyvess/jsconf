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
import net.jmob.jsconf.core.sample.bean.ConfigBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("PROD")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ProfileTest {

    @Autowired
    private ConfigBean simpleConf;

    @Autowired
    private ConfigBean simpleConf2;

    @Autowired
    private ConfigurationFactory context01;

    @Autowired
    private ConfigurationFactory context02;

    @Test
    public void testProfile() {
        assertEquals("Tac", this.simpleConf.getUrl());
    }

    @Test
    public void testProfileConf() {
        assertEquals("Tac", this.simpleConf2.getUrl());
    }

    @Configuration
    static class ContextConfiguration01 {
        @Bean(name = "context01")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/test/app_profile")
                    .withBean("simpleConf", ConfigBean.class)
                    .withBean("simpleConf", ConfigBean.class, "simpleConf2")
                    .useProfiles();
        }
    }

}
