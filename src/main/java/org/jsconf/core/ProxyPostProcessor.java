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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class ProxyPostProcessor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Map<String, BeanProxy> proxyRef = new HashMap<String, BeanProxy>();

	private final ApplicationContext context;

	public ProxyPostProcessor(ApplicationContext context) {
		this.context = context;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
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
			this.log.warn("Only bean with interface can be proxy :{}", beanName);
		}
		return bean;
	}

	public void injectProxyBean() {
		for (Entry<String, BeanProxy> e : this.proxyRef.entrySet()) {
			this.proxyRef.get(e.getKey()).setBean(this.context.getBean(e.getKey()));
		}
	}

	@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
	private @interface BeanMethod {
	}

	private interface BeanProxy {
		@BeanMethod
		public void setBean(Object bean);
	}

	private static class ProxyHeandler implements InvocationHandler, BeanProxy {
		private Object bean;

		public ProxyHeandler(Object bean) {
			setBean(bean);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.isAnnotationPresent(BeanMethod.class)) {
				this.bean = args[0];
				return null;
			} else {
				return method.invoke(this.bean, args);
			}
		}

		@BeanMethod
		public void setBean(Object bean) {
			this.bean = bean;
		}
	}
}
