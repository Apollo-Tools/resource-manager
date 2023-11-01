"""
This is a function to test the different return values.
"""
__author__ = "matthi-g"
#########Function implementation#############
def main(json_input):
    valuetype = json_input["valuetype"]
    
    # Processing
    if valuetype == "array":
        return [1, 2, 5]
    elif valuetype == "number":
        return 5
    elif valuetype == "string":
        return "string"
    elif valuetype == "object":
        return {"valuetype": valuetype}
    else:
        return True

