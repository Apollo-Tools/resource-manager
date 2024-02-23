import logging
import time

from paramiko import SSHClient, AutoAddPolicy

from schemas.schemas import AlertingBenchmark

logger = logging.getLogger('uvicorn.info')


def execute_failure_injection(alerting: AlertingBenchmark):
    client = SSHClient()
    client.load_system_host_keys()
    client.set_missing_host_key_policy(AutoAddPolicy())
    client.connect(alerting.inject_ssh_failure.host, username=alerting.inject_ssh_failure.user,
                   password=alerting.inject_ssh_failure.pwd)

    start_time = time.time()
    stdin, stdout, stderr = client.exec_command(alerting.inject_ssh_failure.command)

    stdout.read()
    stderr.read()
    end_time = time.time()

    stdin.close()
    stdout.close()
    stderr.close()
    client.close()
    return {'start': start_time * 1000, 'end': end_time * 1000}
