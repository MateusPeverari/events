# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -B package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/logs
ENV JAVA_OPTS="-Xms8g -Xmx8g -Xss512k -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+AlwaysPreTouch -XX:InitiatingHeapOccupancyPercent=30 -XX:G1ReservePercent=20 -XX:G1NewSizePercent=20 -XX:G1MaxNewSizePercent=60 -XX:+UnlockDiagnosticVMOptions -XX:+G1SummarizeConcMark -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs -XX:ErrorFile=/app/logs/hs_err_pid%p.log -Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M"
EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
