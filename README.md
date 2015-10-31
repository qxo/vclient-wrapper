## vClient wrapper (2013)

At the time I built up this wrapper, the API to retrieve info from vSphere 
service was difficult to use and hard to understand. So, I decided to create
this small project to make easier getting info from virtual machines handled
by vSphere service. To do so, I wrote the wrapper based on the VMware VI (vSphere)
Java API [1]. This was used to connect to the vSphere version 5.1.


## Dependencies

* log4j
* gson
* vijava

### build and install to maven local repo
`
mvn clean install

`

### get artifacts from repo
`
mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:get  -Dartifact=qxo:vclient-wrapper:1.0.0b01:pom -Ddest=pom.xml

mvn dependency:copy-dependencies -DoutputDirectory=lib   -DincludeScope=test -Dartifact=qxo:vclient-wrapper:1.0.0b01:jar

mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:copy  -Dartifact=qxo:vclient-wrapper:1.0.0b01:jar  -DoutputDirectory=lib

 `

 ### exmaple

` 
 java -cp lib/*  org.vm.vs.wrapper.VSphereClientFacade  <vcenter sdk url> <user> <password> <vm name>
 `
	
## Ref

[1] http://vijava.sourceforge.net/
