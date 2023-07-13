## Python Template

### Structure

A working example that follows the following rules can be found at [here](./apollorm).

Python functions are required to contain at least the two following files:
- main.py - This is the main entry point of the function. It **must** define a function that is
  called *main* and has exactly one input parameter. The input parameter is the deserialized request
  body. The return value of the function must be a **json serializable** dictionary.
- requirements.txt - This file contains all external dependencies of the function. For more 
  information on the file format see this 
  [documentation](https://pip.pypa.io/en/stable/reference/requirements-file-format/). 

Any additional files may be added as needed. For imports of subscripts that you want to use make
sure that you import it similar to the example code:
```
if __package__ is None or __package__ == '':
    from subscript import sum
else:
    from .subscript import sum
```

### Known limitations with external dependencies
- requests - Requiring a version that is equal or higher than 2.29.0 might return an OpenSSL error.
- numpy - The OpenFaaS template includes numpy 1.24.3 by default. Make sure to use this version in
  your requirements.txt. Using a different version might introduce a very long deployment time 
  (>20 minutes).

### Deployment with the RM
1. Implement your code following the above rules.
2. Package all source code files as zip file.
3. Create a new function (RM) using the newly created zip file.
4. The function is ready for deployment.

### Reference
This template is an extension/modification of the official OpenFaaS
[python3-flask-debian template](https://github.com/openfaas/python-flask-template).
