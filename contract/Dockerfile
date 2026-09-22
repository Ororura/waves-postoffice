FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="waves-postoffice-contract"

WORKDIR /app

ENV JAVA_MEM="-Xmx256M" \
    JAVA_OPTS=""

RUN apk --no-cache add curl

COPY build/libs/*-all.jar /app/app.jar

CMD ["/bin/sh", "-c", "exec java $JAVA_MEM $JAVA_OPTS -jar /app/app.jar"]
