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
			<version>1.2.0</version>
		</dependency>

Spring context is required, it's not provided by this library

		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
			<version>3.X.X.RELEASE</version>
		</dependency>
		
To active beans validation, you must import a validator like Hibernate validator
		
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-validator-cdi</artifactId>
            <version>5.2.2.Final</version>
            <scope>test</scope>
        </dependency>

Link for direct download if you don't use a dependency manager:

 - http://central.maven.org/maven2/net/jmob/jsconf/

 
 
## Library usage

####Sample usage with JavaConfig

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

- Find more samples in `src\test\resources\org\jsconf\core\sample`

## References

- https://github.com/typesafehub/config
- http://projects.spring.io/spring-framework

## License

The license is Apache 2.0, see LICENSE file.

Copyright (c) 2013-2015, Yves Galante

####Feedback 
We welcome your feedback jsconf@jmob.net
