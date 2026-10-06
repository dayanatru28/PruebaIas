# Registro de Decisiones Técnicas - BancoIAS

**Candidata:** Leidy Dayana Ortega Trujillo  
**Proyecto:** Core de Preaprobados Bancarios (Fullstack Java WebFlux / Angular)  

Este documento describe las decisiones técnicas y de arquitectura adoptadas para resolver los requerimientos de la prueba técnica de BancoIAS, detallando el contexto, las alternativas evaluadas, las justificaciones y cómo se validó cada solución.

---

## 1. Persistencia y Backend Reactivo (Spring Boot WebFlux + R2DBC)

### Contexto y problema:
El enunciado exigía de forma obligatoria el uso de **Spring Boot WebFlux** y que las solicitudes procesadas fueran persistidas.
### Alternativas consideradas:
* **Alternativa A:** Utilizar la persistencia tradicional con JPA / Hibernate y JDBC hacia una base de datos externa (como PostgreSQL u Oracle).
* **Alternativa B (Adoptada):** Utilizar **Spring Data R2DBC** con base de datos **H2 en memoria**.
### Justificación de la decisión:
* Elegí **WebFlux** porque es un framework que permite crear aplicaciones totalmente no bloqueantes. En lugar de detener un proceso esperando que la base de datos responda, permite continuar atendiendo solicitudes concurrentes y reaccionar cuando el resultado esté disponible.
* Si utilizara JPA o JDBC tradicional en un entorno WebFlux, las llamadas a la base de datos serían bloqueantes y arruinarían las ventajas del modelo reactivo. **R2DBC permite acceder a la base de datos de manera 100% no bloqueante, manteniendo un flujo de datos consistente de inicio a fin.
* Se utilizó **H2 en memoria** porque la prueba permitía no conectarse a un servidor externo pesado ni requerir configuraciones complejas para el evaluador. Esto hace que el proyecto sea mucho más ágil de probar, reduce los tiempos de consulta y ejecución al no depender de latencia de red, y garantiza que la solución se levante de inmediato con solo clonar el repositorio.

---

## 2. Manejo de Concurrencia y Consistencia del Cupo
### Contexto y problema:
El core de BancoIAS puede recibir dos o más solicitudes prácticamente en el mismo milisegundo sobre el mismo preaprobado (por ejemplo, dos compras de $600.000 sobre un saldo de $1.000.000). El sistema no puede permitir sobregiros ni saldos negativos.

### Alternativas consideradas:
* **Alternativa A:** Bloqueos en memoria de código en Java utilizando `synchronized` o candados (`ReentrantLock`).
* **Alternativa B (Adoptada):** **Actualización condicional a nivel de base de datos** mediante consulta SQL:
  ```sql
  UPDATE pre_approved 
  SET available_amount = available_amount - :amount, version = version + 1 
  WHERE id = :id AND available_amount >= :amount AND status = 'ACTIVE'
  ```
### Justificación de la decisión:
* Dejar las validaciones críticas únicamente en el código de la aplicación puede fallar cuando hay ráfagas de solicitudes simultáneas. Si dos procesos leen el saldo al mismo tiempo, ambos verían que hay $1.000.000 y ambos intentarían descontar.
* La base de datos resuelve las escrituras 1 a 1 (fila por fila). Al colocar la condición `available_amount >= :amount AND status = 'ACTIVE'` directamente dentro de la sentencia de actualización, obligamos al motor a verificar el saldo en el instante exacto de hacer el descuento. Si la condición se cumple, descuenta y devuelve 1 fila afectada (AUTORIZADA); si no alcanza, devuelve 0 filas afectadas y el sistema la rechaza inmediatamente. Esto evita sobrecargos, modificaciones inconsistentes y protege el dinero del banco.
* **¿Qué pasaría si la aplicación crece y tenemos varios servidores?**
  Esta es la razón de mayor peso: si en un futuro BancoIAS escala a múltiples servidores o microservicios en la nube, los candados en memoria de Java (`synchronized`) solo funcionarían dentro de una sola máquina; un servidor no sabría lo que el otro tiene en su memoria y podrían generar sobregiros entre ellos. En cambio, al delegar la responsabilidad en la base de datos que todos comparten, la consistencia del saldo queda garantizada sin importar cuántos servidores tengamos corriendo en paralelo.

---

## 3. Manejo de Idempotencia y Referencias Repetidas 

### Contexto y problema:
Por caídas de red, parpadeos de conexión o reintentos de los canales, una misma factura (`requestReference`) puede ser enviada más de una vez. También puede ocurrir que una referencia repetida llegue con valores o información alterada.

### Alternativas consideradas:
* **Alternativa A:** Sobreescribir el registro previo o recalcular el descuento.
* **Alternativa B (Adoptada):** Verificación previa por referencia y respuesta del registro original sin doble cobro.

### Justificación de la decisión:
* Antes de procesar cualquier reducción de saldo, el sistema consulta primero si la referencia ya existe en la tabla `usage_request`.
* Si la referencia ya existe y trae los mismos datos, se retorna inmediatamente el resultado original que ya estaba guardado, **sin volver a descontar un solo peso del cupo**. Esto protege al cliente de cobros duplicados por fallas de plataforma.
* Si la referencia llega con información alterada, tal como lo especifica el enunciado de la prueba, **se preserva la solicitud original intacta en la base de datos** y se devuelve la respuesta original de forma controlada, evitando fraudes o corrupción del historial de solicitudes. Si el cliente necesita hacer una compra distinta, debe emitir una referencia nueva.

---

## 4. Organización Modular del Frontend (Angular 17)

### Contexto y problema:
El enunciado solicita una interfaz funcional que evidencie integración real con el backend. Podría haberse resuelto colocando todo en el componente principal `app.component`.

### Alternativas consideradas:
* **Alternativa A:** Colocar toda la lógica, formularios y llamadas HTTP en un solo componente y servicio monolítico.
* **Alternativa B (Adoptada):** Estructura modular orientada a dominio con separación de servicios y componentes específicos.

### Justificación de la decisión:
* Concebimos el frontend pensando en un proyecto bancario empresarial a gran escala. La administración de preaprobados se diseñó como un componente independiente (`PreApprovedComponent`) dentro de `features/pre-approved/`, dejando `app.component` únicamente como la carcasa institucional del banco (header, layout y footer).
* **División de servicios:** Se crearon dos servicios dedicados que reflejan exactamente la arquitectura del backend:
  * `PreApprovedService`: Se encarga de consultar los cupos y saldos (`/api/v1/preapproved`).
  * `UsageRequestService`: Se encarga de enviar las solicitudes de uso y consultar el historial (`/api/v1/requests`).
* Esta separación aplica el **Principio de Responsabilidad Única (SRP)**, manteniendo un orden limpio. Permite que BancoIAS pueda añadir nuevos módulos en el futuro (cuentas de ahorros, préstamos, tarjetas) sin alterar ni afectar lo que ya está desarrollado.

---

## 5. Estrategia de Validación y Pruebas

Para garantizar que cada decisión técnica funcionara correctamente, se implementaron dos niveles de validación:

1. **Pruebas Automatizadas en Backend (JUnit 5 + Mockito + Reactor Test):**
   * Se utilizó `WebTestClient` y `Mockito` para validar que los controladores y rutas REST respondieran con los códigos HTTP y contratos JSON esperados.
   * Se utilizó `StepVerifier` de Project Reactor para verificar los flujos reactivos.
   * **Prueba de Concurrencia Extrema:** Se diseñó una prueba automatizada ejecutando dos solicitudes paralelas de $600.000 con `Schedulers.parallel()` sobre un saldo de $1.000.000. La prueba demostró que exactamente una fue autorizada, la otra fue rechazada y el saldo final quedó en $400.000 sin sobregiro.
   * **Prueba de Idempotencia:** Se validó que al enviar la misma referencia dos veces consecutivas, el saldo solo se debitara en la primera llamada.
   * *Resultado final:* **9 pruebas ejecutadas, 0 fallos, 0 errores (BUILD SUCCESS).**

2. **Validación Real de Integración desde el Frontend:**
   * Se comprobó directamente desde la interfaz gráfica en el navegador (`http://localhost:4200`), realizando pruebas reales de compras válidas, intentos sobre cupos bloqueados (`PRA-1002`), compras que excedían el saldo disponible y verificación visual inmediata del cambio de saldo en las tarjetas financieras.
