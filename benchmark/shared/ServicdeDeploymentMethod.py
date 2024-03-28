from enum import Enum


class ServiceDeploymentMethod(str, Enum):
    STARTUP = "startup"
    SHUTDOWN = "shutdown"
