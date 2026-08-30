# Etapa 1: Fase de compilacion
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app

# Instalar unzip y curl para que Maven Wrapper pueda descargar y extraer Apache Maven
RUN apt-get update && apt-get install -y --no-install-recommends unzip curl && rm -rf /var/lib/apt/lists/*

# Copiar el wrapper de Maven y el archivo pom.xml para cachear dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Copiar el codigo fuente y compilar el JAR omitiendo los tests
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Fase de ejecucion (Runtime ligero con hardening de seguridad)
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

# Instalar curl para healthcheck y crear usuario sin privilegios root
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -g 1001 avalon && useradd -u 1001 -g avalon -s /bin/sh avalon

# Copiar el JAR compilado desde la etapa 1 y asignar permisos
COPY --from=build /app/target/avalon-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R avalon:avalon /app

# Cambiar a usuario no root
USER 1001:1001

# Exponer el puerto configurado (8900)
EXPOSE 8900

# Variables de entorno recomendadas de la JVM para contenedores
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8900}/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
