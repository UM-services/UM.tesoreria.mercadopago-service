FROM eclipse-temurin:21-jre-alpine

# Instalar curl en la imagen
RUN apk update && apk add curl

# Copiar el archivo JAR
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]
