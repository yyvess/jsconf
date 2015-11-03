package org.jsconf.core.impl;

import java.util.Map;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Copyright 2015 Yves Galante
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BeanDefinition {

    private String id;
    private String key;
    private String className;
    private boolean isAInterface;
    private boolean reloading;

    public String getId() {
        if (isEmpty(id)) {
            return this.key;
        }
        return id;
    }

    public BeanDefinition withId(String id) {
        this.id = id;
        return this;
    }

    public BeanDefinition withKey(String key) {
        this.key = key;
        return this;
    }

    public boolean isReloading() {
        return reloading;
    }

    public BeanDefinition withReloading(boolean reloading) {
        this.reloading = reloading;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public BeanDefinition withClassName(String className) {
        this.className = className;
        return this;
    }

    public BeanDefinition isAInterface(boolean isAInterface) {
        this.isAInterface = isAInterface;
        return this;
    }

    public boolean isAInterface() {
        return isAInterface;
    }
}
