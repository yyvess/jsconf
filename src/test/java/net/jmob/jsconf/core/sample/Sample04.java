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

package net.jmob.jsconf.core.sample;

import net.jmob.jsconf.core.ConfigFormat;
import net.jmob.jsconf.core.ConfigurationFactory;
import net.jmob.jsconf.core.sample.bean.Sequence;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
class Sample04 {

    @Autowired
    private Sequence sequence;

    @Test
    void test() {
        assertNotNull(this.sequence);
        assertEquals("SEQ_NAME", this.sequence.getName());
        assertEquals("org.apache.commons.dbcp.BasicDataSource", this.sequence.getDataSource()
                .getClass().getName());
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory()
                    .withResourceName("net/jmob/jsconf/core/sample/app_04.conf")
                    .withFormat(ConfigFormat.CONF)
                    .withBean("dataSource", BasicDataSource.class)
                    .withBean("sequence", Sequence.class)
                    .strict();
        }
    }
}
