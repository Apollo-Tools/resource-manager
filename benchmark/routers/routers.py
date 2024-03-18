import os
from concurrent.futures import ThreadPoolExecutor

from fastapi import APIRouter, BackgroundTasks
from fastapi.responses import FileResponse, JSONResponse

from benchmarks import function_execution_overhead, utilisation, alerts, reaction_time, service_execution_overhead
from schemas.schemas import AlertingBenchmark, AlertMessage, FunctionDeploymentBenchmark, \
    UtilisationRequest, ServiceDeploymentBenchmark
from shared.rm_operator import RmOperator

router = APIRouter()


@router.post("/benchmarks/reaction-time")
async def benchmark_reaction_time(alerting: AlertingBenchmark, background_tasks: BackgroundTasks):
    background_tasks.add_task(reaction_time.observe_deployment_reaction_time, alerting)
    return {"message": f"Started reaction time benchmark, result={alerting.benchmark_id}, "
                       f"alert_result={alerting.benchmark_id}_alert"}


@router.post("/benchmarks/function-deployment")
async def benchmark_function(function_deployment: FunctionDeploymentBenchmark, background_tasks: BackgroundTasks):
    rm_operator = RmOperator(function_deployment.rm_base_url, function_deployment.token)
    deployment = await rm_operator.create_deployment(function_deployment.request_body)
    if deployment is None:
        return JSONResponse(status_code=400, content="failed to create deployment")

    background_tasks.add_task(function_execution_overhead.observe_function_execution_overhead, deployment,
                              function_deployment)
    return {"message": f"Started function benchmark, result={function_deployment.benchmark_id}"}


@router.post("/benchmarks/service-deployment")
async def benchmark_function(service_deployment: ServiceDeploymentBenchmark, background_tasks: BackgroundTasks):
    rm_operator = RmOperator(service_deployment.rm_base_url, service_deployment.token)
    deployment = await rm_operator.create_deployment(service_deployment.request_body)
    if deployment is None:
        return JSONResponse(status_code=400, content="failed to create deployment")

    background_tasks.add_task(service_execution_overhead.observe_service_execution_overhead, deployment,
                              service_deployment)
    return {"message": f"Started service benchmark, result={service_deployment.benchmark_id}"}


async def observe_util_in_bg(request: UtilisationRequest):
    executor = ThreadPoolExecutor(max_workers=1)
    executor.submit(utilisation.observe_utilisation, request)


@router.post("/benchmarks/utilisation")
async def monitor_utilisation(request: UtilisationRequest, background_tasks: BackgroundTasks):
    background_tasks.add_task(observe_util_in_bg, request)
    background_tasks.add_task(utilisation.apply_deployments, request)
    return {"message": f"Started utilisation monitoring, result={request.benchmark_id}, util_result="
                       f"{request.benchmark_id}_util"}


@router.post("/alerts/receive/{benchmark_id}", status_code=204)
def receive_alert(benchmark_id: str, alert_message: AlertMessage):
    alerts.process_alert(benchmark_id, alert_message)
    return {}


@router.get("/benchmarks/result/{benchmark_id}", response_class=FileResponse)
async def get_result(benchmark_id: str):
    return f"./{benchmark_id}.csv"


@router.delete("/benchmarks/result/{benchmark_id}")
async def delete_result(benchmark_id: str):
    escaped_file = (benchmark_id.replace("/", "")
                    .replace("\\", "")
                    .replace(".", ""))
    path = f"./{escaped_file}.csv"
    if os.path.exists(path):
        os.remove(path)
    return {"message": "done"}
