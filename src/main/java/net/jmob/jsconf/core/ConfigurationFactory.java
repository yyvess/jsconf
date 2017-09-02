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

package net.jmob.jsconf.core;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;

import net.jmob.jsconf.core.impl.BeanDefinition;
import net.jmob.jsconf.core.impl.BeanFactory;
import net.jmob.jsconf.core.impl.ProxyPostProcessor;
import net.jmob.jsconf.core.service.ClassPathScanningCandidate;
import net.jmob.jsconf.core.service.WatchResource;

public class ConfigurationFactory implements ApplicationContextAware, BeanFactoryPostProcessor, BeanPostProcessor {

    private static final String PROFILE_SEPARATOR = "-";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Set<String> beanName = new HashSet<>();
    private final Set<String> proxyBeanName = new HashSet<>();
    private final List<WatchResource> watcher = new ArrayList<>();
    private final Config config = ConfigFactory.empty();
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();

    private String resourceName;
    private boolean withProfiles = false;

    private ApplicationContext context;
    private ProxyPostProcessor proxyPostProcessor;
    private ConfigParseOptions options = ConfigParseOptions.defaults().setAllowMissing(false);

    public void setFormat(ConfigFormat format) {
        if (format != null) {
            this.options = this.options.setSyntax(ConfigSyntax.valueOf(format.name()));
        }
    }

    public void setStrict(boolean strict) {
        this.options = this.options.setAllowMissing(!strict);
    }

    public void setProfiles(boolean withProfiles) {
        this.withProfiles = withProfiles;
    }

    public ConfigurationFactory withFormat(ConfigFormat format) {
        setFormat(format);
        return this;
    }

    public ConfigurationFactory strict() {
        return withStrict(true);
    }

    public ConfigurationFactory withStrict(boolean strict) {
        setStrict(strict);
        return this;
    }

    public ConfigurationFactory useProfiles() {
        return withProfiles(true);
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

    public ConfigurationFactory withScanPackage(String forPackage) {
        ClassPathScanningCandidate candidateComponentProvider = new ClassPathScanningCandidate(false);
        candidateComponentProvider.addIncludeFilter(new AnnotationTypeFilter(ConfigurationProperties.class));
        Set<Class<?>> candidate = candidateComponentProvider.findCandidateClass(forPackage);
        for (Class<?> cl : candidate) {
            withBean(cl);
        }
        return this;
    }

    public ConfigurationFactory withBean(Class<?> bean) {
        if (bean.isAnnotationPresent(ConfigurationProperties.class)) {
            ConfigurationProperties cf = bean.getAnnotation(ConfigurationProperties.class);
            return withBean(cf.value(), bean, cf.id(), cf.hotReloading());
        }
        throw new BeanInitializationException(format("Missing @ConfigurationProperties annotation on class %s", bean));
    }

    public ConfigurationFactory withBean(String path, Class<?> bean) {
        return withBean(path, bean, null);
    }

    public ConfigurationFactory withBean(String path, Class<?> bean, String id) {
        return withBean(path, bean, id, false);
    }

    public ConfigurationFactory withBean(String path, Class<?> bean, boolean reloading) {
        return withBean(path, bean, null, reloading);
    }

    public ConfigurationFactory withBean(String path, Class<?> bean, String id, boolean reloading) {
        this.beanDefinitions.put(path.replace('/', '.')
                , new BeanDefinition()
                        .withId(id)
                        .withKey(path)
                        .withReloading(reloading)
                        .withClassName(bean.getCanonicalName())
                        .isAInterface(bean.isInterface()));
        return this;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        loadContext();
    }

    public synchronized void reload() {
        clearContext();
        loadContext();
        this.proxyPostProcessor.forceProxyInitialization();
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
        this.context = applicationContext;
        this.proxyPostProcessor = new ProxyPostProcessor(applicationContext);
    }

    private void loadContext() {
        this.log.debug("Loading configuration");
        Config localConfig = this.config;
        List<String> resources = new ArrayList<>(withProfile(this.resourceName));
        for (String resource : resources) {
            localConfig = localConfig.withFallback(ConfigFactory.parseResourcesAnySyntax(resource, this.options));
        }
        this.log.debug("Initialize beans");
        registerBeans(localConfig);
        this.log.debug("Beans are initialized");
        if (!this.proxyBeanName.isEmpty()) {
            for (String resource : resources) {
                this.watcher.add(new WatchResource(this).watch(resource));
            }
        }
    }

    private void registerBeans(Config config) {
        for (Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
            registerBean(new BeanFactory(this.context)
                    .withConfig(config)
                    .withBeanDefinitions(beanDefinitions)
                    .withPath(entry.getKey())
                    .withBeanDefinition(entry.getValue()));
        }
    }

    private void clearContext() {
        for (WatchResource watch : this.watcher) {
            watch.stop();
        }
        AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        for (String name : this.beanName) {
            registry.removeBeanDefinition(name);
        }
        this.watcher.clear();
        this.beanName.clear();
        this.proxyBeanName.clear();
    }

    private String registerBean(BeanFactory beanBuilder) {
        String beanId = beanBuilder.registerBean();
        if (beanBuilder.isReloading()) {
            this.proxyBeanName.add(beanId);
        }
        this.beanName.add(beanId);
        return beanId;
    }

    private List<String> withProfile(String name) {
        List<String> resourcesName = new ArrayList<>();
        if (this.withProfiles) {
            String[] profiles = this.context.getEnvironment().getActiveProfiles();
            for (String profile : profiles) {
                resourcesName.add(buildNameWithProfile(name, profile));
            }
        }
        resourcesName.add(name);
        return resourcesName;
    }

    private String buildNameWithProfile(String name, String profile) {
        String nameWithProfile = name;
        int idx = name.lastIndexOf(".");
        if (idx > 0) {
            nameWithProfile = nameWithProfile.subSequence(0, idx) + PROFILE_SEPARATOR + profile + name.substring(idx);
        } else {
            nameWithProfile = nameWithProfile + PROFILE_SEPARATOR + profile;
        }
        return nameWithProfile;
    }
}
