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
    kube_operator = KubeOperator(utilisation.kube_config)
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
        merged_results = []
        tasks = []
        for idx, deployment in enumerate(utilisation.deployments):
            tasks.append(apply_deployment(utilisation.rm_base_url, utilisation.token, deployment, idx))
        result_deployments = await asyncio.gather(*tasks)
        await asyncio.sleep(5)
        tasks = []
        for idx, deployment in enumerate(utilisation.deployments):
            tasks.append(terminate_deployment(utilisation.rm_base_url, utilisation.token,
                                              result_deployments[idx]['deployment']['deployment_id'], idx))
        result_terminations = await asyncio.gather(*tasks)
        for j in range(0, len(result_deployments)):
            merged_results.append(result_deployments[j] | result_terminations[j])
        result[i] = merged_results

    logger.info(f"write file content, utilisation deployments {utilisation.benchmark_id}")
    with (open(f"{utilisation.benchmark_id}.csv", mode="w", newline='') as output):
        writer = csv.writer(output)
        writer.writerow(['id', 'request_idx', 'deployment_id', 'start', 'start_response', 'deployed', 'terminate',
                         'terminate_response', 'terminated', 'function_deployments', 'service_deployments'])
        for i in range(0, utilisation.count):
            for entry in result[i]:
                if entry['start'] == 'error':
                    writer.writerow([i, entry['idx'], 'error', 'error', 'error', 'error', 'error', 'error', 'error',
                                     'error'])
                else:
                    writer.writerow([i, entry['idx'], entry['deployment']['deployment_id'], int(entry['start']),
                                     int(entry['start_response']), int(entry['deployed']), int(entry['terminate']),
                                     int(entry['terminate_response']), int(entry['terminated']),
                                     len(entry['deployment']['function_resources']),
                                     len(entry['deployment']['service_resources'])])


async def apply_deployment(rm_base_url: str, token: str, create_deployment: CreateDeployment, idx: int):
    rm_operator = RmOperator(rm_base_url, token)
    start = time.time()
    deployment = await rm_operator.create_deployment(create_deployment)
    start_response = time.time()
    if deployment is None:
        return {'start': 'error', 'started': 'error', 'deployed': 'error', 'terminate': 'error', 'terminated': 'error',
                'deployment': 'error', 'idx': idx}
    deployment = await rm_operator.wait_for_deployment_created(deployment['deployment_id'])
    deployed = deployment['finished_at']
    if deployment is None:
        return {'start': 'error', 'started': 'error', 'deployed': 'error', 'terminate': 'error', 'terminated': 'error',
                'deployment': 'error', 'idx': idx}
    return {'start': start * 1000, 'start_response': start_response * 1000, 'deployed': deployed,
            'deployment': deployment, 'idx': idx}


async def terminate_deployment(rm_base_url: str, token: str, deployment_id: int, idx: int):
    rm_operator = RmOperator(rm_base_url, token)
    terminate = time.time()
    await rm_operator.cancel_deployment(deployment_id)
    terminate_response = time.time()
    terminated = await rm_operator.wait_for_deployment_terminated(deployment_id)
    return {'terminate': terminate * 1000, 'terminate_response': terminate_response * 1000,
            'terminated': terminated, 'idx': idx}
