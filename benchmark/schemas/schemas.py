from pydantic import BaseModel


class FunctionResourcePair(BaseModel):
    resource_id: int
    function_id: int


class ServiceResourcePair(BaseModel):
    resource_id: int
    service_id: int


class DockerCredentials(BaseModel):
    registry: str
    username: str
    access_token: str


class Credentials(BaseModel):
    docker_credentials: DockerCredentials


class Validation(BaseModel):
    ensemble_id: int
    alert_notification_url: str


class CreateDeployment(BaseModel):
    function_resources: list[FunctionResourcePair]
    service_resources: list[ServiceResourcePair]
    credentials: Credentials
    lock_resources: list
    validation: Validation


class Benchmark(BaseModel):
    benchmark_id: str
    token: str
    rm_base_url: str
    count: int


class SSHUrl(BaseModel):
    resource_id: int
    url: str
    pwd: str


class AlertingBenchmark(Benchmark):
    request_body: CreateDeployment
    ssh_urls: list[SSHUrl]


class DeploymentBenchmark(Benchmark):
    request_body: CreateDeployment


class FunctionDeploymentBenchmark(DeploymentBenchmark):
    concurrency: int
    invoke_body: list | dict | int | float | bool | str


class AlertMessage(BaseModel):
    type: str
    resource_id: int
    metric: str
    value: str | int | float | bool
    timestamp: int
