FROM java:9-jdk
MAINTAINER rasoio <rasoio@naver.com>

COPY ./daonManager.jar /app/app.jar

WORKDIR /app

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]