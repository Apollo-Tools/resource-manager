import logging
from decimal import Decimal

from kubernetes.client import CustomObjectsApi
from kubernetes.config import load_kube_config
from kubernetes.utils import parse_quantity

logger = logging.getLogger('uvicorn.info')


class KubeOperator:
    def __init__(self) -> None:
        load_kube_config()

    def get_metrics_for_pod(self, pod_name: str) -> dict[str, str | Decimal]:
        k8s_client = CustomObjectsApi()
        pods = k8s_client.list_cluster_custom_object('metrics.k8s.io', 'v1beta1', 'pods')
        for pod in pods['items']:
            if pod['metadata']['name'] == pod_name:
                container = pod['containers'][0]
                cpu = parse_quantity(container['usage']['cpu'])
                memory = parse_quantity(container['usage']['memory'])
                return {'timestamp': pod['timestamp'], 'cpu': cpu, 'memory': memory}
        return {'cpu': Decimal(-1), 'memory': Decimal(-1)}
