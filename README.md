# Microservicio de Catálogo de Productos (API REST + JSF)

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Supported-2496ED?style=flat-square&logo=docker&logoColor=white)

Este repositorio contiene la resolución integral a la prueba técnica **"Desarrollador Backend"**. Se ha construido un Microservicio de Catálogo de Productos robusto y escalable, cumpliendo rigurosamente con los lineamientos de arquitectura, validación, pruebas y despliegue exigidos.

---

## 🚀 Cómo Empezar (Ejecución Local)

### Prerrequisitos
- **Java 17** o superior.
- **Maven** (Opcional, incluye el wrapper).
- **Docker** (Opcional, para ejecución containerizada).

### Opción A: Compilación y Ejecución Manual
1. Clona el repositorio e ingresa al directorio del proyecto.
2. Ejecuta la aplicación usando Maven:
   ```bash
   mvn spring-boot:run
   ```
3. La aplicación se levantará en el puerto `9090`.

### Opción B: Ejecución con Docker (Altamente Valorado)
Se incluye un `Dockerfile` optimizado en el repositorio usando la imagen ligera de Alpine.
1. Construir la imagen:
   ```bash
   docker build -t catalogo-microservicio .
   ```
2. Levantar el contenedor:
   ```bash
   docker run -p 9090:9090 catalogo-microservicio
   ```

---

## 🎯 Puntos de Acceso Clave

Una vez en ejecución, puedes visitar las siguientes URLs:

- **1. Interfaz Gráfica de Usuario (Dashboard JSF):**
  [http://localhost:9090/index.xhtml](http://localhost:9090/index.xhtml)
  *(Una interfaz limpia para gestionar visualmente los productos).*

- **2. Documentación OpenAPI / Swagger:**
  [http://localhost:9090/swagger-ui/index.html](http://localhost:9090/swagger-ui/index.html)
  *(Documentación interactiva de la API REST).*

- **3. Base de Datos H2 (Consola Web):**
  [http://localhost:9090/h2-console](http://localhost:9090/h2-console)
  *URL: `jdbc:h2:mem:catalogdb` | User: `sa` | Password: (vacío)*

---

## 🛡️ Uso de la API REST y Seguridad

Las operaciones sobre la interfaz web (`index.xhtml`) son de acceso público. Sin embargo, para simular un entorno B2B, **todos los endpoints `/api/**` están protegidos** mediante **Spring Security Basic Auth**.

Para interactuar vía Postman o Swagger, usa las siguientes credenciales:
- **Usuario:** `admin`
- **Contraseña:** `123`

### Endpoints Disponibles
- `GET /api/products` (Soporta paginación: `?page=0&size=10`)
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `PATCH /api/products/{id}/stock` (Actualización atómica de existencias)
- `DELETE /api/products/{id}` (Borrado lógico)
- `PATCH /api/products/{id}/reactivate` (Reactivación de inventario)

---

## 🏛️ Decisiones de Arquitectura y Cumplimiento de Rúbrica

El proyecto fue diseñado específicamente para abarcar y superar los criterios de evaluación:

1. **Arquitectura Limpia y DTOs:** Separación estricta en capas (`controller`, `service`, `repository`, `model`). Nunca se exponen las Entidades directamente a la API; se utiliza **MapStruct** para un mapeo bidireccional automático y seguro hacia `ProductDTO`.
2. **Validaciones Estríctas:** Implementación de anotaciones de validación de Jakarta (`@NotNull`, `@NotBlank`, `@Min`) en los DTOs.
3. **Manejo de Excepciones Globales:** Se implementó un `@RestControllerAdvice` (`GlobalExceptionHandler`) para capturar errores como JSON mal formados, Validaciones Fallidas (400) y Entidades no Encontradas (404), devolviendo respuestas amigables y formateadas, evitando StackTraces de servidor (500).
4. **Borrado Lógico y Auditoría:** `DELETE` oculta el registro (`active=false`) previniendo pérdida de historial financiero. Se añadió un endpoint de reactivación.
5. **Reglas de Negocio en Capa de Servicios:** Se impide transaccionalmente registrar "stock negativo" al procesar ventas (`updateStock`).
6. **(BONO) Integración de API Pública:** Si se envía un producto sin descripción durante un POST, el servicio hace una llamada REST a `fakestoreapi.com` para inyectar una descripción de respaldo automáticamente.

---

## 🧪 Pruebas Automatizadas (Testing)

Se ha cumplido con la rúbrica de probar la Lógica de Negocio real utilizando un entorno aislado.

Para correr la suite de pruebas desde la terminal:
```bash
mvn test
```

### Cobertura Implementada:
1. **Pruebas de Integración (`@SpringBootTest`):** Validan flujos transaccionales reales con la base H2 en memoria, asegurando que las reducciones de stock y borrados lógicos funcionen sistémicamente. (Clase: `BusinessLogicIntegrationTest.java`).
2. **Pruebas Unitarias Aisladas (JUnit 5 + Mockito):** La capa de servicio (`ProductServiceImplTest.java`) cuenta con pruebas puramente aisladas usando `@Mock` e `@InjectMocks` para garantizar velocidad y comportamiento esperado sin acceso real a base de datos.
