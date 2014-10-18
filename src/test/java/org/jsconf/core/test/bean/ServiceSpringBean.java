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
package org.jsconf.core.test.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("serviceSpring")
public class ServiceSpringBean {

	private String value = "Hello World, I a spring bean!";

	@Autowired
	@Qualifier("test")
	private MyConfig child;

	public String getValue() {
		return this.value;
	}

	public String getChildValue() {
		return this.child.getValue();
	}

	public void setValue(String value) {
		this.value = value;
	}

	public MyConfig getConfig() {
		return this.child;
	}
}
