"""
This is a 'Hello World' function which returns the string ``Hello, world``.
"""
__author__ = "matthi-g"
if __package__ is None or __package__ == '':
    import file
else:
    from . import file


def main(json_input):
    res = {
        "result": file.get_hello()
    }
    return res


# For local development
if __name__ == "__main__":
    response = main("")
    print(response)
