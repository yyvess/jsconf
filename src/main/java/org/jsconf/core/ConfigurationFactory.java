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

import org.jsconf.core.service.ConfigWatchService;
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

	private static final String DEFAULT_CONF_NAME = "app";
	private static final String DEFAULT_SUFIX_DEF = "def";

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ConfigWatchService watchService = new ConfigWatchService(this);
	private final Set<String> beanName = new HashSet<String>();
	private final Set<String> proxyBeanName = new HashSet<String>();

	private String resourceName = DEFAULT_CONF_NAME;

	private GenericApplicationContext context;
	private ProxyPostProcessor proxyPostProcessor;
	private ConfigParseOptions options = ConfigParseOptions.defaults();
	private Config devConfig;
	private Config defConfig;

	public void setFormat(String format) {
		if (StringUtils.hasText(format)) {
			options = options.setSyntax(ConfigSyntax.valueOf(format));
		}
	}

	public void setStrict(String strict) {
		if (StringUtils.hasText(strict)) {
			options = options.setAllowMissing(!Boolean.valueOf(strict));
		}
	}

	public void setResourceName(String resource) {
		this.resourceName = resource;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		loadContext();
	}

	public synchronized void reload() {
		clearContext();
		loadContext();
		this.proxyPostProcessor.forceProxyInitalization();
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (this.proxyBeanName.contains(beanName)) {
			return this.proxyPostProcessor.postProcessBeforeInitialization(bean, beanName);
		}
		return bean;
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
		this.devConfig = getConfig(this.resourceName, null, null);
		for (String profile : profiles) {
			Config c = getConfig(this.resourceName, profile, null);
			this.devConfig = c.withFallback(this.devConfig);
		}
		this.defConfig = getConfig(this.resourceName, null, DEFAULT_SUFIX_DEF);
		for (String profile : profiles) {
			Config c = getConfig(this.resourceName, profile, DEFAULT_SUFIX_DEF);
			this.defConfig = c.withFallback(this.defConfig);
		}
		this.defConfig = this.devConfig.withFallback(this.defConfig);
		this.log.debug("Initalize beans");
		for (Entry<String, ConfigValue> entry : this.defConfig.root().entrySet()) {
			BeanFactory beanBuilder = new BeanFactory(this, this.context).withConfig(entry);
			if (beanBuilder.isValid()) {
				buildBeans(beanBuilder);
			}
		}
		this.log.debug("Beans are initalzed");
		if (!this.proxyBeanName.isEmpty()) {
			this.watchService.watch(this.resourceName);
		}
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
		return ConfigFactory.parseResourcesAnySyntax(finalName, options);
	}
}
