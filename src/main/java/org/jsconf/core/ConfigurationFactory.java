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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.jsconf.core.impl.BeanProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

// TODO Split it
public class ConfigurationFactory implements BeanDefinitionRegistryPostProcessor, BeanPostProcessor,
		ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationFactory.class);

	private static final String DEFAULT_CONF_NAME = "conf";
	private static final String DEFAULT_SUFIX_DEF = "def";

	private static final String ID = "ID";
	private static final String CLASS = "CLASS";
	private static final String PARENT = "PARENT";
	private static final String PROXY = "PROXY";

	private static final String[] RESERVED_WORD = { ID, CLASS, PARENT, PROXY };

	private final Set<String> beanName = new HashSet<String>();
	private final Set<String> proxyName = new HashSet<String>();
	private final Map<String, BeanProxy> proxyRef = new HashMap<String, BeanProxy>();

	private String confName = DEFAULT_CONF_NAME;
	private boolean reloadable = false;
	private Config devConfig;
	private Config defConfig;
	private GenericApplicationContext context;

	public ConfigurationFactory() {
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = (GenericApplicationContext) applicationContext;
	}

	public void setConfName(String confName) {
		this.confName = confName;
	}

	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		loadConfiguration();
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (this.reloadable && this.proxyName.contains(beanName)) {
			if (bean.getClass().getInterfaces().length > 0) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				List<Class<?>> asList = new ArrayList<Class<?>>();
				asList.addAll(Arrays.asList(bean.getClass().getInterfaces()));
				asList.add(BeanProxy.class);
				Class<?>[] interfaces = asList.toArray(new Class<?>[0]);
				BeanProxy proxy = (BeanProxy) Proxy.newProxyInstance(cl, interfaces, new ProxyHeandler(bean));
				this.proxyRef.put(beanName, proxy);
				return proxy;
			} else {
				LOG.warn("Only bean with interface can be proxy :{}", beanName);
			}
		}
		return bean;
	}

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void reload() {
		for (String name : this.beanName) {
			this.context.removeBeanDefinition(name);
		}
		loadConfiguration();
		for (Entry<String, BeanProxy> e : this.proxyRef.entrySet()) {
			this.proxyRef.get(e.getKey()).setBean(this.context.getBean(e.getKey()));
		}
	}

	private void loadConfiguration() {
		LOG.debug("Loading configuration");
		this.devConfig = ConfigFactory.parseResourcesAnySyntax(this.confName);
		this.defConfig = ConfigFactory.parseResourcesAnySyntax(this.confName.concat(".").concat(DEFAULT_SUFIX_DEF));
		this.defConfig = this.devConfig.withFallback(this.defConfig);
		LOG.debug("Configuration loaded");
		LOG.debug("Initalize beans");
		for (Entry<String, ConfigValue> e : this.defConfig.root().entrySet()) {
			if (isABean(e)) {
				makeBean(e, false);
			}
		}
		LOG.debug("All beans are initalzed");
	}

	private String makeBean(Entry<String, ConfigValue> e, boolean child) {
		String id = getBeanValue(e, ID);
		if (StringUtils.isEmpty(id)) {
			if (child) {
				id = UUID.randomUUID().toString();
			} else {
				id = e.getKey();
			}
		}
		LOG.debug("Initalize bean id : {}", id);
		String parentId = getBeanValue(e, PARENT);
		String className = getBeanValue(e, CLASS);
		String proxy = getBeanValue(e, PROXY);
		BeanDefinitionBuilder beanDef = null;
		if (StringUtils.hasText(parentId)) {
			beanDef = BeanDefinitionBuilder.childBeanDefinition(parentId);
			if (StringUtils.hasText(className)) {
				LOG.warn("def.conf : CLASS value :{} is ignored, use PARENT_ID value :{}", className, parentId);
			}
		} else if (StringUtils.hasText(className)) {
			try {
				beanDef = BeanDefinitionBuilder.genericBeanDefinition(Class.forName(className));
			} catch (ClassNotFoundException e1) {
				LOG.error("Class not found : {}", className);
				//TODO throw new Ex
			}
		} else {
			LOG.error("Bean have not class or parent defined", id);
			//TODO throw new Ex
		}
		if (StringUtils.hasText(proxy) && Boolean.valueOf(proxy)) {
			this.proxyName.add(id);
		}
		LOG.debug("Set properties on bean id : {}", id);
		setBeanProperties(e.getValue(), beanDef);
		LOG.debug("Regitre bean id : {}", id);
		this.beanName.add(id);
		this.context.registerBeanDefinition(id, beanDef.getBeanDefinition());
		return id;
	}

	private boolean isMap(Object value) {
		if (value instanceof Map) {
			return true;
		}
		return false;
	}

	private void setBeanProperties(ConfigValue value, BeanDefinitionBuilder beanDef) {
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, ConfigValue> map = (Map<String, ConfigValue>) value;
			for (Entry<String, ConfigValue> e : map.entrySet()) {
				ConfigValueType valueType = e.getValue().valueType();
				if (valueType.equals(ConfigValueType.OBJECT)) {
					if (isABean(e)) {
						beanDef.addPropertyReference(e.getKey(), makeBean(e, true));
					} else {
						beanDef.addPropertyValue(e.getKey(), e.getValue().unwrapped());
					}
				} else if (!Arrays.asList(RESERVED_WORD).contains(e.getKey())) {
					beanDef.addPropertyValue(e.getKey(), e.getValue().unwrapped());
				}
			}
		} else {
			// TODO
			throw new RuntimeException();
		}
	}

	private boolean isABean(Entry<String, ConfigValue> entry) {
		ConfigValue value = entry.getValue();
		Object unwrapped = value.unwrapped();
		if (isMap(unwrapped)) {
			Map<?, ?> m = (Map<?, ?>) unwrapped;
			return m.containsKey(CLASS) || m.containsKey(PARENT);
		}
		return false;
	}

	private String getBeanValue(Entry<String, ConfigValue> entry, String key) {
		ConfigValue value = entry.getValue();
		Object unwrapped = value.unwrapped();
		if (isMap(unwrapped)) {
			@SuppressWarnings("unchecked")
			Map<String, String> m = (Map<String, String>) unwrapped;
			return m.get(key);
		}
		return null;
	}

	private static class ProxyHeandler implements InvocationHandler, BeanProxy {
		private Object bean;

		public ProxyHeandler(Object bean) {
			setBean(bean);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.isAnnotationPresent(SafeVarargs.class)) {
				this.bean = args[0];
				return null;
			} else {
				return method.invoke(this.bean, args);
			}
		}

		// TODO Change annotation
		@SafeVarargs
		public void setBean(Object bean) {
			this.bean = bean;
		}
	}

}
