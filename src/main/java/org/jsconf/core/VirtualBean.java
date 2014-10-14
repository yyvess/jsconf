package org.jsconf.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jmx.access.InvocationFailureException;

public class VirtualBean implements InvocationHandler {

	private Map<String, Object> values = new HashMap<>();

	public VirtualBean() {
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		String name = method.getName();
		if (name.startsWith("set")) {
			if (args == null || args.length != 1) {
				throw new InvocationFailureException(String.format("Incorect arg for method %s", name));
			}
			values.put(name.substring(3), args[0]);
			return null;
		} else if (name.startsWith("get")) {
			if (args != null && args.length != 0) {
				throw new InvocationFailureException(String.format("Incorect arg for method %s", name));
			}
			return values.get(name.substring(3));
		}
		throw new InvocationFailureException(String.format("Incorect method name %s", name));
	}

	@SuppressWarnings("unchecked")
	public static <T> T factory(Class<T> beanInterface) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		List<Class<?>> asList = new ArrayList<Class<?>>();
		asList.add(beanInterface);
		Class<?>[] interfaces = asList.toArray(new Class<?>[asList.size()]);
		return (T) Proxy.newProxyInstance(cl, interfaces, new VirtualBean());
	}

}
