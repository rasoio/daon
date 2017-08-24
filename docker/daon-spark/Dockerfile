FROM java:8-jdk
MAINTAINER rasoio <rasoio@naver.com>

# Version
ENV SPARK_VERSION=2.1.0

# Set home
ENV SPARK_HOME=/app/spark-$SPARK_VERSION

ENV JAVA_HOME              /usr/lib/jvm/java-8-openjdk-amd64
ENV JAVA_OPTS              ${JAVA_OPTS}
ENV PATH                   $PATH:$JAVA_HOME/bin

# Install Spark
RUN mkdir -p "${SPARK_HOME}" \
  && export ARCHIVE=spark-$SPARK_VERSION-bin-hadoop2.7.tgz \
  && curl -s http://d3kbcqa49mib13.cloudfront.net/$ARCHIVE | tar -xz -C "${SPARK_HOME}" --strip-components 1 \
  && rm -rf $ARCHIVE

#COPY spark-env.sh $SPARK_HOME/conf/spark-env.sh
ENV PATH=$PATH:$SPARK_HOME/bin

COPY ./daonSpark.jar /app/job.jar

WORKDIR /app