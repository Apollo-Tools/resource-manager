[![Apollo-Tools RM CI Java Repository](https://github.com/Apollo-Tools/resource-manager/actions/workflows/gradle.yml/badge.svg)](https://github.com/Apollo-Tools/resource-manager/actions/workflows/gradle.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/bc04b2148c2b4b17a9c3602f335c3882)](https://www.codacy.com/gh/Apollo-Tools/resource-manager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Apollo-Tools/resource-manager&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/bc04b2148c2b4b17a9c3602f335c3882)](https://www.codacy.com/gh/Apollo-Tools/resource-manager/dashboard?utm_source=github.com&utm_medium=referral&utm_content=Apollo-Tools/resource-manager&utm_campaign=Badge_Coverage)

# Resource-Manager

The goal is to implement a distributed resource manager that can be used by the Apollo Enactment Engine to request 
resources based on service level objectives (SLOs).  Nevertheless, the resource manager should not depend on the 
Apollo Enactment Engine and should be easily extendable with additional features. This ensures that the resource 
manager can be used by other enactment engines as well and is not limited to Apollo.

The main features of the resource manager are split into three different areas. One being the management of resources. 
It should be possible to manually register resources from multi-cloud, edge and IoT environments and enhance these 
with custom properties. To keep track of these resources it is necessary to implement a monitoring service that 
observes metrics like the online state, utilization (memory, cpu and disc), costs, energy consumption, co2 emissions, 
latency, bandwidth and others.  Based on these metrics and the SLOs that are set by the client, resources are proposed 
that can then be reserved by the client. After reservation all resources that are not deployed should be deployed 
automatically. When they are freed after usage they should be terminated automatically as well.

The manual management of resources and metrics should be visualized in a basic graphical user interface.

## Table of Contents

<!-- TOC -->
* [Resource-Manager](#resource-manager)
  * [Table of Contents](#table-of-contents)
  * [Local Development](#local-development)
    * [Minimum Requirements](#minimum-requirements)
      * [Backend](#backend)
      * [Frontend](#frontend)
    * [Initial setup](#initial-setup)
    * [PostgreSQL](#postgresql)
    * [Backend](#backend-1)
      * [Manual startup](#manual-startup)
      * [With IntelliJ](#with-intellij)
    * [Frontend](#frontend-1)
  * [Deployment on Kubernetes](#deployment-on-kubernetes)
  * [How to use the Resource Manager](#how-to-use-the-resource-manager)
    * [Supported Deployments](#supported-deployments)
    * [Overview](#overview)
    * [Authentication](#authentication)
    * [Resources](#resources)
    * [Functions](#functions)
    * [Services](#services)
    * [Ensembles](#ensembles)
    * [Deployments](#deployments)
<!-- TOC -->

## Local Development

### Minimum Requirements
#### Backend
- Java 11
- Docker
- Gradle 7.3.2
- Terraform 1.4.5
- OpenFaaS Cli 0.15.9
- A PostgreSQL DB (docker-compose for local development can be found in ./local-dev)
#### Frontend
- Node 16.13.0

### Initial setup
### PostgreSQL
- `cd ./local-dev`
- `docker compose up`
### Backend
Copy [config.exmaple.json](./backend/conf/config.example.json) to `./backend/conf/config.json` and adjust the values to 
your requirements:

| variable                   | description                                                                                      |
|----------------------------|--------------------------------------------------------------------------------------------------|
| db_host                    | the host url of the PostgreSQL database                                                          |
| db_port                    | the port of the PostgreSQL database                                                              |
| db_user                    | the user to access the PostgreSQL database                                                       |
| db_password                | the password to access the PostgreSQL database                                                   |
| api_port                   | the port of the Rest-API of the Resource Manager                                                 |
| build_directory            | the directory, where all build file are stored for the automatic resource deployment             |
| dind_directory             | the docker-in-docker volume path of the buil directory. For local development "" should be used. |
| jwt_secret                 | the secret that is used for the encryption of the Json Web Tokens                                |
| token_minutes_valid        | the period of validity of the Json Web Tokens                                                    |
| ensemble_validation_period | The period of time between each validation performed on all existing ensembles.                  |

#### Manual startup
- `cd ./backend`
- `../gradlew build`
- `../gradlew run`
#### With IntelliJ
- `File -> Open...`
- Select [setttings.gradle.kts](settings.gradle.kts)
- Copy `./backend/conf/config.example.json` to `./backend/conf/config.json`
- Open [Main.java](backend/src/main/java/at/uibk/dps/rm/Main.java)
- Execute `public static void main(String[] args)`
### Frontend
- Adjust the values in [.env.local](./frontend/.env.local) and [.env](./frontend/.env) to your requirements:

| variable                   | description                                                                                      |
|----------------------------|--------------------------------------------------------------------------------------------------|
| NEXT_PUBLIC_API_URL        | the url of the Rest-API of the backend                                                           |
| NEXT_PUBLIC_POLLING_DELAY  | the polling delay used for updating the status of a selected resource reservation                |

- `cd ./frontend`
- `npm install`
- `npm run dev`

## Deployment on Kubernetes
The directory [kubernetes](./kubernetes) contains the following file that can be used to deploy the Resource Manager 
on a 
Kubernetes cluster:

| File                                    | Description                   | 
|-----------------------------------------|-------------------------------|
| [rm-api.yaml](./kubernetes/rm-api.yaml) | Deploys the backend           | 
| [rm-db.yaml](./kubernetes/rm-db.yaml)   | Deploys a PostgreSQL database |
| [rm-gui.yaml](./kubernetes/rm-gui.yaml) | Deploys the frontend          |

Make sure that the database is deployed and running before you deploy the backend. The files contain comments about
properties and can be adjusted to your requirements.

To use your own container images following steps have to be performed:
1. Build your images:
   - `docker build -t ~username~/rm-api:latest ./backend --push`
   - `docker build -t ~username~/rm-gui:latest ./frontend --push`
2. Change the images used in [rm-api.yaml](./kubernetes/rm-api.yaml) and [rm-gui.yaml](./kubernetes/rm-gui.yaml) to your
newly created images.

## How to use the Resource Manager

### Supported Deployments

<img height="500" src="doc\supported-deployments.png" alt="Supported Deployments"/>

The Resource Manager supports the deployment of *Functions* and *Services*. *Functions* represent the implementation 
of a serverless function that can be deployed on three different platforms:
- AWS Lambda
- AWS EC2 as a OpenFaaS function
- Any self-managed device with a running instance of OpenFaaS

*Services* represent container images and can be deployed on any Kubernetes instance that is reachable by the 
Resource Manager. Currently only self-managed devices with a running Kubernetes instance are supported.

### Overview
The following describes the basic usage of the Resource Manager. The documentation of the **REST-API** is available 
in the format of an [OpenAPI 3.0](https://swagger.io/specification/v3/) specification. For the optimal reading 
experience it is advised to open the specification inside a tool like [Swagger Editor](https://editor.swagger.io/) or 
[Postman](https://www.postman.com/). The specification is available at the path 
[resource-manage.yaml](./backend/src/main/resources/openapi/resource-manager.yaml)

### Authentication
To have access to the resource manager you have to create a new user. This can be done at the home path of the frontend
while not logged in. After creating the user you can log in with your credentials specified in the previous step. At 
the path **/accounts/profile** it is possible to update the password, add cloud credentials and Virtual Private 
Clouds (VPCs).

**Important**: To deploy resources to **AWS** you have to store valid cloud credentials at the Resource Manager. If your
deployments include virtual machines it is also necessary to register a VPC (Virtual Private Cloud) at the Resource 
Manager for each region that you want to deploy them. Both tasks can be done at the profile page in the frontend.

### Resources
At the path /resources/new-resource you can register new resources and at /resources/resources you can list all
existing resources. Depending on the resource platform, there are some required metrics/properties 
that must be added to it after the creation of a new resource to be qualified for deployments.

### Functions
At the path /functions/new-function you can register new functions and at /functions/functions you can list all
existing functions.

### Services
At the path /services/new-service you can register new functions and at /services/services you can list all
existing functions.

### Ensembles
At the path /ensembles/new-ensemble you can register new ensembles and at /ensembles/ensembles you can list all
existing ensembles.

### Deployments
At the path /deployments/new-deployment you can create a new deployment and at /deployments/deployments you can 
list all existing deployments. Existing deployments can have the status **NEW**, **DEPLOYED**, **TERMINATING**, 
**TERMINATED** and **ERROR**. The status of the deployment depends on the deployment status of the resources that 
are part of the deployment. Opening the details of a deployment, displays the status of all resources as well as 
all logs that were created during deployment/termination. During the deployment and termination the deployments endpoint
gets polled in a predefined interval (configurable with the .env variable *NEXT_PUBLIC_POLLING_DELAY*).
If a new deployment contains a resource with EC2 or OpenFaaS as destination platform you have to provide 
your docker credentials. Do not use your password for that. You can create and use an 
[access token](https://docs.docker.com/docker-hub/access-tokens/) with write and read permissions instead. 
In addition, resources with OpenFaaS as Platform are self-managed and are required to have a running 
instance of [OpenFaaS](https://www.openfaas.com/) that is accessible by the Resource Manager.

If a deployment contains a container resource you have to provide a kube config. See the Open API
Specification mentioned at the beginning of this section for details.

To access all these urls in the gui, the suggested way is to use the sidebar. All routes that are explained above 
are reachable by using the sidebar.
