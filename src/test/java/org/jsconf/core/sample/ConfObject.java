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
package org.jsconf.core.sample;

import java.util.List;
import java.util.Map;

public class ConfObject implements ConfInterface {

	private String vstring;
	private int vint;
	private Map<String, String> vmap;
	private List<String> vlist;

	public String getVstring() {
		return this.vstring;
	}

	public void setVstring(String vstring) {
		this.vstring = vstring;
	}

	public int getVint() {
		return this.vint;
	}

	public void setVint(int vint) {
		this.vint = vint;
	}

	public Map<String, String> getVmap() {
		return this.vmap;
	}

	public void setVmap(Map<String, String> vmap) {
		this.vmap = vmap;
	}

	public List<String> getVlist() {
		return this.vlist;
	}

	public void setVlist(List<String> vlist) {
		this.vlist = vlist;
	}
}
