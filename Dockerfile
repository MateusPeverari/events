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
ENV JAVA_OPTS="\
 -Xms3g -Xmx3g \
 -XX:InitialRAMPercentage=60 \
 -XX:MaxRAMPercentage=60 \
 -XX:+UseG1GC \
 -XX:MaxGCPauseMillis=200 \
 -XX:+ParallelRefProcEnabled \
 -XX:+UseStringDeduplication \
 -XX:+AlwaysPreTouch \
 -Xss512k \
 -XX:+ExitOnOutOfMemoryError \
 -XX:+HeapDumpOnOutOfMemoryError \
 -XX:HeapDumpPath=/app/logs \
 -XX:ErrorFile=/app/logs/hs_err_pid%p.log \
 -Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M"

EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]