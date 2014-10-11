JSON configuration library for Java Spring application, build on the top of Typesafe config.

JSConf
======

Configure easily your applications with configurations format JSON instead of properties files.


## Overview

- JSON / HOCON formats
- Spring integration 
- Hot reloading
- Spring profile
- Spit configuration files to outsource only variables


####Feedback 
We welcome your feedback jsconf@jmob.net

##Examples

####Simple data-source 

File `app.conf` :

```javascript
{
	"datasource" : {
	    "@Class" : "org.apache.commons.dbcp.BasicDataSource",
	    "driverClassName" : "com.mysql.jdbc.Driver",
	    "url" : "jdbc:mysql://localhost:3306/test",
	    "username" : "user",
	    "password" : "********"
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

####Split configuration

Embedded configuration file `app.def.conf`  :

```javascript
{
	"datasource" : {
	        "@Class" : "org.apache.commons.dbcp.BasicDataSource",
	        "driverClassName" : "com.mysql.jdbc.Driver"
	},     
	"sequence" : {
        "@Class" : "org.jsconf.core.sample.Sequence",
        "@Ref" : {dataSource : datasource}
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

Embedded configuration file `app.def.conf`  :

- keyword PROXY is mandatory is you need support hot reload

```javascript
{
	"simpleConf" : {
	    "@Id" : "MyConf",
	    "@Proxy" : "true"
	    "@Parent" : "confAbstract"
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
	
	<jsconf:factory id="factory" resource="org/jsconf/core/test/app" />	
	<bean id="confAbstract" class="SimpleConf" abstract="true"/>	
	
</beans>
```

- Find more examples in `src\test\resources\org\jsconf\core\sample`

## References

- https://github.com/typesafehub/config
- http://projects.spring.io/spring-framework

## License

The license is Apache 2.0, see LICENSE file.

Copyright (c) 2013-2014, Yves Galante
