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

package org.jsconf.core.sample;

import org.jsconf.core.ConfigurationFactory;
import org.jsconf.core.sample.bean.ConfigBean;
import org.jsconf.core.sample.bean.RootConfigBean;
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
public class Sample08 {

    @Autowired
    private RootConfigBean myConfig;

    @Autowired
    private ConfigBean myConfigChild;

    @Test
    public void testChild() {
        assertEquals("Tic", this.myConfig.getValue());
        assertNotNull(this.myConfig.getChild());
        assertEquals("https://localhost/Tic", this.myConfig.getChild().getUrl());
        assertSame(this.myConfigChild, this.myConfig.getChild());
    }

    @Configuration
    static class ContextConfiguration01 {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("org/jsconf/core/sample/app_08")
                    .withBean("myRoot", RootConfigBean.class)
                    .withBean("myRoot/child", ConfigBean.class);
        }
    }
}
