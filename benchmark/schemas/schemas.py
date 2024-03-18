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


class SSHFailure(BaseModel):
    resource_id: int
    host: str
    user: str
    pwd: str
    command: str


class K8sFailure(BaseModel):
    resource_id: int
    namespace: str
    node: str
    command: str
    failure_duration_seconds: int
    kube_config: dict


class DBFailure(BaseModel):
    resource_id: int
    ensemble_slo_id: int
    failure_value: float
    original_value: float
    failure_duration_seconds: int
    dbname: str
    user: str
    password: str
    host: str
    port: int


class AlertingBenchmark(Benchmark):
    failure_window_low: int
    failure_window_high: int
    inject_ssh_failure: SSHFailure | None
    inject_k8s_failure: K8sFailure | None
    inject_db_failure: DBFailure | None
    deployments: list[CreateDeployment]


class DeploymentBenchmark(Benchmark):
    request_body: CreateDeployment


class FunctionDeploymentBenchmark(DeploymentBenchmark):
    concurrency: int
    invoke_body: list | dict | int | float | bool | str


class ServiceDeploymentBenchmark(DeploymentBenchmark):
    kube_config: dict
    namespace: str
    image: str
    replicas: int
    cpu: float
    memory: int
    container_port: int
    svc_port: int
    external_ip: str | None


class AlertMessage(BaseModel):
    type: str
    resource_id: int
    metric: str
    value: str | int | float | bool
    timestamp: int


class UtilisationRequest(Benchmark):
    pod_name: str
    util_interval_seconds: int | float
    util_count: int
    deployments: list[CreateDeployment]
    kube_config: dict
