JSON configuration library for Java Spring application, build on the top of Typesafe config.

JSConf
======

Configure easily your applications with format JSON instead of flat properties files.

## Overview

- JSON / HOCON formats
- Spring integration 
- Hot reloading
- Spring profile


##Examples

####Simplest bean definition 

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
Use it as a bean Spring ... it's a bean Spring 
```java  
@Service("service")
public class Service {

	@Autowired
    private DataSource datasource;
}
```

Initialize the factory on your applicationContext.xml
```xml  
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:jsconf="http://www.jmob.net/schema/jsconf"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
	http://www.jmob.net/schema/jsconf
	http://www.jmob.net/schema/jsconf/jsconf-1.0.xsd">
	
	<jsconf:factory id="factory" resource="org/jsconf/core/test/app" format="CONF"/>
	 
</beans>
```

####With JavaConfig

File `app.conf` :

```javascript
{
	"datasource" : {
	    "driverClassName" : "com.mysql.jdbc.Driver",
	    "url" : "jdbc:mysql://localhost:3306/test",
	    "username" : "user",
	    "password" : "********"
	}
}
```
Use it as a bean Spring ... it's a bean Spring 
```java  
@Service("service")
public class Service {

	@Autowired
    private DataSource datasource;
}
```

```java  
@Configuration
static class ContextConfiguration {
   @Bean
   public static ConfigurationFactory configurationFactory() {
      return new ConfigurationFactory().withResourceName("myconfig.conf")
         .withBean("datasource", BasicDataSource.class);
   }
}
```

####With a def.conf

Define only values on your first configuration file. 
Into a second file packaged with your application, define beans.

Your external configuration file `app.conf` :

```javascript
{
	datasource : {
	    url : "jdbc:mysql://localhost:3306/test",
	    username : "user",
	    password : "********"
	}, 
	sequence : {
 		name : "SEQ_ID"
	}
}
```

Beans definition `app.def.conf`  :

```javascript
{
	"datasource" : {
	        "@Class" : "org.apache.commons.dbcp.BasicDataSource",
	        "driverClassName" : "com.mysql.jdbc.Driver"
	},     
	"sequence" : {
        "@Class" : "org.jsconf.core.sample.bean.Sequence",
        "@Ref" : {dataSource : datasource}
    }
}
```

And inject your bean ..

```java  
@Service("service")
public class Service {

	@Autowired
    private DataSource datasource;
    
	@Autowired
    private Sequence sequence;
}
```

####Hot reloading support 

Add the keyword "@Proxy" at your bean definition.

Configuration file `app.json` :

```javascript
{
	"simpleConf" : {
	    "@Proxy" : "true"
	    "@Interface" : "org.jsconf.core.test.MyConfig",
	    "url" : "https://localhost",
	    "port" : 12,
	    "aMap" : {
	       "key1" : "value1",
	       "key2" : "value2"
	    },
	    "aList" : [ "value1", "value2"]
	}
}
```
When the configuration files change, beans are seamlessly updated on your services.

```java  
@Service("service")
public class Service {

    @Autowired
    private MyConfig conf;
}
```

- Find more samples in `src\test\resources\org\jsconf\core\sample`

## References

- https://github.com/typesafehub/config
- http://projects.spring.io/spring-framework

## License

The license is Apache 2.0, see LICENSE file.

Copyright (c) 2013-2014, Yves Galante

####Feedback 
We welcome your feedback jsconf@jmob.net
