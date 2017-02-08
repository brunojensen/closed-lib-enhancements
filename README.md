# README #

Assuming that you cannot change the code of certain libraries on your java projects, this project aims to generate "enhancements" on target library's contained pojos.

##Builder enchament
It creates methods on pojo's classes to achieve more productivity on software development in legacy systems context.
 - Creates a fluent interface design pattern based on pojo's attribute setters.

### Plugin Usage ###
Add the plugin on maven build section and define what artifacts do you want enhance using closed-artifac tag:
```xml
<plugin>
 <groupId>solutions.kilian.legacy</groupId>
 <artifactId>closed-lib-builder-maven-plugin</artifactId>
 <version>${closed-lib-version}</version>
 <configuration>
  <closed-artifacts>
   <closed-artifact>
    <groupId>group.artifact</groupId>
    <artifactId>artifact-id</artifactId>
    <version>version</version>
   </closed-artifact>
  </closed-artifacts>
  <exclusions>
   <exclusion>class.to.exclude.without.class.suffix</exclusion>
  </exclusions>
 </configuration>
</plugin>
```

Run the closed-lib-builder:generate goal on project that you added the plugin:
```sh
mvn closed-lib-builder:generate
```
Add the "enhanced" artifact to your pom:

```xml
<dependencies>
		<dependency>
			<groupId>group.artifact-enhanced</groupId>
			<artifactId>artifact-id-enhanced</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
</dependencies>
```

###Parameters###

| Parameter        | Default Value | Description |
|------------------|---------------|-------------|
| closed-atifact   |      NULL     | The artifact to enhance |
| exclusion        |      NULL     | A String representing the name of one class that you want to exclude|
| artifact-suffix  |    enhanced   | The resultant artifact's suffix on local repository|


### Sample Project ###

You can use the sample and closed-jar-sample projects to lear more about the plugin execution.



