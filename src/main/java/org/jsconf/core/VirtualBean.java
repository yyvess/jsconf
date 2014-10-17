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

	private final Map<String, Object> values;

	public VirtualBean(Map<String, Object> values) {
		this.values = new HashMap<String, Object>();
		for (Entry<String, Object> e : values.entrySet()) {
			this.values.put(toMethodeName(e.getKey()), e.getValue());
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		int nbArgs = nbArgs(args);
		if (methodName.startsWith("get")) {
			if (nbArgs != 0) {
				throw new InvocationFailureException(String.format("Incorect arg for method %s", methodName));
			}
			return values.get(methodName);
		} else if (methodName.startsWith("equals") && nbArgs == 1) {
			return equals(args[0]);
		} else if (methodName.startsWith("hashCode") && nbArgs == 0) {
			return hashCode();
		}
		throw new InvocationFailureException(String.format("Incorect method name %s", methodName));
	}

	private int nbArgs(Object[] args) {
		return args == null ? 0 : args.length;
	}

	private String toMethodeName(String propertyName) {
		if (propertyName.length() <= 1) {
			return "get" + propertyName.toUpperCase();
		}
		return "get" + propertyName.substring(0, 1).toUpperCase().concat(propertyName.substring(1));
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
