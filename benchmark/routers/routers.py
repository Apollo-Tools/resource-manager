from fastapi import APIRouter

from schemas.schemas import AlertingBenchmark, DeploymentBenchmark, AlertMessage

router = APIRouter()


@router.post("/benchmark/reaction-time")
async def benchmark_reaction_time(alerting: AlertingBenchmark):
    return {"message": f"Started benchmark {alerting}"}


@router.post("/benchmark/function-deployment")
async def benchmark_function(function_deployment: DeploymentBenchmark):
    return {"message": f"Started benchmark {function_deployment}"}


@router.post("/benchmark/utilisation")
async def benchmark_utilisation(utilisation: DeploymentBenchmark):
    return {"message": f"Started benchmark {utilisation}"}


@router.post("/alerting/receive/{benchmark_id}", status_code=204)
async def receive_alert(benchmark_id: str, alert_message: AlertMessage):
    return {"message": f"Started benchmark {benchmark_id}, {alert_message}"}
