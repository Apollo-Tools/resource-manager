FROM gradle:7.3.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
COPY ./ruleset.xml /home/gradle/src
COPY ./ruleset_test.xml /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -PexcludeTestcontainers=true

FROM docker:git

USER root

# Install jdk11
RUN  apk update \
  && apk upgrade \
  && apk add ca-certificates \
  && update-ca-certificates \
  && apk add --update coreutils && rm -rf /var/cache/apk/*   \
  && apk add --update openjdk11 tzdata curl unzip bash \
  && apk add --no-cache nss \
  && rm -rf /var/cache/apk/*


# Install openfaas
RUN curl -sSL https://cli.openfaas.com | sh

# Install terraform
RUN apk add terraform --repository=https://dl-cdn.alpinelinux.org/alpine/edge/community

# Fix sigsev error thrown by netty see: https://github.com/micrometer-metrics/micrometer/issues/2776
RUN apk --no-cache add gcompat
ENV LD_PRELOAD=/lib/libgcompat.so.0

# Install kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
RUN install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

EXPOSE 8888

RUN mkdir /app
RUN mkdir /conf
RUN mkdir /faas-templates
RUN mkdir /terraform

COPY ./conf/config.json ./conf/config.json
COPY ./faas-templates ./faas-templates
COPY ./terraform ./terraform
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/rm-api.jar

ENV db_host=localhost
ENV db_port=5432
ENV db_user=root
ENV db_password=root
ENV max_retries=5
ENV retry_delay_millis=1000
ENV build_directory=build
ENV dind_directory="var/lib/apollo-rm/"
ENV upload_persist_directory="build/upload/"
ENV upload_temp_directory="build/upload/temp/"
ENV max_file_size=10000000
ENV jwt_secret="**secret**"
ENV token_minutes_valid=1080
ENV ensemble_validation_period=60
ENV docker_insecure_registries=[]
ENV kube_config_secrets_name="rm_kubeconfigs"
ENV kube_config_secrets_namespace="default"
ENV kube_config_directory="build/kubeconfig",
ENV kube_api_timeout_seconds=10
ENV kube_monitoring_period=5
ENV kube_image_pull_secrets=["regcred"]

ENTRYPOINT ["java", "-jar" ,"/app/rm-api.jar"]
