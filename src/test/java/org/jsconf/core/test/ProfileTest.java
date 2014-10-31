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

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.sample.bean.ConfigBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ActiveProfiles("PROD")
@RunWith(SpringJUnit4ClassRunner.class)
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
        Assert.assertEquals("Tac", this.simpleConf.getUrl());
    }

    @Test
    public void testProfileConf() {
        Assert.assertEquals("Tac", this.simpleConf.getUrl());
    }

    @Configuration
    static class ContextConfiguration01 {
        @Bean(name = "context01")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_profile").withProfiles(true)
                    .withDefinition(true);
        }
    }

    @Configuration
    static class ContextConfiguration02 {
        @Bean(name = "context02")
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/test/app_profile.conf")
                    .withBean("simpleConf2", ConfigBean.class).withProfiles(true).withStrict(false).withFormat(null);
        }
    }
}
