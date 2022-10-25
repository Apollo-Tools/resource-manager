[![Apollo-Tools RM CI Java Repository](https://github.com/Apollo-Tools/resource-manager/actions/workflows/gradle.yml/badge.svg)](https://github.com/Apollo-Tools/resource-manager/actions/workflows/gradle.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/bc04b2148c2b4b17a9c3602f335c3882)](https://www.codacy.com/gh/Apollo-Tools/resource-manager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Apollo-Tools/resource-manager&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/bc04b2148c2b4b17a9c3602f335c3882)](https://www.codacy.com/gh/Apollo-Tools/resource-manager/dashboard?utm_source=github.com&utm_medium=referral&utm_content=Apollo-Tools/resource-manager&utm_campaign=Badge_Coverage)

# Resource-Manager

## Description
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

## Local Development

### Minimum Requirements
- Java 13
- Docker
- Gradle 7.3.2

### Initial setup
- Change directory to `./local-dev`
- `docker compose up`
- Optional: fill data with example data located in `./local-dev`
- Start `Main.java`
 
