Histone Template Engine 2 ![Travis build bage](https://travis-ci.org/MegafonWebLab/histone-java2.svg?branch=v2)
=======================

Histone — powerful and flexible template engine, which can be used for HTML - code generation as well as any other kind of text - documents. Histone implementations exists for the web - browser as well as for the server (Java and PHP), it allows you to use same templates on the server and on the client. Built - in extension mechanism allows you to extend default template engine features, by adding your own methods and properties for the particular project. Templates has clean and simple syntax and can be stored either as source code or as compiled code that can be executed with the maximum performance wherever it's needed.

Histone Template Engine Java Implementation
-------------------------------------------

[Documentation](https://github.com/MegafonWebLab/histone-java2/wiki)

Using Histone from Maven
------------------------
To use histone in your maven project you should add new maven dependency to your pom.xml
```xml
<dependency>
    <groupId>com.github.megafonweblab.histone</groupId>
    <artifactId>histone-java-v2</artifactId>
    <version>1.0.1</version>
</dependency>
```

Sources tree
------------

Standart Maven project structure.
	|- src/ project sources
	     |- assembly/ file with maven-assembly-plugin assembly descriptor
	     |- etc/ file for maven-licence-plugin checks
	     |- main/ main sources
	     |- test/ tests sources


Distribution archive contents
-----------------------------
    |- libs/ histone-java dependency libraries
    |- histone-java-A.B.C.jar histone-java library
    |- LICENSE.txt Apache v2.0 license file
    |- NOTICE.txt file with copyright info
    |- README.md this file
