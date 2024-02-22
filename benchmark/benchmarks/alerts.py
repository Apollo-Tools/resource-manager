import csv
import logging
import os
import threading

from schemas.schemas import AlertMessage

logger = logging.getLogger('uvicorn.info')

file_lock = threading.Lock()


def process_alert(benchmark_id: str, alert_message: AlertMessage):
    logger.info(f"write file content, alert {benchmark_id}_alert")
    with file_lock:
        path = f"{benchmark_id}_alert.csv"
        with open(path, mode="a", newline='') as output:
            writer = csv.writer(output)
            if os.stat(path).st_size == 0:
                writer.writerow(['timestamp', 'resource', 'metric', 'value'])
            writer.writerow([alert_message.timestamp, alert_message.resource_id, alert_message.metric,
                             alert_message.value])
