# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성 정의 파일만 먼저 복사 → 소스만 바뀌면 의존성 다운로드 레이어를 캐시에서 재사용
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

# 소스 복사 후 실행 가능한 jar 생성
COPY src src
RUN ./gradlew bootJar --no-daemon && cp build/libs/*.jar app.jar

# ---------- 2단계: 실행 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# root가 아닌 전용 사용자로 실행
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

# JAVA_OPTS로 메모리, 시간대 등 JVM 옵션을 compose에서 주입
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]