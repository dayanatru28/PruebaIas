# BancoIAS - Core de Preaprobados Bancarios
**Solución Técnica Fullstack: Spring Boot WebFlux & Angular 17**  
**Candidata:** Leidy Dayana Ortega Trujillo  

Este repositorio contiene la solución completa para la prueba técnica de **Desarrolladora Fullstack Java WebFlux / Angular** para BancoIAS.

---

## 📌 1. Arquitectura y Tecnologías

* **Backend:** Java 21 LTS, Spring Boot 3.3.4, Spring WebFlux (Netty reactivo no bloqueante), Spring Data R2DBC, H2 Database en memoria, Jakarta Validation, JUnit 5, Mockito, Project Reactor Test (`StepVerifier`), Spring AMQP (RabbitMQ).
* **Frontend:** Angular 17 Standalone Components, TypeScript, RxJS, HttpClient, CSS3 responsive.
* **Mensajería (Punto Opcional):** RabbitMQ con tolerancia a fallos y `docker-compose.yml`.

---

## 🚀 2. Instrucciones de Ejecución

### Prerrequisitos
* **Java:** JDK 21 instalado y configurado en `JAVA_HOME`.
* **Maven:** 3.9+ instalado.
* **Node.js:** v20+ y **npm** v10+.
* **Angular CLI:** v17+ (`npm install -g @angular/cli`).
* *(Opcional)* **Docker Desktop** para levantar RabbitMQ si se desea probar el broker.

---

### A. Ejecutar el Backend (Spring Boot WebFlux)

1. Abre una terminal y navega a la carpeta `backend`:
   ```bash
   cd backend
   ```
2. Compila y ejecuta la aplicación:
   ```bash
   mvn spring-boot:run
   ```
3. El servidor iniciará en el puerto **8080** con Netty y cargará automáticamente la base de datos reactiva H2 con los datos semilla iniciales (`PRA-1001`, `PRA-1002`, `PRA-2001`).

#### Ejecutar las Pruebas Automatizadas:
Para correr la suite de 9 pruebas unitarias, de integración y concurrencia:
```bash
mvn test
```

---

### B. Ejecutar el Frontend (Angular 17)

1. Abre una segunda terminal y navega a la carpeta `frontend`:
   ```bash
   cd frontend
   ```
2. Instala las dependencias (si aún no las tienes):
   ```bash
   npm install
   ```
3. Inicia el servidor de desarrollo:
   ```bash
   npm start
   ```
4. Abre tu navegador web e ingresa a:
   👉 **[http://localhost:4200](http://localhost:4200)**

---

### C. Levantar RabbitMQ (Punto Opcional - Sección 9)

Si deseas probar la publicación reactiva de eventos en RabbitMQ:
```bash
docker compose up -d
```
* **Broker AMQP:** `localhost:5672`
* **Consola Web de Administración:** [http://localhost:15672](http://localhost:15672) (Usuario: `guest`, Contraseña: `guest`).

> **Nota de Resiliencia:** Si decides no levantar Docker/RabbitMQ, el backend continuará funcionando normalmente; el publicador capturará la ausencia del broker en logs sin bloquear la transacción ni arrojar errores al usuario.

---

## 📡 3. Endpoints Principales de la API REST

| Método | Endpoint | Descripción | Requisito |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/requests` | Procesa una solicitud de uso de preaprobado aplicando reglas, concurrencia e idempotencia. | RF01 - RF05 |
| `GET` | `/api/v1/requests/{reference}` | Consulta el resultado y detalle de una solicitud por su referencia única. | RF06 |
| `GET` | `/api/v1/requests` | Consulta el historial de solicitudes procesadas recientemente ordenadas por fecha. | RF06 |
| `GET` | `/api/v1/preapproved` | Consulta todos los cupos preaprobados del banco. | Soporte |
| `GET` | `/api/v1/preapproved/customer/{customerId}` | Consulta los cupos preaprobados correspondientes a un cliente (ej: `USR-10`). | RF07 |

---

## 📋 4. Criterios de Completitud y Requisitos Cumplidos

| ID | Requisito | Estado | Evidencia |
| :--- | :--- | :---: | :--- |
| **RF01** | Procesar solicitud de uso con fecha/hora | ✅ Cumplido | `UsageRequestService.java`, persistencia en H2 con timestamp. |
| **RF02** | Validación de reglas de negocio | ✅ Cumplido | Monto $> 0$, preaprobado existente, cliente coincidente, estado activo, monto $\le$ cupo. |
| **RF03** | Conservar resultado (autorizada/rechazada con motivo) | ✅ Cumplido | Entidad `UsageRequest`, solicitudes rechazadas no descuentan saldo. |
| **RF04** | Manejo de solicitudes simultáneas (Concurrencia) | ✅ Cumplido | Actualización condicional a nivel de BD (`available_amount >= :amount`). Test automatizado paralelo. |
| **RF05** | Manejo de referencias repetidas (Idempotencia) | ✅ Cumplido | Búsqueda previa por `requestReference`, no doble cobro, inmutabilidad ante datos alterados. |
| **RF06** | Consultar solicitudes por referencia e historial | ✅ Cumplido | Endpoints GET por referencia e historial reciente por fecha descendente. |
| **RF07** | Interfaz Angular funcional e integrada | ✅ Cumplido | Feature modular `PreApprovedComponent`, tarjetas, formulario, recibos y tabla reactiva. |
| **Opcional**| Publicación en RabbitMQ | ✅ Cumplido | `RabbitMqEventPublisher.java` + `docker-compose.yml`. |

---

## 📄 5. Documentos Adicionales de la Entrega

* 📘 [DECISIONES_TECNICAS.md](DECISIONES_TECNICAS.md): Justificación arquitectónica formal de la solución (persistencia reactiva, concurrencia en BD, idempotencia, organización frontend y trade-offs).
* 🤖 [USO_IA.md](USO_IA.md): Declaración transparente sobre el uso de herramientas de Inteligencia Artificial durante el desarrollo (Sección 8 del enunciado).
