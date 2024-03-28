import asyncio
import logging
import time
from decimal import Decimal

from kubernetes import client, utils, watch
from kubernetes.client import CustomObjectsApi
from kubernetes import config
from kubernetes.utils import parse_quantity

from schemas.schemas import ServiceDeploymentBenchmark

logger = logging.getLogger('uvicorn.info')


class KubeOperator:
    def __init__(self, kube_config) -> None:
        config.load_kube_config_from_dict(config_dict=kube_config)
        self.k8s_client = CustomObjectsApi()
        self.api_client = client.ApiClient()
        self.core_v1 = client.CoreV1Api()
        self.apps_v1 = client.AppsV1Api()

    def get_metrics_for_pod(self, pod_name: str) -> dict[str, str | Decimal]:
        pods = self.k8s_client.list_cluster_custom_object('metrics.k8s.io', 'v1beta1', 'pods')
        for pod in pods['items']:
            if pod['metadata']['name'] == pod_name:
                container = pod['containers'][0]
                cpu = parse_quantity(container['usage']['cpu'])
                memory = parse_quantity(container['usage']['memory'])
                return {'timestamp': pod['timestamp'], 'cpu': cpu, 'memory': memory}
        return {'cpu': Decimal(-1), 'memory': Decimal(-1)}

    def create_stress_deployment(self, node: str, namespace: str, command: str):
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
        utils.create_from_dict(self.api_client, example_dict)
        w = watch.Watch()
        for event in w.stream(func=self.core_v1.list_namespaced_pod,
                              namespace=namespace,
                              label_selector='app=rm-benchmark-stress',
                              timeout_seconds=10):
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
        self.apps_v1.delete_namespaced_deployment(name=deployment_name,
                                                  namespace=namespace,
                                                  body=client.V1DeleteOptions(grace_period_seconds=0))

    def create_service_deployment(self, idx: int, img: str, namespace: str, replicas: int, cpu: float,
                                  memory: int, container_port: int, svc_port: int, external_ip: str):
        deployment_name = f"rm-benchmark-overhead-{idx}"

        service = client.V1Service()
        metadata = client.V1ObjectMeta()
        metadata.name = f"{deployment_name}-service"
        service.metadata = metadata
        spec = client.V1ServiceSpec()
        spec.type = 'LoadBalancer'
        spec.external_i_ps = [external_ip]
        spec.selector = {"app": deployment_name}
        port = client.V1ServicePort(protocol='TCP', target_port=container_port, port=svc_port + idx)
        spec.ports = [port]
        service.spec = spec

        resources = client.V1ResourceRequirements(
            limits={"cpu": str(cpu), "memory": f"{memory}M"},
            requests={"cpu": str(cpu), "memory": f"{memory}M"}
        )
        container = client.V1Container(
            name=deployment_name,
            image=img,
            resources=resources
        )
        template = client.V1PodTemplateSpec(
            metadata=client.V1ObjectMeta(labels={"app": deployment_name}),
            spec=client.V1PodSpec(containers=[container])
        )
        spec = client.V1DeploymentSpec(
            replicas=replicas,
            selector=client.V1LabelSelector(match_labels={"app": deployment_name}),
            template=template,
        )
        deployment_metadata = client.V1ObjectMeta(name=deployment_name)
        deployment = client.V1Deployment(
            api_version="apps/v1",
            kind="Deployment",
            metadata=deployment_metadata,
            spec=spec
        )
        self.core_v1.create_namespaced_service(namespace, service)
        self.apps_v1.create_namespaced_deployment(namespace, deployment)
        w = watch.Watch()
        for event in w.stream(func=self.core_v1.list_namespaced_pod,
                              namespace=namespace,
                              label_selector=f"app={deployment_name}",
                              timeout_seconds=30):
            if event["object"].status.phase == "Running":
                break
            # event.type: ADDED, MODIFIED, DELETED
            if event["type"] == "DELETED":
                # Pod was deleted while we were waiting for it to start.
                logger.debug("%s deleted before it started")
                break
        w.stop()

    async def start_service_deployments(self, index: int, service_deployment: ServiceDeploymentBenchmark):
        tasks = []
        start = time.time()
        for i in range(len(service_deployment.request_body.service_resources)):
            tasks.append(asyncio.to_thread(self.create_service_deployment, index * 1000 + i,
                                           service_deployment.image, service_deployment.namespace,
                                           service_deployment.replicas, service_deployment.cpu,
                                           service_deployment.memory, service_deployment.container_port,
                                           service_deployment.svc_port, service_deployment.external_ip))
        await asyncio.gather(*tasks)
        end = time.time()
        return (end - start) * 1000

    def delete_service_deployment(self, idx: int, namespace: str):
        deployment_name = f"rm-benchmark-overhead-{idx}"

        self.apps_v1.delete_namespaced_deployment(
            name=deployment_name,
            namespace=namespace,
            body=client.V1DeleteOptions(
                propagation_policy='Foreground',
                grace_period_seconds=30
            )
        )
        self.core_v1.delete_namespaced_service(
            name=f"{deployment_name}-service",
            namespace=namespace,
            body=client.V1DeleteOptions(
                propagation_policy='Foreground',
                grace_period_seconds=30
            )
        )

    async def stop_service_deployments(self, index: int, service_deployment: ServiceDeploymentBenchmark):
        tasks = []
        start = time.time()
        for i in range(len(service_deployment.request_body.service_resources)):
            tasks.append(asyncio.to_thread(self.delete_service_deployment, index * 1000 + i,
                                           service_deployment.namespace))
        await asyncio.gather(*tasks)
        end = time.time()
        return (end - start) * 1000
