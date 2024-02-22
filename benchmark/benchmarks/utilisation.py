from schemas.schemas import DeploymentBenchmark


async def observe_utilisation(deployment: dict, utilisation: DeploymentBenchmark):

    rm_operator = RmOperator(function_deployment.rm_base_url, function_deployment.token)
    deployment = await rm_operator.wait_for_deployment_created(deployment['deployment_id'])
    if deployment is None:
        return
