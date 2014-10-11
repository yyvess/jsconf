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
package org.jsconf.core.tag;

import org.jsconf.core.ConfigurationFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class ConfigFactoryBeanParser extends AbstractSingleBeanDefinitionParser {

	protected Class<ConfigurationFactory> getBeanClass(Element element) {
		return ConfigurationFactory.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		bean.addPropertyValue("resourceName", element.getAttribute("resource"));
		bean.addPropertyValue("format", element.getAttribute("format"));
		bean.addPropertyValue("strict", element.getAttribute("strict"));
	}

}