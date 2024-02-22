import csv
import logging
import time

import asyncio
import dateutil.parser as dp

from schemas.schemas import UtilisationRequest, CreateDeployment
from shared.kube_operator import KubeOperator
from shared.rm_operator import RmOperator

logger = logging.getLogger('uvicorn.info')


def observe_utilisation(utilisation: UtilisationRequest):
    kube_operator = KubeOperator()
    metrics = {}
    logger.info(f"observe utilisation, utilisation {utilisation.benchmark_id}_util")
    for i in range(0, utilisation.util_count):
        metrics[i] = kube_operator.get_metrics_for_pod(utilisation.pod_name)
        time.sleep(utilisation.util_interval_seconds)

    logger.info(f"write file content, utilisation {utilisation.benchmark_id}_util")
    with (open(f"{utilisation.benchmark_id}_util.csv", mode="w", newline='') as output):
        writer = csv.writer(output)
        writer.writerow(['id', 'timestamp', 'cpu', 'memory'])
        for i in range(0, utilisation.util_count):
            metric = metrics[i]
            writer.writerow([i, int(dp.parse(metric['timestamp']).timestamp() * 1000), metric['cpu'], metric['memory']])


async def apply_deployments(utilisation: UtilisationRequest):
    result = {}
    for i in range(0, utilisation.count):
        logger.info(f"deploy deployments, try {i}")
        tasks = []
        for idx, deployment in enumerate(utilisation.deployments):
            tasks.append(apply_deployment(utilisation.rm_base_url, utilisation.token, deployment, idx))
        result[i] = await asyncio.gather(*tasks)

    logger.info(f"write file content, utilisation deployments {utilisation.benchmark_id}")
    with (open(f"{utilisation.benchmark_id}.csv", mode="w", newline='') as output):
        writer = csv.writer(output)
        writer.writerow(['id', 'request_idx', 'deployment_id', 'start', 'deployed', 'terminate', 'terminated',
                         'function_deployments',
                         'service_deployments'])
        for i in range(0, utilisation.count):
            for entry in result[i]:
                if entry['start'] == 'error':
                    writer.writerow([i, entry['idx'], 'error', 'error', 'error', 'error', 'error', 'error'])
                else:
                    writer.writerow([i, entry['idx'], entry['deployment']['deployment_id'], int(entry['start']),
                                     int(entry['deployed']), int(entry['terminate']), int(entry['terminated']),
                                     len(entry['deployment']['function_resources']),
                                     len(entry['deployment']['service_resources'])])


async def apply_deployment(rm_base_url: str, token: str, create_deployment: CreateDeployment, idx: int):
    rm_operator = RmOperator(rm_base_url, token)
    start = time.time()
    deployment = await rm_operator.create_deployment(create_deployment)
    if deployment is None:
        return {'start': 'error', 'deployed': 'error', 'terminate': 'error', 'terminated': 'error',
                'deployment': 'error', 'idx': idx}
    deployment = await rm_operator.wait_for_deployment_created(deployment['deployment_id'])
    if deployment is None:
        return {'start': 'error', 'deployed': 'error', 'terminate': 'error', 'terminated': 'error',
                'deployment': 'error', 'idx': idx}
    deployed = deployment['finished_at']
    await asyncio.sleep(5)
    logger.info(f"cancel deployment, deployment {deployment['deployment_id']}")
    terminate = time.time()
    await rm_operator.cancel_deployment(deployment['deployment_id'])
    terminated = await rm_operator.wait_for_deployment_terminated(deployment['deployment_id'])
    return {'start': start * 1000, 'deployed': deployed * 1000, 'terminate': terminate * 1000,
            'terminated': terminated * 1000, 'deployment': deployment, 'idx': idx}
