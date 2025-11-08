# Run Stage
FROM eclipse-temurin:17-jdk-jammy

# curl 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 로그 디렉토리 생성 및 사용자 생성
RUN groupadd -g 1000 appuser && \
    useradd --no-log-init -u 1000 -g appuser -m appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appuser /app/logs

USER appuser

WORKDIR /app

# 빌드 결과물 복사
COPY --chown=appuser:appuser ./build/libs/*.jar channeling.jar

# 포트 노출 및 실행
EXPOSE 8080

CMD ["java", "-jar", "channeling.jar"]