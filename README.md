Histone Template Engine 2 ![Travis build bage](https://travis-ci.org/MegafonWebLab/histone-java2.svg?branch=v2) [![Coverage Status](https://coveralls.io/repos/github/MegafonWebLab/histone-java2/badge.svg?branch=v2)](https://coveralls.io/github/MegafonWebLab/histone-java2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.megafonweblab.histone/histone-java-v2/badge.svg)](http://mvnrepository.com/artifact/com.github.megafonweblab.histone/histone-java-v2)
=======================

Histone — powerful and flexible template engine, which can be used for HTML - code generation as well as any other kind of text - documents. Histone implementations exists for the web - browser as well as for the server (Java and PHP), it allows you to use same templates on the server and on the client. Built - in extension mechanism allows you to extend default template engine features, by adding your own methods and properties for the particular project. Templates has clean and simple syntax and can be stored either as source code or as compiled code that can be executed with the maximum performance wherever it's needed.

Histone Template Engine Java Implementation
-------------------------------------------

[Project web site](http://weblab.megafon.ru/histone/)  
[Documentation](https://github.com/inver/histone-java2/wiki)
[For contributors](http://weblab.megafon.ru/histone/contributors/#Java)  

Using Histone from Maven
------------------------
To use histone in your maven project you should add histone repository to your maven `settings.xml` file
```xml
<repository>
	<id>central</id>
	<snapshots>
		<enabled>false</enabled>
	</snapshots>
	<name>release</name>
	<url>http://weblab.megafon.ru/maven/release-weblab</url>
</repository>
<repository>
	<id>snapshots</id>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
	<name>snapshot</name>
	<url>http://weblab.megafon.ru/maven/snapshot-weblab</url>
</repository>
```
and then add new maven dependency to your pom.xml
```xml
<dependency>
    <groupId>ru.histone</groupId>
    <artifactId>histone</artifactId>
    <version>HISTONE LATEST VERSION</version>
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

Clone, Build and Run test
-----------------------------
For Windows users before clone repo:
```bash
git config —global core.autocrlf false
```
Clone repo:
```bash
git clone https://github.com/MegafonWebLab/histone-java2 histone-java2
```
Go to the repo:
```bash
cd histone-java2
```
Run test:
```
mvn clean test
```
Build and Run test:
```bash
mvn clean package
```
