import csv
import logging
import random
import time

import asyncio

from schemas.schemas import AlertingBenchmark, CreateDeployment
from shared import ssh_operator
from shared.kube_operator import KubeOperator
from shared.rm_operator import RmOperator

logger = logging.getLogger('uvicorn.info')


async def observe_deployment_reaction_time(alerting: AlertingBenchmark):
    logger.info(f"deploy resources, alerting {alerting.benchmark_id}")
    tasks = []
    for idx, deployment in enumerate(alerting.deployments):
        tasks.append(apply_deployment(alerting.rm_base_url, alerting.token, deployment))
    deployments = await asyncio.gather(*tasks)

    timestamps = []
    if None in deployments:
        timestamps.append({'start': -1, 'end': -1})
    else:
        for i in range(0, alerting.count):
            logger.info(f"inject failure {i}, alerting {alerting.benchmark_id}")
            if alerting.inject_k8s_failure:
                k8s_operator = KubeOperator()
                result = k8s_operator.create_stress_deployment(alerting.inject_k8s_failure.node,
                                                               alerting.inject_k8s_failure.namespace,
                                                               alerting.inject_k8s_failure.command)
                await asyncio.sleep(alerting.failure_duration_seconds)
                k8s_operator.terminate_stress_deployment(result['name'], alerting.inject_k8s_failure.namespace)
                timestamps.append({'start': result['start_time'], 'end': time.time() * 1000})
            else:
                timestamps.append(ssh_operator.execute_failure_injection(alerting))
            if i + 1 < alerting.count:
                await asyncio.sleep(random.randint(alerting.failure_window_low,alerting.failure_window_high))

    logger.info(f"terminate resources, alerting {alerting.benchmark_id}")
    tasks = []
    for deployment in deployments:
        if deployment:
            tasks.append(cancel_deployment(alerting.rm_base_url, alerting.token, deployment['deployment_id']))
    await asyncio.gather(*tasks)

    logger.info(f"write file content, alerting {alerting.benchmark_id}")
    with open(f"{alerting.benchmark_id}.csv", mode="w", newline='') as output:
        writer = csv.writer(output)
        writer.writerow(['id', 'start', 'end'])
        for idx, timestamp in enumerate(timestamps):
            writer.writerow([idx, int(timestamp['start']), int(timestamp['end'])])


async def apply_deployment(rm_base_url: str, token: str, create_deployment: CreateDeployment):
    rm_operator = RmOperator(rm_base_url, token)
    deployment = await rm_operator.create_deployment(create_deployment)
    if deployment is None:
        return None
    deployment = await rm_operator.wait_for_deployment_created(deployment['deployment_id'])
    if deployment is None:
        return None
    return deployment


async def cancel_deployment(rm_base_url: str, token: str, deployment_id: int):
    rm_operator = RmOperator(rm_base_url, token)
    await rm_operator.cancel_deployment(deployment_id)
    await rm_operator.wait_for_deployment_terminated(deployment_id)