Spring configuration module provides JSON & HOCON support, build on the top of Typesafe config

JSConf
======

Configure easily your applications with format JSON instead of flat properties files.

## Overview

- JSON,  HOCON and properties formats
- Spring integration 
- Hot reloading
- Spring profile

##Binary Releases

You can find published releases (compiled for Java 7 and above) on Maven Central.

		<dependency>
			<groupId>net.jmob</groupId>
			<artifactId>jsconf</artifactId>
			<version>1.1.0</version>
		</dependency>

You also need to import Spring context

		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
			<version>3.X.X.RELEASE</version>
		</dependency>
		

Link for direct download if you don't use a dependency manager:

 - http://central.maven.org/maven2/net/jmob/jsconf/

 
 
## Using the Library

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

####Usage with JavaConfig

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
      return new ConfigurationFactory().withResourceName("app.conf")
         .withBean("datasource", BasicDataSource.class);
   }
}
```

####Usage of @ConfigurationProperties
File `app.conf` :

```javascript
{ 
 "root":{
    "simpleConf":{
        "url":"Hello World",
        "port":12,
        "aMap":{
            "key1":"value1",
            "key2":"value2"
        },
        "aList":[
            "value1",
            "value2"
        ]
    }
}
```

File `ConfigBean.java` :
```java  
@ConfigurationProperties("root/simpleConf")
public interface ConfigBean {
    String getUrl();
    int getPort();
    Map<?, ?> getAMap();
    List<?> getAList();
}
```

Use it as a bean Spring ... it's a bean Spring 
```java  
@Service("service")
public class Service {
	@Autowired
    private ConfigBean configBean;
}
```

```java  
    @Configuration
    public class ContextConfiguration {
        @Bean
        public static ConfigurationFactory configurationFactory() {
            return new ConfigurationFactory().withResourceName("app.conf") //
                    .withScanPackage("org.jsconf.core.sample.bean");
        }
    }
```

####Use definition file 

Definitionine only values on your first configuration file. 
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

####Active hot reloading 

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
