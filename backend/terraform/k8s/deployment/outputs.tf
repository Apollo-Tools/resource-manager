output "service_info" {
  value = (var.service_type == "NoService" || length(kubernetes_service_v1.service) == 0 ? null : {
    name: kubernetes_service_v1.service[0].metadata[0].name
    cluster_ips: (kubernetes_service_v1.service[0].spec[0].cluster_ips == null ? [] :
      kubernetes_service_v1.service[0].spec[0].cluster_ips)
    external_ips: (kubernetes_service_v1.service[0].spec[0].external_ips == null ? [] :
      kubernetes_service_v1.service[0].spec[0].external_ips)
    ports: [
      for port in kubernetes_service_v1.service[0].spec[0].port:
      {
        port: port.port
        node_port: port.node_port
      }
    ]
  })
}

output "pods_info" {
  value = [
    for pod in data.kubernetes_resources.pods.objects:
    {
      pod_name: pod.metadata.name
      node_name: pod.spec.nodeName
      node_ip: pod.status.hostIP
    }
  ]
}
