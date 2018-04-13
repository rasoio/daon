FROM openjdk:9-jdk-slim
MAINTAINER rasoio <rasoio@naver.com>

COPY ./daon-manager.jar /app/app.jar

WORKDIR /app

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]