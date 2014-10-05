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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

@Configurable
public class ConfigurationFactory implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor,
		BeanPostProcessor {

	private static final String DEFAULT_CONF_NAME = "app";
	private static final String DEFAULT_SUFIX_DEF = "def";

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Set<String> beanName = new HashSet<String>();
	private final Set<String> proxyBeanName = new HashSet<String>();

	private String configuration = DEFAULT_CONF_NAME;

	private GenericApplicationContext context;
	private ProxyPostProcessor proxyPostProcessor;
	private Config devConfig;
	private Config defConfig;

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		loadContext();
	}
	
	public void reload() {
		clearContext();
		loadContext();
		this.proxyPostProcessor.injectProxyBean();
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (this.proxyBeanName.contains(beanName)) {
			return this.proxyPostProcessor.postProcessBeforeInitialization(bean, beanName);
		}
		return bean;
	}

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = (GenericApplicationContext) applicationContext;
		this.proxyPostProcessor = new ProxyPostProcessor(applicationContext);
	}

	private void loadContext() {
		this.log.debug("Loading configuration");
		String[] profiles = this.context.getEnvironment().getActiveProfiles();
		this.devConfig = getConfig(this.configuration, null, null);
		for (String profile : profiles) {
			Config c = getConfig(this.configuration, profile, null);
			this.devConfig = c.withFallback(this.devConfig);
		}
		this.defConfig = getConfig(this.configuration, null, DEFAULT_SUFIX_DEF);
		for (String profile : profiles) {
			Config c = getConfig(this.configuration, profile, DEFAULT_SUFIX_DEF);
			this.defConfig = c.withFallback(this.defConfig);
		}
		this.defConfig = this.devConfig.withFallback(this.defConfig);
		this.log.debug("Configuration loaded");
		this.log.debug("Initalize beans");
		for (Entry<String, ConfigValue> entry : this.defConfig.root().entrySet()) {
			BeanFactory beanBuilder = new BeanFactory(this, this.context).withConfig(entry);
			if (beanBuilder.isABean()) {
				buildBeans(beanBuilder);
			}
		}
		this.log.debug("Beans are initalzed");
	}

	private void clearContext() {
		for (String name : this.beanName) {
			this.context.removeBeanDefinition(name);
		}
		this.beanName.clear();
		this.proxyBeanName.clear();
	}

	private String buildBeans(BeanFactory beanBuilder) {
		String beanId = beanBuilder.registerBean();
		if (beanBuilder.isProxy()) {
			this.proxyBeanName.add(beanId);
		}
		this.log.debug("Regitre bean id : {}", beanId);
		this.beanName.add(beanId);
		return beanId;
	}

	private Config getConfig(String name, String profile, String sufix) {
		String finalName = name;
		if (StringUtils.hasText(profile)) {
			finalName = finalName.concat("-").concat(profile);
		}
		if (StringUtils.hasText(sufix)) {
			finalName = finalName.concat(".").concat(sufix);
		}
		return ConfigFactory.parseResourcesAnySyntax(finalName);
	}
}
