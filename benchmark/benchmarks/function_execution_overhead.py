import csv
import logging

from schemas.schemas import FunctionDeploymentBenchmark
from shared.rm_operator import RmOperator

logger = logging.getLogger('uvicorn.info')


async def observe_function_execution_overhead(deployment: dict, function_deployment: FunctionDeploymentBenchmark):
    rm_operator = RmOperator(function_deployment.rm_base_url, function_deployment.token)
    deployment = await rm_operator.wait_for_deployment_created(deployment['deployment_id'])
    if deployment is None:
        return

    logger.info(f"warmup, deployment {deployment['deployment_id']}")
    await rm_operator.trigger_function_deployment(deployment['function_resources'][0]['direct_trigger_url'],
                                                  10, function_deployment.invoke_body, True)
    direct_times = {}
    indirect_times = {}
    for i in range(0, function_deployment.count):
        logger.info(f"direct invocation {i}, deployment {deployment['deployment_id']}")
        direct_times[i] = \
            await rm_operator.trigger_function_deployment(deployment['function_resources'][0]['direct_trigger_url'],
                                                          function_deployment.concurrency, function_deployment.invoke_body, True)

        logger.info(f"rm invocation {i}, deployment {deployment['deployment_id']}")
        indirect_times[i] = \
            await rm_operator.trigger_function_deployment(deployment['function_resources'][0]['rm_trigger_url'],
                                                          function_deployment.concurrency, function_deployment.invoke_body, False)

    logger.info(f"cancel deployment, deployment {deployment['deployment_id']}")
    await rm_operator.cancel_deployment(deployment['deployment_id'])

    logger.info(f"write file content, function execution {function_deployment.benchmark_id}")
    with open(f"{function_deployment.benchmark_id}.csv", mode="w", newline='') as output:
        writer = csv.writer(output)
        writer.writerow(['test-run', 'type', 'round trip time (ms)'])
        for i in range(0, function_deployment.count):
            for time in direct_times[i]:
                writer.writerow([i, 'direct', str(time)])
            for time in indirect_times[i]:
                writer.writerow([i, 'rm', str(time)])
