# Import from other files https://stackoverflow.com/a/49480246
if __package__ is None or __package__ == '':
    from subscript import sum
else:
    from .subscript import sum

#########Function implementation#############
def main(json_input):
    input1 = json_input["input1"]

    # Processing
    sum_result = sum(input1, 10)

    # return the result
    res = {
        "input1": sum_result
    }
    return res


# For local development
if __name__ == '__main__':
    response = main({
        'input1': 25
    })
    print(response)
