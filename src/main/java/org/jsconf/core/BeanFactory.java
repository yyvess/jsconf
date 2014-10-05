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

	private static final String ID = "_id";
	private static final String CLASS = "_class";
	private static final String PARENT = "_parent";
	private static final String REF = "_ref";
	private static final String PROXY = "_proxy";

	private static final String[] RESERVED_WORD = { ID, CLASS, PARENT, REF, PROXY };

	private static int beanIdGen;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ConfigurationFactory confFactory;
	private final GenericApplicationContext context;

	private String key;
	private String id;
	private String className;
	private String parentId;
	private boolean child;
	private boolean proxy;
	private boolean isABean;
	private Map<String, ConfigValue> properties;

	public BeanFactory(ConfigurationFactory confFactory, GenericApplicationContext context) {
		this.confFactory = confFactory;
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public BeanFactory withConfig(Entry<String, ConfigValue> config) {
		this.isABean = isABean(config);
		if (this.isABean) {
			this.key = config.getKey();
			this.id = getBeanValue(config, ID);
			this.className = getBeanValue(config, CLASS);
			this.parentId = getBeanValue(config, PARENT);
			this.proxy = Boolean.TRUE.equals(getBeanValue(config, PROXY));
			if (isAMap(config.getValue())) {
				this.properties = (Map<String, ConfigValue>) config.getValue();
			}
		}
		return this;
	}

	public boolean isABean() {
		return this.isABean;
	}

	public boolean isProxy() {
		return this.proxy;
	}

	public String registerBean() {
		BeanDefinitionBuilder beanDef;
		if (StringUtils.isEmpty(this.id)) {
			if (this.child) {
				this.id = "child-" + ++beanIdGen;
			} else {
				this.id = this.key;
			}
		}
		this.log.debug("Initalize bean id : {}", this.id);
		if (StringUtils.hasText(this.parentId)) {
			beanDef = BeanDefinitionBuilder.childBeanDefinition(this.parentId);
			if (StringUtils.hasText(this.className)) {
				this.log.warn("def.conf : CLASS value :{} is ignored, use PARENT_ID value :{}", this.className,
						this.parentId);
			}
		} else if (StringUtils.hasText(this.className)) {
			try {
				beanDef = BeanDefinitionBuilder.genericBeanDefinition(Class.forName(this.className));
			} catch (ClassNotFoundException e) {
				this.log.error("Class not found : {}", this.className);
				throw new FatalBeanException("Class not found", e);
			}
		} else {
			this.log.error("Bean have not Class and parent Id defined", this.id);
			throw new FatalBeanException("Bean have not class or parent defined");
		}
		this.log.debug("Set properties on bean id : {}", this.id);
		setBeanProperties(this.properties, beanDef);
		this.log.debug("Regitre bean id : {}", this.id);
		this.context.registerBeanDefinition(this.id, beanDef.getBeanDefinition());
		return this.id;
	}

	private BeanFactory withChild(boolean child) {
		this.child = child;
		return this;
	}

	private void setBeanProperties(Map<String, ConfigValue> properties, BeanDefinitionBuilder beanDef) {
		if (properties != null) {
			for (Entry<String, ConfigValue> e : properties.entrySet()) {
				ConfigValueType valueType = e.getValue().valueType();
				if (valueType.equals(ConfigValueType.OBJECT)) {
					BeanFactory beanBuilder = new BeanFactory(this.confFactory, this.context).withConfig(e).withChild(
							true);
					if (beanBuilder.isABean) {
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
	}

	private <T> T getBeanValue(Entry<String, ConfigValue> entry, String key) {
		ConfigValue value = entry.getValue();
		Object unwrapped = value.unwrapped();
		if (unwrapped instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, T> m = (Map<String, T>) unwrapped;
			return m.get(key);
		}
		return null;
	}

	private boolean isABean(Entry<String, ConfigValue> entry) {
		Object unwrapped = entry.getValue().unwrapped();
		if (isAMap(unwrapped)) {
			Map<?, ?> m = (Map<?, ?>) unwrapped;
			return m.containsKey(CLASS) || m.containsKey(PARENT);
		}
		return false;
	}

	private boolean isAMap(Object value) {
		return value instanceof Map;
	}
}
