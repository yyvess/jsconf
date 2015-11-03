/**
 * Copyright 2013 Yves Galante
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

package org.jsconf.core.test.bean;

import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

public class MyConfig {

    @Value(value = "Spring value")
    private String springValue;

    private String value;

    private MyConfig child;

    private int integer;

    private Map<String, String> map;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MyConfig getChild() {
        return this.child;
    }

    public void setChild(MyConfig child) {
        this.child = child;
    }

    public int getInteger() {
        return this.integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public Map<String, String> getMap() {
        return this.map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public String getSpringValue() {
        return this.springValue;
    }

}
