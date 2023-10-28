import json
import time
from enum import Enum
from main import main

class RequestType(str, Enum):
    """
    The request type is used to determine structure of the response body. If the request type is
    equal to 'rm', additional measurement data is added to the response body.
    """
    CLIENT = 'client'
    RM = 'rm'


########## Boilerplate wrapping code #############
def handler(event, context):
    start = time.time()
    add_monitoring_data = False
    # check request type
    headers = event['headers']
    if 'apollo-request-type' in headers and headers['apollo-request-type'] == RequestType.RM:
        add_monitoring_data = True
    # read in the args from the POST object
    if 'body' in event and isinstance(event['body'], str):
        input_body = json.loads(event['body'])
    else:
        input_body = event
    # execute function
    body = main(input_body)
    # return result
    if add_monitoring_data:
        end = time.time()
        body = {"monitoring_data": {"execution_time_ms": (end-start) * 1000, "start_timestamp": int(start * 1000)},
                "body": body}
    return {"statusCode": 200, "body": body}
##################################################

