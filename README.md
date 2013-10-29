Configuration library for Java using JSON format, integrated with Spring on the top of Typesafe config.

JSConf
======

Library is built on the top off the Typesafe config.

## Overview

- JSON format
- Spring injection 
- Support spring profile
- Support hot reloading
- Configuration can be divided to outsource only variables

Planned tasks :
- Documentation
- Clean code


##Examples

####Simple data-source 

File `conf.conf` :

```javascript
{
	datasource : {
	    CLASS : "org.apache.commons.dbcp.BasicDataSource",
	    driverClassName : "com.mysql.jdbc.Driver",
	    url : "jdbc:mysql://localhost:3306/test",
	    username : "user",
	    password : "********"
	}
}
```

####Splited configuration

Internal configuration file `conf.def.conf`  :

```javascript
{
	datasource : {
	        CLASS : "org.apache.commons.dbcp.BasicDataSource",
	        driverClassName : "com.mysql.jdbc.Driver"
	}
}
```

External configuration file `conf.conf` :

```javascript
{
	datasource : {
	    url : "jdbc:mysql://localhost:3306/test",
	    username : "user",
	    password : "********"
	}
}
```

####Simple configuration bean

File `conf.def.conf` internal configuration :

- keyword PROXY is mandatory is you need support hot reload

```javascript
{
	simpleConf : {
	    ID : "MyConf",
	    PROXY : "true"
	    PARENT : "confAbstract"
    }
}
```


File `conf.conf` external configuration :

```javascript
{
	simpleConf : {
	    vstring : "Hello World",
	    vint : 12,
	    vmap : {
	       key1 : "value1",
	       key2 : "value2"
	    },
	    vlist : [ "value1", "value2"]
	}
}
```

The configuration bean is directly injected to a spring service

```java  
@Service("service")
public class Service {

	@Autowired
	@Qualifier("MyConf")
    private Conf conf;
}

```

```xml  
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context-2.5.xsd">
	
	<bean class="org.jsconf.core.ConfigurationFactory"/>
	
	<bean id="confAbstract" class="SimpleConf" abstract="true"/>
	
</beans>
```

- Find more examples in `src\test\resources\org\jsconf\core\sample`

## References

- https://github.com/typesafehub/config
- http://projects.spring.io/spring-framework

## License

The license is Apache 2.0, see LICENSE file.

Copyright (c) 2013, Yves Galante
