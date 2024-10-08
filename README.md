[![Apollo-Tools RM CI Java Repository](https://github.com/Apollo-Tools/resource-manager/actions/workflows/gradle.yml/badge.svg)](https://github.com/Apollo-Tools/resource-manager/actions/workflows/gradle.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/bc04b2148c2b4b17a9c3602f335c3882)](https://www.codacy.com/gh/Apollo-Tools/resource-manager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Apollo-Tools/resource-manager&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/bc04b2148c2b4b17a9c3602f335c3882)](https://www.codacy.com/gh/Apollo-Tools/resource-manager/dashboard?utm_source=github.com&utm_medium=referral&utm_content=Apollo-Tools/resource-manager&utm_campaign=Badge_Coverage)

# Apollo-Tools Resource-Manager (ARM)

The Apollo-Tools Resource Manager (ARM) is a multi-user resource management tool for the edge-cloud 
continuum. It simplifies the management and unifies monitoring of resources and enables 
automatic application deployments across all registered resources. 

The ARM provides the following key features:
- Manage and monitor AWS Lambda functions, Amazon EC2 instances, OpenFaaS instances and K8S
  clusters.
- Filter resources based on Service Level Objectives (SLO) and store a subset of these resources as 
  *Resource Ensembles*.
- Automatically deploy functions (Python 3.8, Java 11) and services (Docker containers) on the 
  supported platforms.
- Continuous monitoring of registered resources.
- Optional alerting of resources that are part of active application deployments.

## Table of Contents

<!-- TOC -->
* [Apollo-Tools Resource-Manager (ARM)](#apollo-tools-resource-manager-arm)
  * [Table of Contents](#table-of-contents)
  * [Local Development](#local-development)
    * [Minimum Requirements](#minimum-requirements)
      * [Backend](#backend)
      * [Frontend](#frontend)
    * [Persistence](#persistence)
    * [External Monitoring System](#external-monitoring-system)
  * [Initial setup](#initial-setup)
    * [1. Prepare resources](#1-prepare-resources)
    * [2. Persistence and External Monitoring System](#2-persistence-and-external-monitoring-system)
    * [3. Backend](#3-backend)
      * [Manual startup](#manual-startup)
      * [With IntelliJ](#with-intellij)
    * [4. Frontend](#4-frontend)
  * [Deployment on Kubernetes](#deployment-on-kubernetes)
  * [How to use the ARM](#how-to-use-the-arm)
    * [Supported Deployments](#supported-deployments)
    * [Function Templates / Examples](#function-templates--examples)
    * [Overview](#overview)
    * [Authentication](#authentication)
    * [Resources](#resources)
    * [Functions](#functions)
    * [Services](#services)
    * [Ensembles](#ensembles)
    * [Deployments](#deployments)
    * [Benchmarks](#benchmarks)
<!-- TOC -->

## Local Development

### Minimum Requirements

#### Backend

- [Java](https://www.java.com/de/) 11
- [Docker](https://www.docker.com/)
- [Gradle](https://gradle.org/) 7.3.2
- [Terraform](https://www.terraform.io/) 1.4.5
- [OpenFaaS Cli](https://www.openfaas.com/) 0.15.9
#### Frontend

- [Node](https://nodejs.org/) 16.13.0

### Persistence

- [PostgreSQL](https://www.postgresql.org/) 14.10

### External Monitoring System

- [VictoriaMetrics](https://victoriametrics.com/) v1.96.0
- [Grafana](https://grafana.com/) 10.2.2
- [Metrics Server](https://github.com/kubernetes-sigs/metrics-server) v0.7.0 (enabled on all 
  registered k8s resources)
- [Node Exporter](https://github.com/prometheus/node_exporter) 1.7.0 (setup on all OpenFaaS 
  resources)

## Initial setup

### 1. Prepare resources

- Install Metrics Service on K8s resources, installation instructions can be found 
  [here](https://github.com/kubernetes-sigs/metrics-server?tab=readme-ov-file#installation) 
- Install node exporter on OpenFaaS resources, example bash script that installs OpenFaaS CLI and
  Node Exporter can be found [here](./backend/terraform/aws/vm/templates/startup.sh)

### 2. Persistence and External Monitoring System

- `cd ./local-dev`
- `docker compose up`

### 3. Backend

Copy [config.exmaple.json](./backend/conf/config.example.json) to `./backend/conf/config.json` and adjust the values to 
your requirements:

| Name                          | Description                                                                                                                                                                                                                          | Type           |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| db_host                       | the host url of the PostgreSQL database                                            <br/>                                                                                                                                             | `string`       |
| db_port                       | the port of the PostgreSQL database                                                                                                                                                                                                  | `int`          |
| db_user                       | the user to access the PostgreSQL database                                                                                                                                                                                           | `string`       |
| db_password                   | the password to access the PostgreSQL database                                                                                                                                                                                       | `string`       |
| max_retries                   | the amount of retries for database operations if a serialization error occurs                                                                                                                                                        | `int`          |
| retry_delay_millis            | the time span in milliseconds to wait until a database operation is called again after a serialization error occurred                                                                                                                | `ìnt`          |
| api_port                      | the port of the Rest-API of the ARM                                                                                                                                                                                                  | `int`          |
| build_directory               | the directory, where all build file are stored for the automatic resource deployment                                                                                                                                                 | `string`       |
| dind_directory                | the docker-in-docker volume path of the build directory. For local development "" should be used.                                                                                                                                    | `string`       |
| upload_persist_directory      | the directory, where uploaded files are persisted after successful validation                                                                                                                                                        | `string`       |
| upload_temp_directory         | the directory, where uploaded files are temporarily stored                                                                                                                                                                           | `string`       |
| max_file_size                 | the maximum size of an uploaded file in bytes                                                                                                                                                                                        | `long`         |
| jwt_secret                    | the secret that is used for the encryption of the Json Web Tokens                                                                                                                                                                    | `string`       |
| jwt_algorithm                 | the algorithm that is used for the jwt signature                                                                                                                                                                                     | `string`       |
| token_minutes_valid           | the period of validity of the Json Web Tokens in minutes                                                                                                                                                                             | `int`          |
| ensemble_validation_period    | the time period between each validation performed on all existing ensembles in minutes                                                                                                                                               | `int`          |
| docker_insecure_registries    | the insecure registries that are accessed for openfaas and ec2 deployments                                                                                                                                                           | `list(string)` |
| kube_config_secrets_name      | the name of the secret that contains the kube-configs of the registered k8s instances                                                                                                                                                | `string`       |
| kube_config_secrets_namespace | the name space location of the kube config secrets                                                                                                                                                                                   | `string`       |
| kube_config_directory         | the path of the directory where the kube configs to access registered k8s resources are stored                                                                                                                                       | `string`       |
| kube_api_timeout_seconds      | the timeout of requests that are sent to the k8s api in seconds                                                                                                                                                                      | `int`          |
| kube_image_pull_secrets       | the names of the secrets that contain access credentials to private docker <br/>registries. These secrets are only used for deployments to k8s resources and must be present on every registered k8s resource that should access it. | `list(string)` |
| monitoring_push_url           | the push url of the external monitoring system (VictoriaMetrics)                                                                                                                                                                     | `string`       |
| monitoring_query_url          | the query url of the external monitoring system (VictoriaMetrics)                                                                                                                                                                    | `string`       |
| latency_monitoring_count      | The number of echo requests to send per latency test                                                                                                                                                                                 | `int`          |
| kube_monitoring_period        | the time period between each monitoring update for all registered k8s resources in seconds                                                                                                                                           | `int`          |
| openfaas_monitoring_period    | the time period between each monitoring update for all registered OpenFaaS resources in seconds                                                                                                                                      | `int`          |
| region_monitoring_period      | the time period between each monitoring update for all registered regions in seconds                                                                                                                                                 | `int`          |
| aws_price_monitoring_period   | the time period between each monitoring update the aws price list api in seconds                                                                                                                                                     | `int`          |
| file_cleanup_period           | the time period between file clean ups of failed deployments. This is necessary for deployments where the RM was not able to automatically clean up the files.                                                                       | `int`          |

#### Manual startup

- `cd ./backend`
- Copy `./backend/conf/config.example.json` to `./backend/conf/config.json` and replace the
  existing values with the desired values
- `../gradlew build`
- `../gradlew run`

#### With IntelliJ

- `File -> Open...`
- Select [setttings.gradle.kts](settings.gradle.kts)
- Copy `./backend/conf/config.example.json` to `./backend/conf/config.json` and replace the 
  existing values with the desired values
- Select the gradle task `rm/application/run` in the gradle sidebar

### 4. Frontend

- Adjust the values in [.env.local](./frontend/.env.local) and [.env](./frontend/.env) to your
  requirements:

| Name                      | Description                                                                       |
|---------------------------|-----------------------------------------------------------------------------------|
| NEXT_PUBLIC_API_URL       | the url of the Rest-API of the backend                                            |
| NEXT_PUBLIC_POLLING_DELAY | the polling delay used for updating the status of a selected resource reservation |

- `cd ./frontend`
- `npm install`
- `npm run dev`

## Deployment on Kubernetes

The directory [kubernetes](./kubernetes) contains the following file that can be used to deploy
the ARM on a Kubernetes cluster:

| File                                                  | Description                                                                    | 
|-------------------------------------------------------|--------------------------------------------------------------------------------|
| [rm-api.yaml](./kubernetes/rm-api.yaml)               | Deploys the backend                                                            | 
| [rm-db.yaml](./kubernetes/rm-db.yaml)                 | Deploys a PostgreSQL database                                                  |
| [rm-gui.yaml](./kubernetes/rm-gui.yaml)               | Deploys the frontend                                                           |
| [rm-kube-secret](./kubernetes/rm-kube-secret.yaml)    | Deploys kubeconfigs that are necessary to monitor the registered k8s resources |
| [rm-monitoring.yaml](./kubernetes/rm-monitoring.yaml) | Deploys VictoriaMetrics and Grafana                                            |


Make sure that the database and secret are deployed and running before you deploy the backend. The
files contain comments about properties and can be adjusted to your requirements.

Deployments can be executed with: <br/>
`kubectl apply -f ./PATH/TO/DEPLOYMENT/FILE.yaml`

To use your own container images following steps have to be performed:
1. Build your images:
   - `docker build -t ~username~/rm-api:latest ./backend --push`
   - `docker build -t ~username~/rm-gui:latest ./frontend --push`
2. Change the images used in [rm-api.yaml](./kubernetes/rm-api.yaml) and
   [rm-gui.yaml](./kubernetes/rm-gui.yaml) to your newly created images.
3. Apply the deployment with `kubectl apply` as mentioned above.

## How to use the ARM

### Supported Deployments

<img height="500" src="doc\supported-deployments.png" alt="Supported Deployments"/>

The ARM supports the deployment of *Functions* and *Services*. 

*Functions* represent the implementation 
of a serverless function that have to be written in either Java 11 or Python 3.8 and can be 
deployed to three different platforms:
- [AWS Lambda](https://aws.amazon.com/lambda/)
- [Amazon EC2](https://aws.amazon.com/de/ec2/) with [OpenFaaS](https://www.openfaas.com/) as
  serverless computing platform 
- Any self-managed device with a running instance of [OpenFaaS](https://www.openfaas.com/)

*Services* represent container images and can be deployed on any Kubernetes instance that can be
accessed and is registered at the ARM. Currently only self-managed devices with a
running Kubernetes instance are supported.

### Function Templates / Examples

At [faas-templates](./backend/faas-templates) you can find templates for all supported function
runtimes. Each runtime directory contains a README.md that explains, how to implement a function
in the respective language. For example implementations go to
[faas-examples](./backend/faas-examples). The example functions are ready to deploy using the
ARM and provide additional guidance for function developers.

### Overview

The following section describes the basic functionalities of the ARM using the
frontend. The documentation of the **REST-API** is available as an
[OpenAPI 3.0](https://swagger.io/specification/v3/) specification. For the optimal reading
experience it is advised to open the specification inside a tool like
[Swagger Editor](https://editor.swagger.io/), [Postman](https://www.postman.com/) or ide specific
plugins that can display OpenAPI 3.0 specifications. The specification is available at the path
[resource-manager.yaml](./backend/src/main/resources/openapi/resource-manager.yaml)

### Authentication

To access the ARM as a new user, a new account has to be created by an existing
account, that has the *admin* role. After the account has been created, the user can log in with
the credentials specified in the previous step. At the path **/accounts/profile** it is possible
to update the password, add cloud credentials and Virtual Private Clouds (VPCs).

**Important**: To deploy resources to **AWS** the user has to store valid cloud credentials at the
ARM. If the deployments include virtual machines (Amazon EC2) it is also necessary to
register a VPC (Virtual Private Cloud) in the ARM for each region the user wants to
deploy them. Both tasks can be done at the profile page in the frontend. For k8s deployments it is
also necessary to assign a namespace to the user's account. This has to be done by accounts that
have the admin role.

### Resources

At the path /resources/new-resource users can register new resources and at /resources/resources
all existing resources can be listed. Depending on the resource platform, there are some required
metrics/properties that must be added to a resource after it's creation to be qualified for
deployments. Nodes of K8s resources are created automatically by the monitoring service of the RM.
**They can not be registered manually**.

### Functions

At the path /functions/new-function users can register new functions and at /functions/functions
all existing functions can be listed. It is possible to create private and public functions. Both
can only be modified by the creator but public functions can be used for deployments by everyone.

### Services

At the path /services/new-service users can register new functions and at /services/services all
existing service can be listed. It is possible to create private and public services. Both can
only be modified by the creator but public services can be used for deployments by everyone.

### Ensembles

At the path /ensembles/new-ensemble users can register new resource ensembles and at 
/ensembles/ensembles all existing ensembles can be listed. Ensembles are private and can only be
viewed by their creator. An ensemble consists of a list of service level objectives and list of
resources. A service level objective defines a limit for a certain metric that has to be fulfilled
by all resources that are part of the resource ensemble. When creating a new resource ensemble,
all resources have to fulfill the specified service level objectives. After the creation the
ensemble can be manually validated. In the ensemble details invalid resources are highlighted with
a red background. Additionally, all registered resource ensembles are validated periodically. The
validation period can be defined with *ensemble_validation_period*.

### Deployments

At the path /deployments/new-deployment users can create a new deployment and at
/deployments/deployments all existing deployments can be listed. Deployments are private and can
only be viewed by their creator. Existing deployments can have the status **NEW**, **DEPLOYED**,
**TERMINATING**, **TERMINATED** and **ERROR**. The status of the deployment depends on the
deployment status of the resources that are part of the deployment. Opening the details of a
deployment, displays the status of all resources as well as all logs that were created during
deployment/termination. During the deployment and termination the deployments endpoint gets polled
in a predefined interval (configurable with the .env variable *NEXT_PUBLIC_POLLING_DELAY*).
If a new deployment contains a resource with EC2 or OpenFaaS as destination platform, users must 
provide valid docker credentials for a docker registry, that is reachable by all resources. 
**Important**: Do not use your actual password for that. You can create and use an
[access token](https://docs.docker.com/docker-hub/access-tokens/) with write and read permissions
instead and delete the token after all resources were deployed. In addition, resources with
OpenFaaS as platform are self-managed and are required to have a running instance of
[OpenFaaS](https://www.openfaas.com/) that is accessible by the ARM.

If a deployment only contains a container resource users don't have to provide any additional
credentials. The only requirement for deployments on container resources is one namespace per
resources that has to be assigned to the users account by an admin.

To access all these urls in the gui, the suggested way is to use the sidebar. All routes that are
explained above are reachable by using the sidebar.

### Benchmarks
For the benchmarking of the ARM, we implemented a benchmarking tool using python that can be found 
at [benchmark](./benchmark). The results of the benchmarks can be found at 
[benchmark-results](./benchmark/results). The analysis of these results has been implemented in 
[Jupyter Notebooks](https://jupyter.org/) and is located at 
[rm-analysis](./benchmark/results/rm-analysis). The raw data can be found in the other directories. 
