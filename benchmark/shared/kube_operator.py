import logging
import time
from decimal import Decimal

from kubernetes import client, utils, watch
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

    def create_stress_deployment(self, node: str, namespace: str, command: str):
        api_client = client.ApiClient()
        core_v1 = client.CoreV1Api()
        example_dict = {
            'apiVersion': 'apps/v1',
            'kind': 'Deployment',
            'metadata': {'name': 'rm-benchmark-stress', 'namespace': namespace},
            'spec': {
                'selector': {'matchLabels': {'app': 'rm-benchmark-stress'}},
                'replicas': 1,
                'template': {
                    'metadata': {'labels': {'app': 'rm-benchmark-stress'}},
                    'spec': {
                        'containers': [{
                            'name': 'rm-benchmark-stress',
                            'image': 'mohsenmottaghi/container-stress',
                            'command': [command.split(" ")[0]],
                            'args': command.split(" ")[1:]
                        }],
                        'nodeName': node
                    }}
            }
        }
        utils.create_from_dict(api_client, example_dict)
        w = watch.Watch()
        for event in w.stream(func=core_v1.list_namespaced_pod,
                              namespace=namespace,
                              label_selector='app=rm-benchmark-stress',
                              timeout_seconds=1):
            if event["object"].status.phase == "Running":
                w.stop()
                start_time = time.time()
                return {'name': 'rm-benchmark-stress', 'timestamp': start_time * 1000}
            # event.type: ADDED, MODIFIED, DELETED
            if event["type"] == "DELETED":
                # Pod was deleted while we were waiting for it to start.
                logger.debug("%s deleted before it started")
                w.stop()
                return {'name': 'rm-benchmark-stress', 'timestamp': -1}

        return {'name': 'rm-benchmark-stress', 'timestamp': -1}


    def terminate_stress_deployment(self, deployment_name: str, namespace: str):
        api_client = (client.AppsV1Api())

        api_client.delete_namespaced_deployment(name=deployment_name,
                                                namespace=namespace,
                                                body=client.V1DeleteOptions(grace_period_seconds=0))
