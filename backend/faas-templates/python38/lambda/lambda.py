import json
from main import main

########## Boilerplate wrapping code #############
def handler(event, context):
    # read in the args from the POST object
    if 'body' in event and isinstance(event['body'], str):
        input_body = json.loads(event['body'])
    else:
        input_body = event
    res = main(input_body)
    return {"statusCode": 200, "body": res}
##################################################
