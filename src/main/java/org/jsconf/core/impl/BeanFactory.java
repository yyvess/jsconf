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
package org.jsconf.core.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsconf.core.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

public class BeanFactory {

	public static final String ID = "@Id";
	public static final String CLASS = "@Class";
	public static final String INTERFACE = "@Interface";
	public static final String PARENT = "@Parent";
	public static final String REF = "@Ref";
	public static final String PROXY = "@Proxy";

	private static final String[] RESERVED_WORD = { ID, CLASS, INTERFACE, PARENT, REF, PROXY };

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ConfigurationFactory confFactory;
	private final GenericApplicationContext context;

	private String key;
	private String id;
	private String className;
	private String interfaceName;
	private String parentId;
	private String childName;
	private boolean proxy;
	private boolean isValid;
	private Map<String, ConfigValue> properties;

	public BeanFactory(ConfigurationFactory confFactory, GenericApplicationContext context) {
		this.confFactory = confFactory;
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public BeanFactory withConfig(Entry<String, ConfigValue> config) {
		if (!isABeanConfigEntry(config)) {
			return this;
		}
		this.isValid = true;
		this.key = config.getKey();
		this.id = getBeanValue(config, ID);
		this.className = getBeanValue(config, CLASS);
		this.interfaceName = getBeanValue(config, INTERFACE);
		this.parentId = getBeanValue(config, PARENT);
		this.proxy = Boolean.TRUE.equals(getBeanValue(config, PROXY));
		if (isAMap(config.getValue())) {
			this.properties = (Map<String, ConfigValue>) config.getValue();
		}
		return this;
	}

	public boolean isValid() {
		return this.isValid;
	}

	public boolean isProxy() {
		return this.proxy;
	}

	public String registerBean() {
		String beanId = this.id;
		BeanDefinitionBuilder beanDefinition;
		if (StringUtils.isEmpty(beanId)) {
			if (StringUtils.hasText(this.childName)) {
				beanId = this.childName;
			} else {
				beanId = this.key;
			}
		}
		this.log.debug("Initalize bean id : {}", beanId);
		if (StringUtils.hasText(this.parentId)) {
			beanDefinition = buildBEanFromParent(beanId);
		} else if (StringUtils.hasText(this.className)) {
			beanDefinition = buildBeanFromClass(beanId);
		} else if (StringUtils.hasText(this.interfaceName)) {
			beanDefinition = buildBeanFromInterface();
		} else {
			throw new BeanCreationException(beanId, "Bean have not class or parent defined");
		}
		this.log.debug("Regitre bean id : {}", beanId);
		this.context.registerBeanDefinition(beanId, beanDefinition.getBeanDefinition());
		return beanId;
	}

	private BeanDefinitionBuilder buildBEanFromParent(String beanId) {
		BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.childBeanDefinition(this.parentId);
		if (StringUtils.hasText(this.className)) {
			throw new BeanCreationException(beanId, String.format("Bean have a Class %s and a Parent defined : %s",
					this.className, this.parentId));
		}
		return setBeanProperties(beanDefinition, beanId, this.properties);
	}

	private BeanDefinitionBuilder buildBeanFromClass(String beanId) {
		try {
			BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(Class
					.forName(this.className));
			return setBeanProperties(beanDefinition, beanId, this.properties);
		} catch (ClassNotFoundException e) {
			throw new BeanCreationException(String.format("Class not found : %s", this.className), e);
		}
	}

	private BeanDefinitionBuilder buildBeanFromInterface() {
		try {
			Class<?> classBean = Class.forName(this.interfaceName);
			if (!classBean.isInterface()) {
				throw new BeanCreationException(String.format("Interface is not an interface : %s", this.interfaceName));
			}
			return BeanDefinitionBuilder.genericBeanDefinition(VirtualBean.class).setFactoryMethod("factory")//
					.addConstructorArgValue(classBean).addConstructorArgValue(getAllProperties(this.properties));
		} catch (ClassNotFoundException e) {
			throw new BeanCreationException(String.format("Class not found : %s", this.interfaceName), e);
		}
	}

	private BeanDefinitionBuilder setBeanProperties(BeanDefinitionBuilder beanDefinition, String beanId,
			Map<String, ConfigValue> properties) {
		if (properties != null) {
			int childId = 0;
			for (Entry<String, ConfigValue> e : properties.entrySet()) {
				ConfigValueType valueType = e.getValue().valueType();
				if (valueType.equals(ConfigValueType.OBJECT)) {
					if (isABeanConfigEntry(e)) {
						BeanFactory beanBuilder = new BeanFactory(this.confFactory, this.context);
						beanBuilder.withConfig(e).defineChildName(beanId, ++childId);
						beanDefinition.addPropertyReference(e.getKey(), beanBuilder.registerBean());
					} else if (REF.equals(e.getKey())) {
						@SuppressWarnings("unchecked")
						Map<String, String> refs = (Map<String, String>) e.getValue().unwrapped();
						for (Entry<String, String> r : refs.entrySet()) {
							beanDefinition.addPropertyReference(r.getKey(), r.getValue());
						}
					} else {
						beanDefinition.addPropertyValue(e.getKey(), e.getValue().unwrapped());
					}
				} else if (!Arrays.asList(RESERVED_WORD).contains(e.getKey())) {
					beanDefinition.addPropertyValue(e.getKey(), e.getValue().unwrapped());
				}
			}
		}
		return beanDefinition;
	}

	private Map<String, Object> getAllProperties(Map<String, ConfigValue> properties) {
		Map<String, Object> values = new HashMap<>();
		if (properties != null) {
			for (Entry<String, ConfigValue> e : properties.entrySet()) {
				values.put(e.getKey(), e.getValue().unwrapped());
			}
		}
		return values;
	}

	private BeanFactory defineChildName(String parentId, int childId) {
		this.childName = parentId + "-child-" + childId;
		return this;
	}

	@SuppressWarnings("unchecked")
	private <T> T getBeanValue(Entry<String, ConfigValue> entry, String key) {
		ConfigValue value = entry.getValue();
		Object object = value.unwrapped();
		if (isAMap(object)) {
			return (T) ((Map<?, ?>) object).get(key);
		}
		return null;
	}

	private boolean isABeanConfigEntry(Entry<String, ConfigValue> entry) {
		Object object = entry.getValue().unwrapped();
		if (isAMap(object)) {
			Map<?, ?> map = (Map<?, ?>) object;
			return map.containsKey(CLASS) || map.containsKey(INTERFACE) || map.containsKey(PARENT);
		}
		return false;
	}

	private boolean isAMap(Object value) {
		return value instanceof Map;
	}
}
