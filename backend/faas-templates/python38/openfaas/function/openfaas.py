import json
from .main import main

########## Boilerplate wrapping code #############
# OpenFaaS
def handle(req):
    input_body = json.loads(req)
    res = main(input_body)
    return res
##################################################
