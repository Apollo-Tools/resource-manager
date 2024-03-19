import asyncio
import csv
import logging

from schemas.schemas import ServiceDeploymentBenchmark
from shared.ServicdeDeploymentMethod import ServiceDeploymentMethod
from shared.kube_operator import KubeOperator
from shared.rm_operator import RmOperator

logger = logging.getLogger('uvicorn.info')


async def observe_service_execution_overhead(deployment: dict, service_deployment: ServiceDeploymentBenchmark):
    rm_operator = RmOperator(service_deployment.rm_base_url, service_deployment.token)
    k8s_operator = KubeOperator(service_deployment.kube_config)
    deployment = await rm_operator.wait_for_deployment_created(deployment['deployment_id'])
    if deployment is None:
        return

    logger.info(f"warmup, deployment {deployment['deployment_id']}")
    await rm_operator.startup_stop_service_deployments([deployment['service_resources'][0]['resource_deployment_id']],
                                                       ServiceDeploymentMethod.STARTUP)
    await rm_operator.startup_stop_service_deployments([deployment['service_resources'][0]['resource_deployment_id']],
                                                       ServiceDeploymentMethod.STOP)

    rm_start_times = {}
    rm_stop_times = {}
    k8s_start_times = {}
    k8s_stop_times = {}
    for i in range(0, service_deployment.count):
        logger.info(f"RM service startup {i}, deployment {deployment['deployment_id']}")
        ids = [sr['resource_deployment_id'] for sr in deployment['service_resources']]
        rm_start_times[i] = await rm_operator.startup_stop_service_deployments(ids, ServiceDeploymentMethod.STARTUP)
        await asyncio.sleep(5)
        rm_stop_times[i] = await rm_operator.startup_stop_service_deployments(ids, ServiceDeploymentMethod.STOP)

    for i in range(0, service_deployment.count):
        logger.info(f"K8S service startup {i}, deployment {deployment['deployment_id']}")
        k8s_start_times[i] = await k8s_operator.start_service_deployments(i, service_deployment)
        await asyncio.sleep(5)
        k8s_stop_times[i] = await k8s_operator.stop_service_deployments(i, service_deployment)

    logger.info(f"cancel deployment, deployment {deployment['deployment_id']}")
    await rm_operator.cancel_deployment(deployment['deployment_id'])

    logger.info(f"write file content, service startup {service_deployment.benchmark_id}")
    with open(f"{service_deployment.benchmark_id}.csv", mode="w", newline='') as output:
        writer = csv.writer(output)
        writer.writerow(['test-run', 'system', 'type', 'round trip time (ms)'])
        for i in range(0, service_deployment.count):
            for time in rm_start_times[i]:
                writer.writerow([i, 'rm', 'start', str(time)])
            for time in rm_stop_times[i]:
                writer.writerow([i, 'rm', 'stop', str(time)])
            for time in k8s_start_times[i]:
                writer.writerow([i, 'k8s', 'start', str(time)])
            for time in k8s_stop_times[i]:
                writer.writerow([i, 'k8s', 'stop', str(time)])
