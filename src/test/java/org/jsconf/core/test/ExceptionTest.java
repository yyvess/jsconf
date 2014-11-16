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

import static org.mockito.Mockito.mock;

import org.jsconf.core.ConfigurationFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ExceptionTest {

    @Test(expected = BeanCreationException.class)
    public void testClassNotFound() {
        ConfigurationFactory confFactory = new ConfigurationFactory()
                .withResourceName("org/jsconf/core/test/app_exception01.conf");
        confFactory.postProcessBeanFactory(null);
    }

    @Test(expected = BeanCreationException.class)
    public void testInterfaceNotFound() {
        ConfigurationFactory confFactory = new ConfigurationFactory()
                .withResourceName("org/jsconf/core/test/app_exception02.conf");
        confFactory.postProcessBeanFactory(null);
    }

    @Test(expected = BeanCreationException.class)
    public void testNotAnInterface() {
        ConfigurationFactory confFactory = new ConfigurationFactory()
                .withResourceName("org/jsconf/core/test/app_exception03.conf");
        confFactory.postProcessBeanFactory(null);
    }

    @Test(expected = BeanCreationException.class)
    public void testClassAndParentDefined() {
        ConfigurationFactory confFactory = new ConfigurationFactory()
                .withResourceName("org/jsconf/core/test/app_exception05.conf");
        confFactory.setApplicationContext(mock(GenericApplicationContext.class));
        confFactory.postProcessBeanFactory(mock(ConfigurableListableBeanFactory.class));
    }

    @Test(expected = BeanCreationException.class)
    public void testTryToProxyAClass() {
        ConfigurationFactory confFactory = new ConfigurationFactory()
                .withResourceName("org/jsconf/core/test/app_exception04.conf");
        confFactory.setApplicationContext(mock(GenericApplicationContext.class));
        confFactory.postProcessBeanFactory(mock(ConfigurableListableBeanFactory.class));
        confFactory.postProcessBeforeInitialization(new Object(), "simpleConf");
    }

}
