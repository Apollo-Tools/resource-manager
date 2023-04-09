FROM gradle:7.3.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

FROM openjdk:11-slim

EXPOSE 8888

RUN mkdir /app
RUN mkdir /conf

COPY ./conf/config.json ./conf/config.json
COPY --from=build /home/gradle/src/build/libs/*-fat.jar /app/rm-api.jar


ENV db_host=localhost
ENV db_port=5432
ENV db_user=root
ENV db_password=root
ENV jwt_secret=**secret**
ENV token_minutes_valid=1080

ENTRYPOINT ["java", "-jar" ,"/app/rm-api.jar"]