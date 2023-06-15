# This docker file is a prebuild for openfaas usage with numpy preinstalled. This is needed because the installation of
# numpy takes a lot of time on linx/arm/v7 (>10 minutes).
ARG PYTHON_VERSION=3.8
FROM --platform=${TARGETPLATFORM:-linux/amd64} ghcr.io/openfaas/of-watchdog:0.9.10 as watchdog
FROM --platform=${TARGETPLATFORM:-linux/amd64} python:${PYTHON_VERSION}-slim-buster as build

COPY --from=watchdog /fwatchdog /usr/bin/fwatchdog
RUN chmod +x /usr/bin/fwatchdog

ARG ADDITIONAL_PACKAGE="libgfortran5 libatlas3-base g++"
# Alternatively use ADD https:// (which will not be cached by Docker builder)

RUN apt-get -qy update \
    && apt-get -qy install gcc make ${ADDITIONAL_PACKAGE} \
    && rm -rf /var/lib/apt/lists/*

USER root
RUN pip install --no-cache-dir numpy==1.24.3 --extra-index-url https://www.piwheels.org/simple
RUN pip install --no-cache-dir flask waitress tox==3.*

