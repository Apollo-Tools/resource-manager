FROM gradle:7.3.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
COPY ./ruleset.xml /home/gradle/src
COPY ./ruleset_test.xml /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

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


EXPOSE 8888

RUN mkdir /app
RUN mkdir /conf
RUN mkdir /runtime
RUN mkdir /terraform

COPY ./conf/config.json ./conf/config.json
COPY ./runtime ./runtime
COPY ./terraform ./terraform
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/rm-api.jar

ENV db_host=localhost
ENV db_port=5432
ENV db_user=root
ENV db_password=root
ENV build_directory=build
ENV dind_directory="var/lib/apollo-rm/"
ENV jwt_secret="**secret**"
ENV token_minutes_valid=1080
ENV TF_CLI_CONFIG_FILE="/terraform/config.tfrc"

ENTRYPOINT ["java", "-jar" ,"/app/rm-api.jar"]
