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

package org.jsconf.core;

import static org.jsconf.core.impl.BeanFactory.CLASS;
import static org.jsconf.core.impl.BeanFactory.ID;
import static org.jsconf.core.impl.BeanFactory.INTERFACE;
import static org.jsconf.core.impl.BeanFactory.PROXY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsconf.core.impl.BeanFactory;
import org.jsconf.core.impl.ProxyPostProcessor;
import org.jsconf.core.service.WatchResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;

public class ConfigurationFactory implements ApplicationContextAware, BeanFactoryPostProcessor, BeanPostProcessor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Set<String> beanName = new HashSet<>();
    private final Set<String> proxyBeanName = new HashSet<>();
    private final List<WatchResource> watcher = new ArrayList<>();

    private boolean withDefinition = false;
    private boolean withProfiles = false;

    private String resourceName;

    private GenericApplicationContext context;
    private ProxyPostProcessor proxyPostProcessor;
    private ConfigParseOptions options = ConfigParseOptions.defaults().setAllowMissing(false);
    private Config config = ConfigFactory.empty();

    public void setFormat(ConfigFormat format) {
        if (format != null) {
            this.options = this.options.setSyntax(ConfigSyntax.valueOf(format.name()));
        }
    }

    public void setStrict(boolean strict) {
        this.options = this.options.setAllowMissing(!strict);
    }

    public void setDefinition(boolean withDefinition) {
        this.withDefinition = Boolean.valueOf(withDefinition);
    }

    public void setProfiles(boolean withProfiles) {
        this.withProfiles = withProfiles;
    }

    // TOOD use enum
    public ConfigurationFactory withFormat(ConfigFormat format) {
        setFormat(format);
        return this;
    }

    public ConfigurationFactory withStrict(boolean strict) {
        setStrict(strict);
        return this;
    }

    public ConfigurationFactory withDefinition(boolean def) {
        setDefinition(def);
        return this;
    }

    public ConfigurationFactory withProfiles(boolean profile) {
        setProfiles(profile);
        return this;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public ConfigurationFactory withResourceName(String resourceName) {
        setResourceName(resourceName);
        return this;
    }

    public ConfigurationFactory withBean(String path, Class<?> bean) {
        return withBean(path, bean, null);
    }

    public ConfigurationFactory withBean(String path, Class<?> bean, boolean proxy) {
        return withBean(path, bean, null, proxy);
    }

    public ConfigurationFactory withBean(String path, Class<?> bean, String id) {
        return withBean(path, bean, id, false);
    }

    public ConfigurationFactory withBean(String path, Class<?> bean, String id, boolean proxy) {
        Map<String, Object> properties = new HashMap<>(2);
        Map<String, Map<String, ? extends Object>> object = new HashMap<>(2);
        if (StringUtils.hasText(id)) {
            properties.put("\"" + ID + "\"", id);
        }
        if (proxy) {
            properties.put("\"" + PROXY + "\"", true);
        }
        if (bean.isInterface()) {
            properties.put("\"" + INTERFACE + "\"", bean.getCanonicalName());
        } else {
            properties.put("\"" + CLASS + "\"", bean.getCanonicalName());
        }
        String[] splitedPath = path.split("/");
        object.put(splitedPath[splitedPath.length - 1], properties);
        for (int i = splitedPath.length - 2; i >= 0; i--) {
            Map<String, Map<String, ? extends Object>> child = object;
            object = new HashMap<>(2);
            object.put(splitedPath[i], child);
        }
        this.config = this.config.withFallback(ConfigFactory.parseMap(object));
        return this;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        loadContext();
    }

    public synchronized void reload() {
        clearContext();
        loadContext();
        this.proxyPostProcessor.forceProxyInitalization();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (this.proxyBeanName.contains(beanName)) {
            return this.proxyPostProcessor.postProcessBeforeInitialization(bean, beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (GenericApplicationContext) applicationContext;
        this.proxyPostProcessor = new ProxyPostProcessor(applicationContext);
    }

    private void loadContext() {
        this.log.debug("Loading configuration");
        Config localConfig = this.config;
        List<String> ressources = new ArrayList<>(withProfile(this.resourceName));
        for (String resource : ressources) {
            localConfig = localConfig.withFallback(ConfigFactory.parseResourcesAnySyntax(resource, this.options));
        }
        this.log.debug("Initalize beans");
        for (Entry<String, ConfigValue> entry : localConfig.root().entrySet()) {
            BeanFactory beanBuilder = new BeanFactory(this, this.context).withConfig(entry);
            if (beanBuilder.isValid()) {
                buildBeans(beanBuilder);
            }
        }
        this.log.debug("Beans are initalzed");
        if (!this.proxyBeanName.isEmpty()) {
            for (String resource : ressources) {
                this.watcher.add(new WatchResource(this).watch(resource));
            }
        }
    }

    private void clearContext() {
        for (WatchResource watch : this.watcher) {
            watch.stop();
        }
        for (String name : this.beanName) {
            this.context.removeBeanDefinition(name);
        }
        this.watcher.clear();
        this.beanName.clear();
        this.proxyBeanName.clear();
    }

    private String buildBeans(BeanFactory beanBuilder) {
        String beanId = beanBuilder.registerBean();
        if (beanBuilder.isProxy()) {
            this.proxyBeanName.add(beanId);
        }
        this.beanName.add(beanId);
        return beanId;
    }

    private List<String> withProfile(String name) {
        List<String> ressourcesName = new ArrayList<>();
        if (this.withProfiles) {
            String[] profiles = this.context.getEnvironment().getActiveProfiles();
            for (String profile : profiles) {
                String nameWithProfile = name;
                int idx = name.lastIndexOf(".");
                if (idx > 0) {
                    nameWithProfile = nameWithProfile.subSequence(0, idx) + "-" + profile + name.substring(idx);
                } else {
                    nameWithProfile = nameWithProfile + "-" + profile;
                }
                ressourcesName.add(nameWithProfile);
            }
        }
        ressourcesName.addAll(withDefinition(name));
        return ressourcesName;
    }

    private List<String> withDefinition(String name) {
        List<String> ressourcesName = new ArrayList<>();
        if (this.withDefinition) {
            int idx = name.lastIndexOf(".");
            if (idx > 0) {
                ressourcesName.add(name.subSequence(0, idx) + ".def" + name.substring(idx));
            } else {
                ressourcesName.add(name + ".def");
            }
        }
        ressourcesName.add(name);
        return ressourcesName;
    }
}
