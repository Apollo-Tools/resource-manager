UPDATE runtime
SET template_path ='./runtime/python/cloud_function.py'
WHERE runtime.name = 'python3.8';