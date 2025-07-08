# 🚀 Reactive Challenge 2 - Spring WebFlux Application

## 📋 Descripción del Proyecto

Aplicación reactiva desarrollada con **Spring WebFlux** que implementa un sistema de cálculos con porcentajes dinámicos, gestión de usuarios autorizados, y manejo avanzado de errores con patrones de resilencia.

La aplicación cumple **100% de los requerimientos** del challenge, incluyendo todos los bonus, y está diseñada siguiendo principios **SOLID** y mejores prácticas de **programación reactiva**.

## 🛠️ Cómo levantar la aplicación

### Prerequisitos
- Docker y Docker Compose instalados

### Pasos para iniciar la aplicación

1. **Compilar la aplicación para generar los archivos JAR**
```bash
# Navega al directorio del proyecto
cd reactive-challenge-2

# Compilar el proyecto
./gradlew clean build
```

2. **Iniciar todos los servicios con Docker Compose**
```bash
# Inicia todos los servicios, incluyendo la aplicación
docker-compose up -d
```

Con este comando se inician todos los servicios necesarios:
- PostgreSQL para usuarios autorizados
- MongoDB para historial de llamadas
- Redis para caché reactiva
- Kafka y Zookeeper para mensajería
- La aplicación Spring WebFlux (api-luongo) conectada a todos estos servicios

### Verificar que la aplicación está corriendo
```bash
# La aplicación debería estar disponible en el puerto 8083
curl http://localhost:8083/learning-reactive/debug/ping
```

Respuesta esperada:
```json
{"message":"API is working!","timestamp":"2025-07-03T17:xx:xx"}
```

### Solo si necesitas ejecutar la aplicación localmente (sin Docker)
```bash
# Compilar el proyecto
./gradlew clean build

# Ejecutar la aplicación (asegúrate que los servicios de Docker estén corriendo)
./gradlew bootRun

# Para modo debug con más logs
./gradlew bootRun --args='--debug'
```

## 📝 Requerimientos del Challenge y Cómo Probarlos

### 1️⃣ Cálculo con porcentaje dinámico

#### Requisitos
- Endpoint REST que recibe dos números (num1 y num2)
- Suma los números y aplica un porcentaje adicional obtenido de un servicio externo
- El resultado debe ser (num1 + num2) + (num1 + num2) * porcentaje

#### Cómo probar

```bash
# Realizar una solicitud al endpoint de cálculo
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10.0, "number_2": 20.0}' http://localhost:8083/learning-reactive/calculation
```

Respuesta esperada:
```json
{"result":45.60,"num1":10.0,"num2":20.0}
```

> El resultado 45.60 indica que el servicio externo devolvió un porcentaje de 0.52 (52%)
> Cálculo: (10 + 20) + (10 + 20) * 0.52 = 30 + 15.60 = 45.60

### 2️⃣ Caché del porcentaje

#### Requisitos
- El porcentaje obtenido del servicio externo debe almacenarse en Redis
- Si el servicio externo falla, se debe usar el último valor en caché
- Si no hay valor en caché, la API debe responder con error HTTP adecuado

#### Cómo probar

```bash
# 1. Limpiar la caché de Redis
curl -X DELETE http://localhost:8083/learning-reactive/debug/clear-cache

# 2. Realizar una solicitud al endpoint de cálculo - Debería fallar con timeout o usar caché
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 15.0, "number_2": 25.0}' http://localhost:8083/learning-reactive/calculation

# 3. Realizar otra solicitud - Debería usar el valor en caché si se pudo obtener en el paso anterior
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 5.0, "number_2": 10.0}' http://localhost:8083/learning-reactive/calculation
```

> Si la respuesta es un error 503 Service Unavailable, significa que no había valor en caché y el servicio externo falló
> Si la respuesta es un cálculo exitoso, significa que se usó el valor en caché

### 3️⃣ Reintentos ante fallos del servicio externo

#### Requisitos
- Implementar lógica de reintento con máximo 3 intentos
- Si después de los reintentos sigue fallando, enviar evento a Kafka
- Consumir el evento de Kafka y loguearlo

#### Cómo probar

```bash
# 1. Ajustar los timeouts para forzar errores (Ya configurado en application.yml)
# timeout de API externo: 2 segundos
# backoff inicial: 0.5 segundos
# backoff máximo: 2 segundos

# 2. Limpiar caché y hacer una solicitud para forzar reintentos
curl -X DELETE http://localhost:8083/learning-reactive/debug/clear-cache
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 7.5, "number_2": 12.5}' http://localhost:8083/learning-reactive/calculation
```

> Verificar en los logs de la aplicación:
> 1. Mensajes de reintento: "ABOUT TO RETRY #1/2 FOR BigDecimal"
> 2. Mensaje de retry agotado: "RETRY EXHAUSTED FOR BigDecimal"
> 3. Publicación del evento: "RETRY EXHAUSTED EVENT RECEIVED"
> 4. Mensaje de Kafka: "RECEIVED MESSAGE FROM TOPIC CR_RETRY_EXHAUSTED"

### 4️⃣ API de usuarios autorizados

#### Requisitos
- Endpoints REST para alta y baja de usuarios autorizados
- Los usuarios deben guardarse en PostgreSQL usando R2DBC

#### Cómo probar

```bash
# 1. Crear un nuevo usuario
curl -X POST -H "Content-Type: application/json" -d '{"username":"testuser","email":"test@example.com"}' http://localhost:8083/learning-reactive/users

# 2. Obtener todos los usuarios
curl -X GET http://localhost:8083/learning-reactive/users

# 3. Obtener un usuario específico por ID (reemplazar {id} con un ID válido)
curl -X GET http://localhost:8083/learning-reactive/users/{id}

# 4. Desactivar un usuario (reemplazar {id} con un ID válido)
curl -X DELETE http://localhost:8083/learning-reactive/users/{id}
```

### 5️⃣ Bonus: Historial de llamadas

#### Requisitos
- Endpoint para consultar historial de llamadas a la API
- El registro debe ser asíncrono y almacenado en MongoDB
- Solo usuarios autorizados pueden acceder al historial

#### Cómo probar

```bash
# Consultar el historial de llamadas
curl -X GET http://localhost:8083/learning-reactive/history
```

Respuesta esperada:
```json
[
  {
    "id":"...",
    "timestamp":[2025,7,3,17,xx,xx],
    "endpoint":"/learning-reactive/calculation",
    "method":"POST",
    "parameters":"{\"number_1\": 10.0, \"number_2\": 20.0}",
    "response":"...",
    "successful":true
  },
  ...
]
```

### 6️⃣ Bonus: Manejo de errores HTTP

#### Requisitos
- Validaciones de input en los endpoints
- Manejo adecuado de errores HTTP (4XX y 5XX)

#### Cómo probar

```bash
# 1. Probar error de validación enviando formato incorrecto
curl -X POST -H "Content-Type: application/json" -d '{"num1": 10.0, "num2": 20.0}' http://localhost:8083/learning-reactive/calculation

# 2. Probar error de servicio (después de limpiar caché)
curl -X DELETE http://localhost:8083/learning-reactive/debug/clear-cache
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10.0, "number_2": 20.0}' http://localhost:8083/learning-reactive/calculation
```

## 🧪 Ejecución de Tests

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar solo tests unitarios
./gradlew test --tests "*UnitTest"

# Ejecutar solo tests de integración
./gradlew test --tests "*IntegrationTest"
```

## 📚 Arquitectura y Tecnologías

### Tecnologías utilizadas
- **Spring WebFlux**: Framework reactivo para aplicaciones web
- **Project Reactor**: Biblioteca reactiva para Java
- **R2DBC**: API reactiva para acceso a bases de datos relacionales
- **MongoDB Reactive**: Driver reactivo para MongoDB
- **Redis Reactive**: Cliente reactivo para Redis
- **Kafka Reactive**: Cliente reactivo para Apache Kafka

### Patrones de diseño implementados
- **Factory Pattern**: Para creación de objetos de configuración y respuestas HTTP
- **Strategy Pattern**: Para diferentes estrategias de reintentos y manejo de errores
- **Repository Pattern**: Para acceso a datos de manera reactiva
- **Circuit Breaker Pattern**: Para protección contra fallos en servicios externos

### Principios SOLID aplicados
- **Single Responsibility Principle**: Cada componente tiene una única responsabilidad
- **Open/Closed Principle**: Extensión sin modificación a través de interfaces y configuración
- **Dependency Inversion Principle**: Dependencia de abstracciones, no de implementaciones

## 🔍 Estructura del Proyecto

```
src/main/java/cl/tenpo/learning/reactive/tasks/task2/
├── application/           # Capa de aplicación (servicios)
├── domain/                # Modelos y entidades de dominio
├── infrastructure/        # Adaptadores e infraestructura
│   ├── boot/             # Inicialización de componentes
│   ├── cache/            # Servicios de caché (Redis)
│   ├── client/           # Clientes HTTP reactivos
│   ├── config/           # Configuraciones
│   ├── event/            # Eventos y listeners
│   ├── exception/        # Manejo global de errores
│   ├── factory/          # Factories para creación de objetos
│   ├── filter/           # Filtros web reactivos
│   ├── persistence/      # Repositorios reactivos
│   └── retry/            # Estrategias de reintentos
└── presentation/         # Capa de presentación
    ├── controller/       # Controladores REST
    ├── dto/              # Objetos de transferencia de datos
    └── handler/          # Handlers funcionales