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
package org.jsconf.core.test;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MyConfig implements MyConfigInterface {

	private String value;

	private MyConfigInterface aChild;

	private int aInt;

	private Map<String, String> aMap;

	@Autowired
	@Qualifier("child")
	private SimpleBeanSpring springBeanConfigured;

	@Autowired
	@Qualifier("springOnConf")
	private SimpleBeanSpring springBean;

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public String getValueSpring() {
		return this.springBean.getValue();
	}

	@Override
	public String getValueSpringConfigured() {
		return this.springBeanConfigured.getValue();
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public MyConfigInterface getAChild() {
		return this.aChild;
	}

	public void setAChild(MyConfigInterface aChild) {
		this.aChild = aChild;
	}

	@Override
	public int getAInt() {
		return this.aInt;
	}

	public void setAInt(int aInt) {
		this.aInt = aInt;
	}

	@Override
	public Map<String, String> getAMap() {
		return this.aMap;
	}

	public void setAMap(Map<String, String> aMap) {
		this.aMap = aMap;
	}

}
