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

package org.jsconf.core.sample;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.jsconf.core.ConfigurationFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class Sample00 {

    @Autowired()
    private DataSource datasource;

    @Test
    public void test() {
        BasicDataSource basic = (BasicDataSource) this.datasource;
        Assert.assertEquals("jdbc:mysql://localhost:3306/test", basic.getUrl());
        Assert.assertEquals("com.mysql.jdbc.Driver", basic.getDriverClassName());
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("org/jsconf/core/sample/app_00");
        }
    }
}
