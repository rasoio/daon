FROM java:8-jdk
MAINTAINER rasoio <rasoio@naver.com>

ENV JAVA_HOME              /usr/lib/jvm/java-8-openjdk-amd64
ENV JAVA_OPTS              ${JAVA_OPTS}
ENV PATH                   $PATH:$JAVA_HOME/bin

EXPOSE 5001

COPY ./daonManager.jar /app/app.jar

COPY ./daonManager.conf /app/app.conf

WORKDIR /app

CMD ["java", "$JAVA_OPTS", "-jar", "app.jar"]