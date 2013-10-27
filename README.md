JSON configuration library integrated into Spring

JSConf
======

Library is built on the top off the Typesafe config.

This Library is under development !!!

## Overview

- JSON format
- Spring injection 
- Support hot reloading
- Configuration can be divided to outsource only the variables

Planned features :

- Support spring profile

Planned tasks :
- Documentation
- Clean code


##Examples

####Simple data-source 

File conf.conf.def (internal configuration) :

```javascript
{
	"datasource" : {
	        "CLASS" : "org.apache.commons.dbcp.BasicDataSource",
	        "driverClassName" : "com.mysql.jdbc.Driver"
	}
}
```

File conf.conf (external configuration) :

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

File conf.conf.def (internal configuration) :

```javascript
{
	"simpleConf" : {
	    "ID" : "MyConf",
	    "PROXY" : "true"
        "PARENT" : "confAbstract"
    }
}
```

File conf.conf (external configuration) :

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

The configuration bean is directly injected the spring service

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

- You can find more examples in the project code
## References

- https://github.com/typesafehub/config
- http://projects.spring.io/spring-framework/

## License

The license is Apache 2.0, see LICENSE file

Copyright (c) 2013, Yves Galante