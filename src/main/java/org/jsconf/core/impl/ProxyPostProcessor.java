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

package org.jsconf.core.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ProxyPostProcessor {

    private final Map<String, BeanProxy> proxyRef = new HashMap<>();

    private final ApplicationContext context;

    public ProxyPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getInterfaces().length > 0) {
            BeanProxy proxy = this.proxyRef.get(beanName);
            if (proxy == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                List<Class<?>> asList = new ArrayList<>();
                asList.addAll(Arrays.asList(bean.getClass().getInterfaces()));
                asList.add(BeanProxy.class);
                Class<?>[] interfaces = asList.toArray(new Class<?>[asList.size()]);
                proxy = (BeanProxy) Proxy.newProxyInstance(cl, interfaces, new ProxyHandler());
                this.proxyRef.put(beanName, proxy);
            }
            proxy.setBean(bean);
            return proxy;
        } else {
            throw new BeanCreationException(beanName
                    , format("Reloading is only available on bean with interfaces : %s", beanName));
        }
    }

    public void forceProxyInitialization() {
        for (String beanName : this.proxyRef.keySet()) {
            this.context.getBean(beanName);
        }
    }

    @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
    private @interface SetBeanMethod {
    }

    private interface BeanProxy {
        @SetBeanMethod
        void setBean(Object bean);
    }

    private static class ProxyHandler implements InvocationHandler {

        private Object bean;

        public ProxyHandler() {
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.isAnnotationPresent(SetBeanMethod.class)) {
                this.bean = args[0];
                return null;
            } else {
                return method.invoke(this.bean, args);
            }
        }
    }
}
