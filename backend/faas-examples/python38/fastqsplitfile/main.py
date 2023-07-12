"""
This function expects a FASTQ sequence file that is located at an AWS S3
bucket and the size of the subsequences and then uploads the subsequences into the same bucket.
The response contains the file names of the uploaded subsequences.

Note: This function needs to be modified to work outside of AWS Lambda,
e.g. provide credentials in request body.
"""
__author__ = "matthi-g"

import boto3
import time
import os
import shutil
if __package__ is None or __package__ == '':
    from fastqSplit import split
else:
    from .fastqSplit import split

temp_path = '/tmp'


def download(s3, bucket, src, dst):
    s3.download_file(bucket, src, dst)


def upload(s3, bucket, src, dst):
    s3.upload_file(src, bucket, dst)


def main(json_input):
    start = time.time()

    # Prepare input
    bucket = json_input["bucket"]
    seqs_per_file = json_input["seqsPerFile"]
    seqs = json_input["seqs"]

    # Clear tmp directory
    for root, dirs, files in os.walk(temp_path):
        for f in files:
            os.unlink(os.path.join(root, f))
        for d in dirs:
            shutil.rmtree(os.path.join(root, d))

    # Download files
    s3 = boto3.client('s3')
    download(s3, bucket, seqs, f'{temp_path}/{seqs}')
    input_size = os.stat(f'{temp_path}/{seqs}').st_size

    # Actual execution
    files = split(seqs_per_file, f'{temp_path}/{seqs}', temp_path)

    # Upload files
    for file in files:
        upload(s3, bucket, f'{temp_path}/{file}', file)

    # Prepare output
    result = {
        "input_size_mb": input_size / 1000000,
        "splitted": files,
    }

    end = time.time()
    result["actual_ms"] = (end-start)*1000

    return result


# For local development
if __name__ == '__main__':
    response = main({
        'bucket': '<BUCKET-NAME>',
        'seqsPerFile': 12000,
        'seqs': '<FILE_NAME>'
    })
    print(response)
