# Registro de Uso de Inteligencia Artificial

**Candidata:** Leidy Dayana Ortega Trujillo  
**Proyecto:** Core de Preaprobados Bancarios (Fullstack Java WebFlux / Angular)  

En cumplimiento con la Sección 8 del documento de la prueba, comparto de forma transparente cómo y para qué me apoyé en herramientas de Inteligencia Artificial durante el desarrollo de este ejercicio:

---

### 1. Actividades en las que utilicé la IA

* **Estructura y sintaxis de Spring WebFlux:**  
  Anteriormente yo manejaba y tenía conocimientos sobre Spring Boot MVC tradicional. Al ser WebFlux un paradigma reactivo diferente, utilicé la IA como apoyo para comprender la sintaxis, la gramática de las funciones reactivas (`Mono` y `Flux`), la configuración del proyecto y la forma correcta de construir las consultas hacia la base de datos con R2DBC.

* **Estrategia para el manejo de solicitudes masivas:**  
  Consulté con la IA cuál era la forma más práctica y mejor pensada en la industria para manejar solicitudes masivas y simultáneas sobre un mismo cupo. A través de este análisis se llegó a la conclusión de que la mejor alternativa era resolverlo directamente a nivel de base de datos mediante una actualización condicional, en lugar de intentar controlarlo con candados en memoria de la aplicación.

* **Diseño y creación de las pruebas automatizadas:**  
  Para la ejecución de las pruebas se hizo necesario el uso de la IA. Aunque tengo conocimientos básicos sobre la importancia de las pruebas, no tenía conceptos claros a nivel de código sobre la estructura y el funcionamiento de pruebas reactivas (con herramientas como `StepVerifier`). Por esta razón, el diseño y la creación del código de las pruebas automatizadas se realizaron mediante el apoyo de la IA.

* **Diseño visual y experiencia de usuario en Angular:**  
  El documento mencionaba que *“no se evalúa diseño gráfico avanzado; la interfaz debe ser funcional y evidenciar integración real con el backend”*. Luego de que yo definí la estructura básica del frontend, los servicios, los componentes y la conexión con el backend, le solicité a la IA que creara en código la estructura visual y los estilos para la página web del banco, logrando así un diseño mucho más limpio, agradable y con una mejor experiencia de usuario.

---

### 2. Cómo validé los resultados

* **Compilación y ejecución:** Cada bloque generado se compiló localmente con Maven y Angular CLI para asegurar que no contuviera errores sintácticos.
* **Validación de pruebas:** Ejecuté las pruebas automatizadas con `mvn test`, comprobando que los 9 casos pasaran en verde con 0 errores y confirmaran las reglas de negocio.
* **Prueba manual en el navegador:** Validé la integración real directamente desde la pantalla en `http://localhost:4200`, realizando solicitudes reales, comprobando el descuento de saldo, el rechazo por cupos bloqueados y la respuesta ante saldos insuficientes.

---

### 3. Consideraciones de seguridad y datos sensibles

* Durante toda la interacción con la IA se utilizaron únicamente los datos de ejemplo proporcionados en el documento (`USR-10`, `PRA-1001`, `REF-001`, etc.).
* En ningún momento se ingresaron contraseñas, credenciales reales, tokens ni información personal o confidencial.
