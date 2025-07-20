# Etapa 1: Compilación con Maven y JDK
# Usamos una imagen oficial que contiene Maven y Java (Temurin)
FROM maven:3-eclipse-temurin-24-alpine AS build

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos solo el pom.xml para aprovechar la caché de Docker
# Si las dependencias no cambian, esta capa no se reconstruye
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente
COPY src ./src

# Compilamos la aplicación y generamos el JAR
RUN mvn clean package


# Etapa 2: Creación de la imagen final y ligera
# Usamos una imagen solo con el JRE, que es más pequeña
FROM eclipse-temurin:24-jre-alpine

# Instalar curl en la imagen final
RUN apk update && apk add curl

# Creamos un usuario y grupo no privilegiados por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Instalar curl en la imagen final
RUN apk update && apk add curl

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos el JAR generado desde la etapa de compilación
COPY --from=build /app/target/um.tesoreria.mercadopago-service.jar ./um.tesoreria.mercadopago-service.jar

# Cambiamos al usuario no privilegiado
USER appuser

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "um.tesoreria.mercadopago-service.jar"]
