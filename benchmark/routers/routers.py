from fastapi import APIRouter, BackgroundTasks
from fastapi.responses import FileResponse, JSONResponse

from benchmarks import function_execution_overhead
from schemas.schemas import AlertingBenchmark, DeploymentBenchmark, AlertMessage, FunctionDeploymentBenchmark
from shared.kube_operator import KubeOperator
from shared.rm_operator import RmOperator

router = APIRouter()


@router.post("/benchmarks/reaction-time")
async def benchmark_reaction_time(alerting: AlertingBenchmark):
    return {"message": f"Started benchmark {alerting}"}


@router.post("/benchmarks/function-deployment")
async def benchmark_function(function_deployment: FunctionDeploymentBenchmark, background_tasks: BackgroundTasks):
    rm_operator = RmOperator(function_deployment.rm_base_url, function_deployment.token)
    deployment = await rm_operator.create_deployment(function_deployment.request_body)
    if deployment is None:
        return JSONResponse(status_code=400, content="failed to create deployment")

    background_tasks.add_task(function_execution_overhead.observe_function_execution_overhead, deployment,
                              function_deployment)
    return {"message": f"Started benchmark {function_deployment.benchmark_id}"}


@router.post("/benchmarks/utilisation")
async def benchmark_utilisation(utilisation: DeploymentBenchmark, background_tasks: BackgroundTasks):
    rm_operator = RmOperator(utilisation.rm_base_url, utilisation.token)
    kube_operator = KubeOperator()
    kube_operator.get_metrics_for_pod('nginx-85-2024-02-22-04-01-19-649d7dfc45-z9mw8')
    # TODO: continue
    #deployment = await rm_operator.create_deployment(utilisation.request_body)
    #if deployment is None:
    #    return JSONResponse(status_code=400, content="failed to create deployment")
    return {"message": f"Started benchmark {utilisation}"}


@router.post("/alerts/receive/{benchmark_id}", status_code=204)
async def receive_alert(benchmark_id: str, alert_message: AlertMessage):
    return {"message": f"Started benchmark {benchmark_id}, {alert_message}"}


@router.get("/benchmarks/result/{benchmark_id}", response_class=FileResponse)
async def get_result(benchmark_id: str):
    return f"./{benchmark_id}.csv"
