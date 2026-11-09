# Práctica final UCO Kudos IS2
## **Estudiante:** Juan Pablo Avendaño Duque

Este Documento aborda el desarrollo de UCO Kudos cómo práctica final para la asignatura de Ingeniería de Software 2.

---
## Contenido
1. [Qué es UCO Kudos](#1--qué-es-uco-kudos)
2. [Estrategia agéntica](#2-estrategia-agentica)

---

## 1 · Qué es UCO Kudos
Plataforma institucional para que la comunidad UCO se reconozca públicamente. La línea base demuestra el flujo
completo de **Publicar Kudo**:

- Persistencia del reconocimiento (con archivo adjunto opcional).
- Correo electrónico al destinatario.
- Mensaje de WhatsApp al destinatario.
- Notificación push en tiempo real si el destinatario está conectado.
- Registro de auditoría de cada paso.

Consideraciones: ~90 % de los usuarios desde el celular, alto volumen de lecturas, adjuntos, y tres canales de
notificación.

---

# 2 Estrategia Agentica
Se busca el uso responsable de la inteligencia artifical, por ende se mencionarán a grandes rasgos las **practicas relacionadas con el uso de inteligencia artifical**, mostrando las herramientas que fueron utilizadas a grandes rasgos para el desarrollo y estudio de este producto.

1. **Biblioteca MCP:** Herramienta de consulta RAG que cuenta con libros y repositorios de la bibliografia compartida ademmás de contenido traido con ***Web Scraping*** Además de imagenes alimentadas que también son consultadas y utilizadas para citar libros, diagramas y documentación oficial obtenida usandolos cómo parte de una respuesta agentica.
2. **SPEC-KIT + Skills:** Utilizando herramientas públicas para agentes se añadieron herramientas de desarrollo que definen al agente un camino común de desarrollo para preguntar y definir un comportamiento robusto o profesional, además el uso con skills propias permitió el comportamiento de estudio deseado del agente garantizando preguntas, explicación y entendimiento del código antes de terminar un desarrollo.
3. **Separación de entornos:** Se implementaron ***Guardrails*** al agente de trabajo para que este sólo pueda trabajar sobre una copia local que nunca toca el repositorio, permitiendo la lectura y el review pero no una modificación directa sobre el código.
4. **Expertos de un solo tema.** Subagentes especializados, uno por práctica evaluada (Twelve-Factor, DevSecOps,
   OWASP, C4, etc) y por tecnología, que revisan solo su tema y citan la fuente.
5. **Pruebas adversarias.** Las pruebas generadas con IA dejan huecos; se complementan con agentes atacantes que
   buscan fallos que las pruebas no detectan, dando un rol de uso diferente por agente con tal de buscar el funcionamiento no esperado.
6. **Validación humana.** Cada decisión queda registrada como ADR con su autor; lo generado por IA se marca como
   borrador hasta que el estudiante lo revisa, cumpliendo así la practica **human in the loop**.
7. **Verificación de diagramas.** Los diagramas C4 los revisan los expertos de C4 y de Azure, y Archify valida el
   modelo. La disposición final de las cajas la hace el estudiante a mano en draw.io.
   ---

## 3 · Arquitectura
La arquitectura es la práctica completa: todo lo que sigue (diagramas, decisiones, código, pruebas y despliegue)
vive dentro de esta sección y se va agregando a medida que queda listo. Los diagramas están en un solo archivo con
tres páginas: [`docs/alto-nivel/c4-uco-kudos.drawio`](docs/alto-nivel/c4-uco-kudos.drawio). Las decisiones, en
[`docs/adr/`](docs/adr/).

Mientras se dibujaban los diagramas se fue definiendo cómo se comporta el sistema; esas decisiones quedan como
citas cortas debajo de cada uno.

### 3.1 · Diagrama de contexto
**Estado:** aceptado (v5, 8-nov-2026).

![C4 nivel 1 · Contexto](docs/alto-nivel/c4-1-contexto.png)

UCO Kudos es un solo sistema para el miembro de la comunidad: publica, lee y recibe Kudos desde el celular. Por
fuera solo quedan dos sistemas: Microsoft Entra External ID, que autentica, y Twilio, que entrega el WhatsApp. El
correo y el push van por dentro porque son servicios de Azure que solo se configuran.

> «El administrador no es otro actor: es un miembro de la comunidad con un rol más, que además puede ver la
> auditoría.»

> «El tenant de la UCO no nos deja registrar la aplicación, entonces usamos un tenant propio de Entra External ID.»

> «Construimos lo mínimo: el front y kudos-service. Todo lo demás se configura, no se programa.» (ADR-007)

La nota amarilla del diagrama guarda tres preguntas que siguen abiertas: si el reconocimiento es entre cualquier
par de miembros, qué pasa con un Kudo a alguien que nunca ha entrado y la creación del tenant.

### 3.2 · Diagrama de contenedores
**Estado:** aceptado (v13, 9-nov-2026).

![C4 nivel 2 · Contenedores](docs/alto-nivel/c4-2-contenedores.png)

El diagrama sigue el flujo de Publicar Kudo con números del 1 al 10. El front entra por el WAF y el API Gateway,
kudos-service guarda el Kudo con su outbox y el adjunto, publica `KudoPublicado` en el Message Broker y este lo
empuja al Notification Gateway, que avisa por correo, WhatsApp y push al mismo tiempo y deja cada paso en la
auditoría. El título de cada caja es el concepto; debajo, entre corchetes, la tecnología que lo resuelve.

> «Publicar no espera a las notificaciones: el Kudo se guarda, se responde y lo demás pasa por detrás.»

> «El evento se empuja, no se pregunta por él: por eso Event Grid y no una cola.» (ADR-007)

> «Si el mismo evento llega dos veces, nadie recibe dos correos.» (ADR-009)

> «Una sola puerta de entrada: todo pasa por el WAF, y aun así kudos-service revisa el token.» (ADR-006, ADR-013)

### 3.3 · Diagrama de despliegue
**Estado:** aceptado (v4, 9-nov-2026).

![C4 · Despliegue en Azure](docs/alto-nivel/c4-3-despliegue.png)

Muestra dónde corre cada contenedor del diagrama anterior: todo en una suscripción de Azure for Students, en la
región `brazilsouth`, más GitHub para el CI/CD. Se ven los planes elegidos, el service mesh mínimo de Container
Apps y los riesgos que se aceptan por costo.

> «No hay red privada porque no cabe en el crédito; cada capa revisa el token y lo que no cubre queda escrito.»
> (ADR-013)

> «Un pull request nunca despliega: solo `main`, después de Sonar y Trivy y con mi aprobación.» (ADR-014)

> «Con un solo servicio propio, la malla es la que trae la plataforma.» (ADR-008)

### 3.4 · Bloques de arquitectura y de solución (ABB → SBB)
**Estado:** borrador revisado (9-nov-2026). Tabla completa con origen y alternativas en
[`docs/alto-nivel/diseno-alto-nivel.md`](docs/alto-nivel/diseno-alto-nivel.md), §5.

El ABB es la capacidad que el sistema necesita, sin marca; el SBB es el producto que la resuelve. Tipo: **P** propio
(código), **A** administrado de Azure (configuración), **E** externo.

> «El concepto se queda; el producto puede cambiar. Cambiar el producto es una ADR nueva, no otra arquitectura.»

| # | ABB | SBB | Tipo | ADR |
|---|---|---|---|---|
| 1 | Front web | SPA Vue 3 + TypeScript en Blob `$web` | P | 002 |
| 2 | Servicio de Kudos | Java 21 + Spring Boot en Container Apps | P | 007, 013 |
| 3 | Publicación fiable de eventos (outbox) | Outbox en PostgreSQL + relevo `@Scheduled` | P | 010 |
| 4 | Identity Provider | Microsoft Entra External ID | E | 013 |
| 5 | WAF | Azure Front Door Standard + política WAF | A | 006, 013 |
| 6 | CDN | Azure Front Door | A | 006 |
| 7 | API Gateway | Azure API Management Consumption | A | 006, 013 |
| 8 | Message Broker | Azure Event Grid + dead-letter en Blob | A | 007 |
| 9 | Notification Gateway | Azure Logic Apps Consumption | A | 007, 009 |
| 10 | Servicio de correo | Azure Communication Services Email | A | 006 |
| 11 | Mensajería WhatsApp | Twilio WhatsApp API | E | 006 |
| 12 | Real-time Push | Azure Web PubSub Free | A | 016 |
| 13 | Base de datos | Azure Database for PostgreSQL Flexible | A | 010, 013 |
| 14 | Caché | Azure Managed Redis | A | 011, 017 |
| 15 | Almacén de adjuntos | Azure Blob Storage + SAS de delegación | A | 013 |
| 16 | Registro de auditoría | Azure Table Storage | A | 012 |
| 17 | Data Mesh | Azure Data Factory + Blob con catálogo | A | 015 |
| 18 | Service Mesh | Entorno de Container Apps (Envoy + mTLS) | A | 008 |
| 19 | Gestor de secretos | Azure Key Vault | A | 003, 013 |
| 20 | Parameter Catalog | Azure App Configuration | A | 003 |
| 21 | Observabilidad | Application Insights + Log Analytics | A | — |
| 22 | Pipeline CI/CD | GitHub Actions + OIDC | E | 014 |
| 23 | Análisis estático | SonarQube Cloud | E | 014 |
| 24 | Escáner de vulnerabilidades | Trivy | E | 014 |
| 25 | Registro de imágenes | GitHub Container Registry | E | 014 |
| 26 | Ejecución de contenedores | Azure Container Apps | A | 008, 014 |

