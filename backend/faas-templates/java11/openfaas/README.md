## Template: java11-apollo-rm

The Java11-Apollo-RM template uses gradle as a build system.

Gradle version: 7.5.1

### Structure

There are three projects which make up a single gradle build:

- function - (Library) your function code as a developer, you will only ever see this folder
- entrypoint - (App) Vert.x HTTP server
- model - Interfaces, Exceptions

### Handler

The handler is written in the `./src/main/java/org/apollorm/model/function/Main.java` folder. This
class must implement the org.apollorm.model.FunctionHandler interface.

### External dependencies

External dependencies can be specified in ./build.gradle in the normal way using jcenter, a local JAR or some other remote repository.

### Deployment yaml file sample:

```yaml
provider:
  name: faas
  gateway: http://localhost:8080
functions:
  hello-vert-x:
    lang: java11-apollo-rm
    handler: ./function
    image: docker-user/function_java11:latest
```
### Reference

This template is an extension/modification of the official 
[java11-vert-x template](https://github.com/openfaas/templates/tree/master/template/java11-vert-x).
