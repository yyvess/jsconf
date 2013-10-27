JSConf
======

Copyright (c) 2013, Yves Galante

JSON configuration library for Spring build on the top off the Typesafe configuration library.

This Library is under development !!!

## Overview

- JSON format
- Spring injection 
- Support hot reloading
- Configuration can be split to externalize only some variables

Planned features :

- Support spring profile

Planned tasks :
- Documentation
- Clean code


##Examples

####Simple data-source 

conf.conf.def (internal configuration) :

```javascript
{
	"datasource" : {
	        "CLASS" : "org.apache.commons.dbcp.BasicDataSource",
	        "driverClassName" : "com.mysql.jdbc.Driver"
	}
}
```

conf.conf (external configuration) :

```javascript
{
	datasource : {
	    url : "jdbc:mysql://localhost:3306/test",
	    username : "user",
	    password : "********"
	}
}
```

####Bean spring 

-The bean "MyConf" can be injected on an others standard bean spring


conf.conf.def (internal configuration) :

```javascript
{
	"simpleConf" : {
	    "ID" : "MyConf",
	    "PROXY" : "true"
        "PARENT_ID" : "confAbstract"
    }
}
```

conf.conf (external configuration) :

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

## License

The license is Apache 2.0, see LICENSE file