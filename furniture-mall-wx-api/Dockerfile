FROM java:8

WORKDIR /app

ENV TZ Asia/Shanghai

COPY target/furniture-mall-wx-api-0.1.0-exec.jar app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar","/app/app.jar"]