import json
from .cloud_function import main

# OpenFaaS
def handle(req):
    print(req)
    input_body = json.loads(req)
    res = main(input_body)
    return {"statusCode": 200, "body": res}
