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

package net.jmob.jsconf.core.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.lang.String.format;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

public class BeanFactory {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GenericApplicationContext context;

    private BeanDefinition beanDefinition;
    private Config config;
    private String path;
    private Map<String, BeanDefinition> beanDefinitions;

    public BeanFactory(GenericApplicationContext context) {
        this.context = context;
    }

    public BeanFactory withBeanDefinition(BeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
        return this;
    }

    public BeanFactory withBeanDefinitions(Map<String, BeanDefinition> beanDefinitions) {
        this.beanDefinitions = beanDefinitions;
        return this;
    }

    public BeanFactory withPath(String path) {
        this.path = path;
        return this;
    }

    public BeanFactory withConfig(Config config) {
        this.config = config;
        return this;
    }

    public boolean isReloading() {
        return this.beanDefinition.isReloading();
    }

    public String registerBean() {
        final BeanDefinitionBuilder beanDefinition;
        final String beanId = this.beanDefinition.getId();
        this.log.debug("Initialize bean id : {}", beanId);
        if (this.beanDefinition.isAInterface()) {
            beanDefinition = buildBeanFromInterface();
        } else {
            beanDefinition = buildBeanFromClass();
        }
        this.log.debug("Register bean id : {}", beanId);
        this.context.registerBeanDefinition(beanId, beanDefinition.getBeanDefinition());
        return beanId;
    }

    private BeanDefinitionBuilder buildBeanFromClass() {
        try {
            BeanDefinitionBuilder beanDefinition = genericBeanDefinition(Class.forName(this.beanDefinition.getClassName()));
            return addPropertiesValue(beanDefinition, buildProperties(beanDefinition));
        } catch (ClassNotFoundException e) {
            throw new BeanCreationException(format("Class not found : %s", this.beanDefinition.getClassName()), e);
        }
    }

    private BeanDefinitionBuilder buildBeanFromInterface() {
        try {
            Class<?> classBean = Class.forName(this.beanDefinition.getClassName());
            if (!classBean.isInterface()) {
                throw new BeanCreationException(format("Interface is not an interface : %s", this.beanDefinition.getClassName()));
            }
            BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(VirtualBean.class);
            return beanDefinitionBuilder
                    .setFactoryMethod("factory")
                    .addConstructorArgValue(classBean)
                    .addConstructorArgValue(unwrapProperties(buildProperties(beanDefinitionBuilder)));
        } catch (ClassNotFoundException e) {
            throw new BeanCreationException(format("Class not found : %s", this.beanDefinition.getClassName()), e);
        }
    }

    private Map<String, Object> unwrapProperties(Set<Entry<String, ConfigValue>> properties) {
        Map<String, Object> values = new HashMap<>();
        if (properties != null) {
            for (Entry<String, ConfigValue> e : properties) {
                values.put(e.getKey(), e.getValue().unwrapped());
            }
        }
        return values;
    }

    private BeanDefinitionBuilder addPropertiesValue(BeanDefinitionBuilder beanDefinition
            , Set<Entry<String, ConfigValue>> properties) {
        for (Entry<String, ConfigValue> e : properties) {
            beanDefinition.addPropertyValue(e.getKey(), e.getValue().unwrapped());
        }
        return beanDefinition;
    }

    private Set<Entry<String, ConfigValue>> buildProperties(BeanDefinitionBuilder beanDefinition) {
        Set<String> paths = beanDefinitions.keySet();
        Set<Entry<String, ConfigValue>> entries = new HashSet<>();
        try {
            for (Entry<String, ConfigValue> entry : config.getConfig(path).root().entrySet()) {
                String fullPath = path.concat(".").concat(entry.getKey());
                if (paths.contains(fullPath)) {
                    beanDefinition.addPropertyReference(entry.getKey(), beanDefinitions.get(fullPath).getId());
                } else {
                    entries.add(entry);
                }
            }
        } catch (ConfigException.Missing e) {
            this.log.debug(format("No configuration found on path %s", path));
        }
        return entries;
    }
}
