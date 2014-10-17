package org.jsconf.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jmx.access.InvocationFailureException;

public class VirtualBean implements InvocationHandler {

	private static final String GET_PREFIX = "get";
	private static final String HASH_CODE_METHOD = "hashCode";
	private static final String EQUALS_METHOD = "equals";

	private final Map<String, Object> values;

	public VirtualBean(Map<String, Object> values) {
		this.values = new HashMap<String, Object>();
		for (Entry<String, Object> e : values == null ? this.values.entrySet() : values.entrySet()) {
			this.values.put(toGetMethod(e.getKey()), e.getValue());
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		int nbArgs = nbArgs(args);
		if (methodName.startsWith(GET_PREFIX) && nbArgs == 0) {
			return magicGet(methodName);
		} else if (methodName.startsWith(HASH_CODE_METHOD) && nbArgs == 0) {
			return hashCode();
		} else if (methodName.startsWith(EQUALS_METHOD) && nbArgs == 1) {
			return equals(args[0]);
		}
		throw new InvocationFailureException(String.format("Incorect method name %s:%s", methodName, nbArgs));
	}

	private Object magicGet(String methodName) {
		return values.get(methodName);
	}

	private int nbArgs(Object[] args) {
		return args == null ? 0 : args.length;
	}

	private String toGetMethod(String propertyName) {
		if (propertyName.length() <= 1) {
			return GET_PREFIX + propertyName.toUpperCase();
		}
		return GET_PREFIX + propertyName.substring(0, 1).toUpperCase().concat(propertyName.substring(1));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VirtualBean other = (VirtualBean) obj;
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	};

	@Override
	public int hashCode() {
		final int prime = 47;
		return prime + (values == null ? 0 : values.hashCode());
	}

	@SuppressWarnings("unchecked")
	public static <T> T factory(Class<T> beanInterface, Map<String, Object> values) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		List<Class<?>> asList = new ArrayList<Class<?>>();
		asList.add(beanInterface);
		Class<?>[] interfaces = asList.toArray(new Class<?>[asList.size()]);
		return (T) Proxy.newProxyInstance(cl, interfaces, new VirtualBean(values));
	}
}
