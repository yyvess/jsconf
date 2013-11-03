JSON configuration library for Java Spring application, build on the top of Typesafe config.

JSConf
======

Configure your spring application with JSON instead of placeholder.


## Overview

- JSON format
- Spring integration 
- Support spring profile
- Support reloading
- Configuration files can be spited to outsource only variables

Planned tasks :
- Documentation
- Clean code
- Auto reload

####Feedback 
We welcome your feedback jsconf@jmob.net

##Examples

####Simple data-source 

File `app.conf` :

```javascript
{
	datasource : {
	    _class : "org.apache.commons.dbcp.BasicDataSource",
	    driverClassName : "com.mysql.jdbc.Driver",
	    url : "jdbc:mysql://localhost:3306/test",
	    username : "user",
	    password : "********"
	}
}
```

```java  
@Service("service")
public class Service {

	@Autowired
    private DataSource datasource;
}
```

####Splited configuration

Internal configuration file `app.def.conf`  :

```javascript
{
	datasource : {
	        _class : "org.apache.commons.dbcp.BasicDataSource",
	        driverClassName : "com.mysql.jdbc.Driver"
	},     
	sequence : {
        _class : "org.jsconf.core.sample.Sequence",
        _ref : {dataSource : datasource}
    }
}
```

External configuration file `app.conf` :

```javascript
{
	datasource : {
	    url : "jdbc:mysql://localhost:3306/test",
	    username : "user",
	    password : "********"
	}, 
	sequence : {
 		name : "SEQ_NAME"
	}
}
```

```java  
@Service("service")
public class Service {

	@Autowired
    private DataSource datasource;
    
	@Autowired
    private Sequence sequence;
}
```

####Simple configuration bean

Internal configuration file `app.def.conf`  :

- keyword PROXY is mandatory is you need support hot reload

```javascript
{
	simpleConf : {
	    _id : "MyConf",
	    _proxy : "true"
	    _parent : "confAbstract"
    }
}
```


External configuration file `app.conf` :

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
