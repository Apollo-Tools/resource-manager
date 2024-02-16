# Copyright (c) Alex Ellis 2017. All rights reserved.
# Licensed under the MIT license. See LICENSE file in the project root for full license information.
# Source: https://github.com/openfaas/python-flask-template/tree/master/template/python3-flask-debian
import json
from flask import Flask, request
from waitress import serve
import os
import time
from enum import Enum
from function import openfaas

app = Flask(__name__)

class RequestType(str, Enum):
    """
    The request type is used to determine structure of the response body. If the request type is
    equal to 'rm', additional measurement data is added to the response body.
    """
    CLIENT = 'client'
    RM = 'rm'

# distutils.util.strtobool() can throw an exception
def is_true(val):
    return len(val) > 0 and val.lower() == "true" or val == "1"

@app.before_request
def fix_transfer_encoding():
    """
    Sets the "wsgi.input_terminated" environment flag, thus enabling
    Werkzeug to pass chunked requests as streams.  The gunicorn server
    should set this, but it's not yet been implemented.
    """
    transfer_encoding = request.headers.get("Transfer-Encoding", None)
    if transfer_encoding == u"chunked":
        request.environ["wsgi.input_terminated"] = True

@app.route("/", defaults={"path": ""}, methods=["POST", "GET"])
@app.route("/<path:path>", methods=["POST", "GET"])
def main_route(path):
    start = time.time()
    add_monitoring_data = False
    # check request type
    if 'apollo-request-type' in request.headers and request.headers['apollo-request-type'] == RequestType.RM:
        add_monitoring_data = True
    raw_body = os.getenv("RAW_BODY", "false")
    as_text = True
    if is_true(raw_body):
        as_text = False
    # execute function
    body = openfaas.handle(request.get_data(as_text=as_text))
    # return result
    if add_monitoring_data:
        end = time.time()
        body = {"monitoring_data": {"execution_time_ms": (end-start) * 1000, "start_timestamp": int(start * 1000)},
                "body": json.dumps(body)}
    return body

if __name__ == '__main__':
    serve(app, host='0.0.0.0', port=5000)
