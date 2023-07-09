## Java Template

The Java templates use gradle as a build system.

Gradle version: 7.5.1

### Structure

There are three projects which make up a single gradle build:

- function - This is the code of the function implementation. The files inside this folder are the
  only ones that should be modified by function developers. A template of the function project can 
  be found [here](./apollorm/function). For development, it is suggested to use copy the whole 
  [apollorm](./apollorm) directory.
- entrypoint - Each platform implements a different entrypoint. The function developer does not 
  need to know any implementation details of this project but if you are interested implementation 
  details can be found in the directories of the corresponding platforms.
- model - Interfaces, Exceptions that are used by the entrypoint and function. Modifications of
  these classes don't apply to the final deployment!

### Handler
The handler is implemented in the `Main.java` of the function project. This class must implement
the org.apollorm.model.FunctionHandler interface for a successful deployment.

### External dependencies
External dependencies must be specified in the `gradle.build.` that is located inside the function
directory.

### Deployment with the RM
1. Implement your code within the function project.
2. `gradle buildRMZip`
3. Create a new function (RM) using the newly created 
   [function.zip (function/build/function.zip)](./apollorm/function/build/function.zip).
4. The function is ready for deployment.

### Reference
This template is an extension/modification of the official OpenFaaS
[java11-vert-x template](https://github.com/openfaas/templates/tree/master/template/java11-vert-x).
