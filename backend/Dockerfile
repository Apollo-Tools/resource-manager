FROM gradle:7.3.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
COPY ./ruleset.xml /home/gradle/src
COPY ./ruleset_test.xml /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -PexcludeTestcontainers=true -x test

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
RUN wget https://releases.hashicorp.com/terraform/1.4.5/terraform_1.4.5_linux_amd64.zip
RUN unzip terraform_1.4.5_linux_amd64.zip && rm terraform_1.4.5_linux_amd64.zip
RUN mv terraform /usr/bin/terraform

# Fix sigsev error thrown by netty see: https://github.com/micrometer-metrics/micrometer/issues/2776
RUN apk --no-cache add gcompat
ENV LD_PRELOAD=/lib/libgcompat.so.0

EXPOSE 8080

RUN mkdir /app
RUN mkdir /conf
RUN mkdir /faas-templates
RUN mkdir /terraform
RUN mkdir /monitoring

COPY ./conf/config.json ./conf/config.json
COPY ./faas-templates ./faas-templates
COPY ./terraform ./terraform
COPY ./monitoring ./monitoring
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/rm-api.jar

ENV db_host=localhost
ENV db_port=5432
ENV db_user=root
ENV db_password=root
ENV max_retries=5
ENV retry_delay_millis=1000
ENV api_port=8080
ENV build_directory="build"
ENV dind_directory="var/lib/apollo-rm/"
ENV upload_persist_directory="build/upload/"
ENV upload_temp_directory="build/upload/temp/"
ENV max_file_size=10000000
ENV jwt_secret="**secret**"
ENV jwt_algorithm="HS256"
ENV token_minutes_valid=1080
ENV ensemble_validation_period=60
ENV docker_insecure_registries=[]
ENV kube_config_secrets_name="rm_kubeconfigs"
ENV kube_config_secrets_namespace="default"
ENV kube_config_directory="build/kubeconfig",
ENV kube_api_timeout_seconds=10
ENV kube_image_pull_secrets=["regcred"]

ENV monitoring_push_url="http://rm-monitoring-vm:8890"
ENV monitoring_query_url="http://rm-monitoring-vm:8889"
ENV kube_monitoring_period=0.25
ENV openfaas_monitoring_period=0.1
ENV region_monitoring_period=0.1
ENV aws_price_monitoring_period=1440

ENTRYPOINT ["java", "-jar" ,"/app/rm-api.jar"]
