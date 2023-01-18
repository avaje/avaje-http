# openapi-maven-plugin
Maven plugin for OpenAPI generation (swagger)

```xml

<plugin>
  <groupId>io.avaje</groupId>
  <artifactId>openapi-maven-plugin</artifactId>
  <version>1.0</version>
  <executions>
    <execution>
      <id>main</id>
      <phase>process-classes</phase>
      <goals>
        <goal>openapi</goal>
      </goals>
    </execution>
  </executions>
</plugin>

```
