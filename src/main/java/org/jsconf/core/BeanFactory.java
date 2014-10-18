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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

public class BeanFactory {

	protected static final String ID = "@Id";
	protected static final String CLASS = "@Class";
	protected static final String INTERFACE = "@Interface";
	protected static final String PARENT = "@Parent";
	protected static final String REF = "@Ref";
	protected static final String PROXY = "@Proxy";

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
		String id = this.id;
		BeanDefinitionBuilder beanDef;
		if (StringUtils.isEmpty(id)) {
			if (StringUtils.hasText(this.childName)) {
				id = this.childName;
			} else {
				id = this.key;
			}
		}
		this.log.debug("Initalize bean id : {}", id);
		if (StringUtils.hasText(this.parentId)) {
			beanDef = buildBEanFromParent(id);
		} else if (StringUtils.hasText(this.className)) {
			beanDef = buildBeanFromClass(id);
		} else if (StringUtils.hasText(this.interfaceName)) {
			beanDef = buildBeanFromInterface();
		} else {
			this.log.error("Bean have not Class and parent Id defined", id);
			throw new FatalBeanException("Bean have not class or parent defined");
		}
		this.log.debug("Regitre bean id : {}", id);
		this.context.registerBeanDefinition(id, beanDef.getBeanDefinition());
		return id;
	}

	private BeanDefinitionBuilder buildBEanFromParent(String id) {
		BeanDefinitionBuilder beanDef = BeanDefinitionBuilder.childBeanDefinition(this.parentId);
		if (StringUtils.hasText(this.className)) {
			this.log.warn("def.conf : CLASS value :{} is ignored, use parentId value :{}", this.className,
					this.parentId);
		}
		return setBeanProperties(beanDef, id, this.properties);
	}

	private BeanDefinitionBuilder buildBeanFromClass(String id) {
		try {
			BeanDefinitionBuilder beanDef = BeanDefinitionBuilder.genericBeanDefinition(Class.forName(this.className));
			return setBeanProperties(beanDef, id, this.properties);
		} catch (ClassNotFoundException e) {
			this.log.error("Class not found : {}", this.className);
			throw new FatalBeanException("Class not found", e);
		}
	}

	private BeanDefinitionBuilder buildBeanFromInterface() {
		try {
			Class<?> classBean = Class.forName(this.interfaceName);
			if (!classBean.isInterface()) {
				this.log.error("Interface {} is not a interface.", this.interfaceName);
			}
			return BeanDefinitionBuilder.genericBeanDefinition(VirtualBean.class).setFactoryMethod("factory")//
					.addConstructorArgValue(classBean).addConstructorArgValue(getAllProperties(this.properties));
		} catch (ClassNotFoundException e) {
			this.log.error("Class not found : {}", this.className);
			throw new FatalBeanException("Class not found", e);
		}
	}

	private BeanDefinitionBuilder setBeanProperties(BeanDefinitionBuilder beanDef, String id,
			Map<String, ConfigValue> properties) {
		if (properties != null) {
			int childId = 0;
			for (Entry<String, ConfigValue> e : properties.entrySet()) {
				ConfigValueType valueType = e.getValue().valueType();
				if (valueType.equals(ConfigValueType.OBJECT)) {
					if (isABeanConfigEntry(e)) {
						BeanFactory beanBuilder = new BeanFactory(this.confFactory, this.context);
						beanBuilder.withConfig(e).defineChildName(id, ++childId);
						beanDef.addPropertyReference(e.getKey(), beanBuilder.registerBean());
					} else if (REF.equals(e.getKey())) {
						@SuppressWarnings("unchecked")
						Map<String, String> refs = (Map<String, String>) e.getValue().unwrapped();
						for (Entry<String, String> r : refs.entrySet()) {
							beanDef.addPropertyReference(r.getKey(), r.getValue());
						}
					} else {
						beanDef.addPropertyValue(e.getKey(), e.getValue().unwrapped());
					}
				} else if (!Arrays.asList(RESERVED_WORD).contains(e.getKey())) {
					beanDef.addPropertyValue(e.getKey(), e.getValue().unwrapped());
				}
			}
		}
		return beanDef;
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
