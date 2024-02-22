import logging

from kubernetes.client import CustomObjectsApi
from kubernetes.config import load_kube_config
from kubernetes.utils import parse_quantity

logger = logging.getLogger('uvicorn.error')


class KubeOperator:
    def __init__(self) -> None:
        load_kube_config()


    def get_metrics_for_pod(self, pod_name: str):
        k8s_client = CustomObjectsApi()
        pods = k8s_client.list_cluster_custom_object('metrics.k8s.io', 'v1beta1', 'pods')
        for pod in pods['items']:
            if pod['metadata']['name'] == pod_name:
                container = pod['containers'][0]
                logger.info(f"CPU: {parse_quantity(container['usage']['cpu'])} "
                            f"Memory: {parse_quantity(container['usage']['memory'])}")
        print('done')
