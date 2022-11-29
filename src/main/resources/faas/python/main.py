import json
from cloud_function import main


##################################################
########## Boilerplate wrapping code #############
##################################################

# TODO: Test IBM
# TODO: Add Google Cloud, Azure
# IBM wrapper
#def main(args):
#    res = cloud_function(args)
#    return res


def handler(event, context):
    # read in the args from the POST object
    if 'body' in event:
        input = json.loads(event['body'])
    else:
        input = event
    res = main(input)
    return {"statusCode": 200, "body": res}


##################################################
##################################################



# Docker wrapper
if __name__ == "__main__":
    # read the json
    json_input = json.loads(open("jsonInput.json").read())
    result = cloud_function(json_input)

    # write to std out
    print(json.dumps(result))