

# Práctica final UCO Kudos IS2
## **Estudiante:** Juan Pablo Avendaño Duque

Este Documento aborda el desarrollo de UCO Kudos cómo práctica final para la asignatura de Ingeniería de Software 2.

---
## Contenido
1. [Qué es UCO Kudos](#1--qué-es-uco-kudos)
2. [Estrategia agéntica](#2-estrategia-agentica)
3. [Arquitectura](#3--arquitectura)
   1. [Diagrama de contexto](#31--diagrama-de-contexto)
   2. [Diagrama de contenedores](#32--diagrama-de-contenedores)
   3. [Diagrama de despliegue](#33--diagrama-de-despliegue)
   4. [Bloques de arquitectura y de solución (ABB → SBB)](#34--bloques-de-arquitectura-y-de-solución-abb--sbb)
   5. [Atributos de calidad](#35--atributos-de-calidad)
   6. [Diagrama de componentes](#36--diagrama-de-componentes)
   7. [Diseño detallado](#37--diseño-detallado)
4. [Decisiones de arquitectura (ADR)](#4--decisiones-de-arquitectura-adr)
5. [Riesgos y trade-offs](#5--riesgos-y-trade-offs)
6. [Seguridad](#6--seguridad)
7. [Cloud native y observabilidad](#7--cloud-native-y-observabilidad)
8. [CI/CD y pruebas](#8--cicd-y-pruebas)
9. [Despliegue](#9--despliegue)
10. [Uso responsable de IA](#10--uso-responsable-de-ia)

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
2. **Skills de desarrollo:** Utilizando herramientas públicas para agentes se añadieron herramientas de desarrollo que definen al agente un camino común de desarrollo para preguntar y definir un comportamiento robusto o profesional, además el uso con skills propias permitió el comportamiento de estudio deseado del agente garantizando preguntas, explicación y entendimiento del código antes de terminar un desarrollo.
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
cuatro páginas: [`docs/Diseño/Alto Nivel/c4-uco-kudos.drawio`](docs/Dise%C3%B1o/Alto%20Nivel/c4-uco-kudos.drawio).

Mientras se dibujaban los diagramas se fue definiendo cómo se comporta el sistema; esas decisiones quedan como
citas cortas debajo de cada uno.

### 3.1 · Diagrama de contexto
**Estado:** aceptado (v5, 8-nov-2026).

![C4 nivel 1 · Contexto](docs/Dise%C3%B1o/Alto%20Nivel/Contexto.jpg)

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

![C4 nivel 2 · Contenedores](docs/Dise%C3%B1o/Alto%20Nivel/Contenedores.jpg)

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

![C4 · Despliegue en Azure](docs/Dise%C3%B1o/Alto%20Nivel/Despiegue.jpg)

Muestra dónde corre cada contenedor del diagrama anterior: todo en una suscripción de Azure for Students, en la
región `brazilsouth`, más GitHub para el CI/CD. Se ven los planes elegidos, el service mesh mínimo de Container
Apps y los riesgos que se aceptan por costo.

> «No hay red privada porque no cabe en el crédito; cada capa revisa el token y lo que no cubre queda escrito.»
> (ADR-013)

> «Un pull request nunca despliega: solo `main`, después de Sonar y Trivy y con mi aprobación.» (ADR-014)

> «Con un solo servicio propio, la malla es la que trae la plataforma.» (ADR-008)

### 3.4 · Bloques de arquitectura y de solución (ABB → SBB)
**Estado:** actualizado con el despliegue (11-nov-2026). La columna «Implementación» dice si el bloque está
desplegado, desplegado con límites (y cuál) o solo en diseño.

El ABB es la capacidad que el sistema necesita, sin marca; el SBB es el producto que la resuelve. Tipo: **P** propio
(código), **A** administrado de Azure (configuración), **E** externo.

> «El concepto se queda; el producto puede cambiar. Cambiar el producto es una ADR nueva, no otra arquitectura.»

| # | ABB | SBB | Tipo | ADR | Implementación |
|---|---|---|---|---|---|
| 1 | Front web | SPA Vue 3 + TypeScript en Blob `$web` | P | 002 | Desplegado |
| 2 | Servicio de Kudos | Java 27 + Spring Boot 4.1 en Container Apps | P | 007, 013 | Desplegado |
| 3 | Publicación fiable de eventos (outbox) | Outbox en PostgreSQL + relevo `@Scheduled` | P | 010 | Desplegado |
| 4 | Identity Provider | Microsoft Entra ID (tenant propio, app roles) | E | 013, 020 | Desplegado |
| 5 | WAF | Azure Front Door Standard + política WAF | A | 006, 013 | Solo diseño (riesgo R-04) |
| 6 | CDN | Azure Front Door | A | 006 | Solo diseño (riesgo R-04) |
| 7 | API Gateway | Azure API Management Consumption | A | 006, 013 | Desplegado |
| 8 | Message Broker | Azure Event Grid + dead-letter en Blob | A | 007, 021 | Desplegado |
| 9 | Notification Gateway | Azure Logic Apps Consumption | A | 007, 009, 021 | Desplegado |
| 10 | Servicio de correo | Azure Communication Services Email | A | 006 | Desplegado |
| 11 | Mensajería WhatsApp | Twilio WhatsApp API (cuenta de prueba, plantilla) | E | 006, 021 | Con límites: cuenta de prueba de Twilio (R-08) |
| 12 | Real-time Push | Azure Web PubSub Free | A | 016, 022 | Desplegado |
| 13 | Base de datos | Azure Database for PostgreSQL Flexible 18 | A | 010, 013 | Desplegado |
| 14 | Caché | Azure Managed Redis (clave en Key Vault) | A | 011, 017, 018 | Desplegado |
| 15 | Almacén de adjuntos | Azure Blob Storage + SAS de delegación | A | 013 | Desplegado |
| 16 | Registro de auditoría | Azure Table Storage | A | 012, 021 | Con límites: solo inserción, no WORM (R-11) |
| 17 | Data Mesh | Azure Data Factory + Blob con catálogo | A | 015 | Solo diseño |
| 18 | Service Mesh | Ingress Envoy de Container Apps (sin mTLS en Express) | A | 008, 018 | Con límites: sin mTLS en Express |
| 19 | Gestor de secretos | Azure Key Vault | A | 003, 013, 018 | Con límites: tres secretos copiados en la app (R-02) |
| 20 | Parameter Catalog | Variables de entorno de Container Apps (App Configuration a futuro) | A | 003, 019 | Desplegado |
| 21 | Observabilidad | Log Analytics (Application Insights a futuro) | A | 018 | Con límites: sin trazas ni correlationId en el log (R-13) |
| 22 | Pipeline CI/CD | GitHub Actions + OIDC | E | 014 | Desplegado |
| 23 | Análisis estático | SonarQube Cloud | E | 014 | Desplegado |
| 24 | Escáner de vulnerabilidades | Trivy | E | 014 | Desplegado |
| 25 | Registro de imágenes | GitHub Container Registry | E | 014 | Desplegado |
| 26 | Ejecución de contenedores | Azure Container Apps (modo Express) | A | 008, 014, 018 | Desplegado |

### 3.5 · Atributos de calidad
**Estado:** aceptado (9-nov-2026).

Los atributos salen de los drivers del enunciado (DRV-001 a DRV-007) y se escriben como escenarios de seis partes:
fuente, estímulo, entorno, artefacto, respuesta y medida de la respuesta, con los nombres de *Software Architecture in
Practice* (Bass, Clements y Kazman, 3.ª ed.). Cada escenario nombra componentes del diagrama, no productos, y su medida
se puede probar. El detalle completo está en el diseño de alto nivel.

> «Si no se puede medir, no es un atributo de calidad, es un deseo.»

> «El costo no es un atributo: es una restricción que manda sobre todo lo demás.»

| # | Atributo | Escenario | Medida de la respuesta |
|---|---|---|---|
| QA-1 | Rendimiento | 100 miembros consultan el feed en carga pico | p95 < 300 ms; aciertos de la Caché ≥ 80 % |
| QA-2 | Rendimiento | Un miembro publica un Kudo, incluso con un canal caído | p95 < 500 ms |
| QA-3 | Disponibilidad | Mensajería WhatsApp no responde durante una publicación | 0 Kudos perdidos; 0 avisos duplicados; reintento < 1 min |
| QA-4 | Seguridad | Un rol sin permiso intenta administrar categorías, vetar o tocar datos ajenos | 100 % de intentos resistidos en la matriz endpoint × 7 roles |
| QA-5 | Modificabilidad | Se pide un canal nuevo o cambiar un proveedor | 0 líneas cambiadas en el Servicio de Kudos |
| QA-6 | Usabilidad (movilidad) | Un miembro abre la app desde un celular con 4G | LCP < 2,5 s; sin scroll horizontal a 360 px |
| QA-7 | Seguridad (no repudio) | Se publica un Kudo | 100 % de los pasos en Auditoría; nadie puede borrar el registro |
| QA-9 | Rendimiento | Llega un Kudo para un destinatario conectado | Aviso en tiempo real p95 < 10 s |
| QA-10 | Seguridad | Se sube un archivo falso o se pide un adjunto ajeno | 100 % rechazados (4xx / 403) |
| QA-11 | Desplegabilidad | Se integra un cambio a `main` | Despliegue < 15 min; vuelta atrás < 5 min; vulnerabilidades HIGH bloquean |
| QA-12 | Seguridad | Un Kudo trae inyección SQL o `<script>` | 100 % de cargas sin ejecutarse |
| QA-13 | Escalabilidad | Pico de 10× la carga de lectura durante 5 min | 1 → 3 réplicas en < 2 min; 0 errores 5xx |
| QA-14 | Disponibilidad | Se borra un adjunto o se daña la Base de datos | RPO ≤ 5 min; RTO medido, meta < 2 h |
| QA-15 | Monitoreabilidad | Falla un canal o se acumulan eventos fallidos | Alerta < 5 min; traza completa por `correlationId` |
| QA-16 | Facilidad de prueba | Se termina un caso de uso del núcleo | Cobertura ≥ 80 % del dominio; mutación reportada |
| QA-17 | Interoperabilidad | Se publica el evento `KudoPublicado` con esquema versionado | 100 % de pruebas de contrato en verde |

### 3.6 · Diagrama de componentes

![C4 nivel 3 · Componentes del Servicio de Kudos](docs/Dise%C3%B1o/Alto%20Nivel/Componentes.jpg)
Es el zoom al Servicio de Kudos. Cada caja es un componente por funcionalidad: agrupa todo lo de su función detrás de una interfaz y por dentro es hexagonal (dominio, casos de uso y adaptadores). Para la línea base son seis: Usuarios y acceso, Reconocimientos, Adjuntos, Categorías, Eventos de integración y Auditoría. Moderación y Calificaciones llegan después.

>«Un componente es un grupo de funcionalidad relacionada detrás de una interfaz bien definida; no una clase.» (Simon Brown, The C4 model; Clean Architecture, cap. 34)

>«Reconocimientos es el centro: todo lo demás le presta un servicio.»


### 3.7 · Diseño detallado
Fuentes en [`docs/Diseño/Detallado/Diagramas/`](docs/Dise%C3%B1o/Detallado/Diagramas/).


#### Paquetes
![Diagrama de paquetes de kudos-service](docs/Diseño/Detallado/paquetes.jpg)

El código del Servicio de Kudos se organiza con el empaquetado del profesor, el mismo de la práctica `ucotestapi`, en
tres capas. Abajo está **infraestructura**, con los controladores REST y los adaptadores a PostgreSQL, Azure y Event
Grid. En medio están los **servicios**, con los casos de uso y sus puertos de entrada y salida. Arriba está el
**dominio**, Java puro. Cada flecha es una dependencia y todas apuntan hacia el dominio: cada paquete de
infraestructura implementa el puerto que tiene justo encima.

#### Modelo de dominio

![Modelo de dominio](docs/Dise%C3%B1o/Detallado/Diagramas/modelo-dominio.jpg)

Las clases del dominio agrupadas por componente. Hay tres agregados: Kudo (con sus adjuntos), Usuario y Categoría. Entre ellos solo se conocen por id, así cada componente cambia sin arrastrar a los demás. Cuando se publica un Kudo nace el evento KudoPublicado, que lleva los datos del destinatario para avisarle. Todo es Java puro, sin Spring.

>«Kudo no conoce a Usuario, conoce su id: cada agregado cuida su propia consistencia.»

>«Usuario se crea con los datos del token la primera vez que entra; así, cuando alguien le manda un Kudo, ya sabemos a dónde avisarle.»

#### Entidad-relación · Base de datos

![Entidad-relación · Base de datos](docs/Dise%C3%B1o/Detallado/Diagramas/ER-Kudos.jpg)

Cinco tablas en la Base de datos: usuario, categoria, kudo, adjunto y outbox_evento. El outbox guarda el evento en la misma transacción que el Kudo y no tiene llaves foráneas, para que el relevo lo lea sin tocar lo demás. La auditoría no vive aquí, va al Registro de auditoría. Las tablas e índices se crean con Flyway.

>«El feed se pagina por fecha e id; el índice va en ese mismo orden para no saltar ni repetir Kudos.»

#### Secuencia · Publicar Kudo

![Secuencia · Publicar Kudo](docs/Dise%C3%B1o/Detallado/Flujo.jpg)
Se lee en cuatro tramos: la petición entra y se valida; el Kudo y su evento se guardan en una sola transacción; se audita, se limpia la caché del feed y se responde 201; y por detrás el relevo publica el evento y el Notification Gateway avisa por correo, WhatsApp y push al mismo tiempo.

>«El 201 sale antes de las notificaciones: publicar no espera a nadie.» (ADR-007)

>«El Kudo y su evento viajan juntos o no viaja ninguno.» (ADR-010)

---

## 4 · Decisiones de arquitectura (ADR)
Formato Nygard (contexto, decisión, consecuencias). Una ADR aceptada no se edita: un cambio es una ADR nueva y la
anterior queda reemplazada o precisada. Todas las decide Juan Pablo Avendaño Duque.

| ADR | Decisión | Estado |
|---|---|---|
| 001 | Un back de dominio + servicios administrados integrados por eventos | Reemplazada por 007 |
| 002 | Front web funcional, sin PWA ni almacenamiento en el cliente | Aceptada (push en 016) |
| 003 | Nada fijo en el código: catálogo de parámetros y Key Vault | Aceptada (implementación en 019) |
| 004, 005 | Data mesh: no adoptar / mínimo por servicio | Reemplazadas (005 por 015) |
| 006 | Proveedores: WAF en Front Door, correo en ACS Email, WhatsApp en Twilio | Aceptada |
| 007 | Broker de eventos: Event Grid empuja `KudoPublicado` a la Logic App | Aceptada |
| 008 | Service mesh mínimo: ingress Envoy y mTLS del entorno | Aceptada (mTLS precisado por 018) |
| 009 | Consumidor idempotente en el Notification Gateway | Aceptada (clave en 021) |
| 010 | Relevo del outbox hacia Event Grid | Aceptada |
| 011 | Invalidación de la caché del feed al publicar | Aceptada |
| 012 | Registro de auditoría de solo inserción | Aceptada (alcance en 021) |
| 013 | Exposición y red: defensa en capas sin VNet | Aceptada (precisada por 018 y 020) |
| 014 | CI/CD y cadena de suministro (OIDC, Sonar, Trivy, aprobación manual) | Aceptada |
| 015 | Data mesh con Data Factory y un solo productor | Aceptada, solo diseño |
| 016 | Push en tiempo real con Azure Web PubSub | Aceptada (endpoint en 022) |
| 017 | Caché del feed en Azure Managed Redis | Aceptada (acceso con clave, 018) |
| 018 | Container Apps en modo Express: el portal solo ofrece Express; sin referencias a Key Vault, sin mTLS ni agente de trazas, escala a cero. Los tres secretos se copian a la app y Key Vault sigue siendo la fuente | Aceptada · 11-nov |
| 019 | El catálogo de parámetros son las variables de entorno de la Container App (factor III) más Key Vault; App Configuration queda para cambios sin reinicio | Aceptada · 11-nov |
| 020 | Identidad en un tenant workforce propio (`ucokudos`) que simula el de la UCO: `kudos-api` con 7 app roles, scope `Kudos.Usar`, tokens v2 y claims `email` y `phone_number` (este por mapeo, con `acceptMappedClaims`); `kudos-front` como SPA con PKCE | Aceptada · 11-nov |
| 021 | Notification Gateway: idempotencia por evento + canal en la tabla `procesados`; auditoría de solo inserción sin garantía WORM; disparador sin *Split on* (CloudEvents llega como objeto); WhatsApp con plantilla de Twilio por la cuenta de prueba | Aceptada · 11-nov |
| 022 | `GET /api/usuarios` (id y nombre de quienes ya entraron) y `GET /api/push/conexion` (URL de Web PubSub con el id del usuario) | Aceptada · 11-nov |

## 5 · Riesgos y trade-offs
> «Cada ahorro de hoy tiene un precio conocido y una salida escrita.»

| Trade-off | Se gana | Se paga |
|---|---|---|
| Un servicio propio y el resto administrado (007) | Poco código y nada que operar | Dependencia de Azure |
| Broker entre el back y los canales (007) | Publicar no espera a los canales | Entrega al menos una vez: hace falta idempotencia (009, 021) |
| Container Apps Express (018) | Montaje en minutos por el portal | Secretos copiados, sin mTLS ni trazas, arranque en frío |
| Planes gratuitos o mínimos (presupuesto de USD 10/mes) | Costo controlado | Límites de Web PubSub (20 conexiones) y de Twilio (cuenta de prueba) |

| ID | Riesgo | Disparador | Mitigación hoy | En producción |
|---|---|---|---|---|
| R-01 | Dependencia de Azure | Cambio de precio o retiro de un servicio | ABB separado del SBB; el dominio no conoce Azure (ArchUnit) | Cambiar el adaptador del puerto con una ADR |
| R-02 | Secretos copiados fuera de Key Vault | Rotar y olvidar la app | Key Vault es la fuente; son solo tres | Entorno con referencias a Key Vault |
| R-03 | Escala a cero (503 y relevo detenido) | Minutos sin tráfico | Min replicas = 1 en la demo | Mínimo de una réplica o relevo como job |
| R-04 | Sin WAF ni CDN (Front Door) | Tráfico malicioso | APIM valida el token y el CORS; el back revisa token y cabecera | Front Door con política WAF |
| R-05 | PostgreSQL y Redis con endpoint público | Credencial filtrada | Firewall a servicios de Azure; TLS; claves en Key Vault | Red privada y private endpoints |
| R-06 | Correo y teléfono viajan en el evento | Lectura del dead-letter | Contenedor privado y TTL de un día | Evento con ids y consulta en la Logic App |
| R-07 | `acceptMappedClaims` en la API | Token con claims mapeados de otra app | Un solo tenant; claim no restringido | Llave de firma propia o *claims provider* |
| R-08 | WhatsApp en cuenta de prueba | Sesión vencida (3 días) | `join twilio-trial` antes de la demo | Cuenta pagada y plantilla propia (cero código) |
| R-09 | CVE-2026-109518 aceptada (`apt` de la imagen base) | Ubuntu sin arreglo publicado | `.trivyignore` con revisión el 20-nov; la app no usa apt ni corre como root | Imagen *distroless* |
| R-10 | Una sola región, sin zonas | Caída regional | Infraestructura documentada y recreable | Zonas o región secundaria |
| R-11 | Auditoría borrable por el rol de datos | Error de permisos | La Logic App solo inserta | Almacenamiento inmutable |
| R-12 | Redis caído vuelve lento el feed | Corte de Redis | El feed sale de PostgreSQL | Timeout corto y *circuit breaker* |
| R-13 | El log no imprime el correlationId | Investigar un fallo | El id viaja en cabeceras, evento y auditoría | Patrón de log con el id y Application Insights |
| R-14 | Costo fijo de Managed Redis | Pasar el presupuesto | Alertas; borrar Redis después de la entrega | Caché por réplica si la carga lo permite |

## 6 · Seguridad
> «Una sola puerta de entrada, y aun así kudos-service revisa el token.» (ADR-013)

Defensa en capas: Entra ID (login con PKCE) → API Management (`validate-jwt`, CORS solo para el front, cabecera secreta
`X-Kudos-Gateway`) → kudos-service (valida el token otra vez, exige la cabecera y lee el rol) → datos con identidad
administrada.

| OWASP 2021 | Control | Dónde |
|---|---|---|
| A01 Control de acceso | Token en dos capas; sin rol, 403; *assignment required*; el autor sale del token; SAS de solo lectura por 10 min | `ConfiguracionDeSeguridad`, `IdentidadDesdeToken` |
| A02 Criptografía | TLS en todo; secretos en Key Vault; comparación del secreto en tiempo constante | `FiltroCabeceraDelGateway` |
| A03 Inyección | Consultas parametrizadas (JPA); Vue escapa el texto; tipo de adjunto por *magic bytes* | `TipoDeArchivo.detectar` |
| A04 Diseño inseguro | Reglas del dominio, outbox e idempotencia | `PreparacionDelKudo` |
| A05 Configuración | Nada fijo en el código; errores técnicos sin detalle; solo `/actuator/health` expuesto | `ManejadorDeErrores` |
| A06 Componentes vulnerables | Trivy bloquea HIGH y CRITICAL; CVE de Jackson y commons-codec corregidas en el pom | `backend-ci.yml` |
| A07 Autenticación | Entra ID con MSAL; issuer y audiencia validados. Falta forzar MFA (plan gratuito) | `validate-jwt` |
| A08 Integridad | Acciones fijadas por SHA, despliegue con OIDC, imagen con el SHA del commit, aprobación manual | `backend-ci.yml` |
| A09 Registro y monitoreo | Auditoría por canal; logs en Log Analytics. Falta el correlationId en el log y alertas (R-13) | `RelevoOutbox` |
| A10 SSRF | El back nunca descarga URLs del usuario | — |

STRIDE del flujo Publicar Kudo: suplantación (token validado en dos capas y cabecera del gateway), manipulación (autor
desde el token, adjunto por *magic bytes*), repudio (auditoría con autor y correlationId), divulgación (contenedor
privado y SAS temporal), denegación de servicio (broker y dead-letter; falta WAF), elevación (solo los 7 app roles que
asigna el administrador). Zero Trust parcial: verificación explícita y mínimo privilegio sí; red privada y MFA no.

## 7 · Cloud native y observabilidad
- **Twelve-Factor:** configuración por variables (III, ADR-019), servicios de respaldo por URL (IV), construir →
  liberar → ejecutar con la imagen del commit (V), procesos sin estado: sesión en el token, caché en Redis y archivos en
  Blob (VI), concurrencia por réplicas con `for update skip locked` en el relevo (VIII), logs a la salida estándar (XI),
  migraciones con Flyway (XII). Pendientes: arranque en frío (IX) y token local distinto de Entra en desarrollo (X).
- **Sistema reactivo:** responsivo (publicar no espera canales), resiliente (outbox, dead-letter, idempotencia por
  canal, Redis caído → PostgreSQL), elástico (réplicas y servicios serverless), orientado a mensajes (CloudEvents 1.0).
- **Well-Architected:** confiabilidad con reintentos y dead-letter (falta segunda región); costo con planes mínimos y
  presupuesto; excelencia operativa con CI/CD aprobado (sin infraestructura como código, por regla); rendimiento con
  caché y paginación por cursor; seguridad en §6.
- **Cloud Adoption Framework:** suscripción y presupuesto preparados, grupo `rg-kudos`, convención de nombres
  (`ca-`, `psql-`, `kv-`, `apim-`), roles RBAC acotados y ADR como gobierno.
- **Observabilidad:** sondas `/actuator/health/liveness` y `/readiness`; logs en Log Analytics; métricas de Event Grid
  (publicados, entregados, dead-letter) y de la Logic App; correlationId en cabecera, evento y auditoría.

## 8 · CI/CD y pruebas
> «Un pull request nunca despliega: solo `master`, después de Sonar y Trivy y con mi aprobación.» (ADR-014)

- **Back** (`.github/workflows/backend-ci.yml`): compilación y 74 pruebas → SonarQube Cloud (quality gate) → imagen
  Docker → Trivy → GHCR → aprobación en el entorno `production` → `az containerapp update` con OIDC.
- **Front** (`frontend-ci.yml` del repositorio del front): 8 pruebas con Vitest, revisión de tipos y build → aprobación
  → `$web` con OIDC.
- **Pirámide:** unitarias de dominio y casos de uso con dobles (`dobles/Dobles.java`), adaptadores de Azure con Mockito,
  integración con PostgreSQL 18 y Redis 8 reales (Testcontainers, `PublicarKudoIntegracionTest`), reglas de
  arquitectura con ArchUnit (`ArquitecturaTest`), mutación con PIT y sesiones adversarias.

## 9 · Despliegue
- Aplicación: https://storagekudouco.z15.web.core.windows.net/ · API: https://apim-kudos-uco.azure-api.net/api
- Recursos en `rg-kudos`: Container App `ca-kudos-service` (Express), PostgreSQL `psql-kudos-uco`, Redis
  `redis-kudos-uco-v2`, API Management `apim-kudos-uco`, Event Grid `kudos-eventos`, Logic App
  `logic-kudos-notificaciones`, Web PubSub `wps-kudos-uco`, Communication Services `azcs-kudos-uco`, Storage
  `storagekudouco`, Key Vault `kv-kudos-uco`, Log Analytics `log-kudos`.
- Configuración: 34 variables `KUDOS_*` (`application.yaml`) y `AZURE_CLIENT_ID`; tres secretos copiados desde Key
  Vault (ADR-018). Una variable nueva se guarda en la app antes del push, o la revisión no arranca.
- El funcionamiento de punta a punta, con capturas, está en `docs/evidencia/README.md`.

## 10 · Uso responsable de IA
La IA propone y Juan decide. El agente trabajó con reglas fijas: nunca escribe en los repositorios ni hace commit
(trabaja en una copia y Juan sincroniza), nunca envía mensajes reales, la infraestructura la monta Juan por el portal y
cada decisión queda como ADR firmada por él. Errores de la IA detectados durante el despliegue: subject OIDC sin los ids
inmutables que ya envía GitHub (falló el primer CD), afirmar sin confirmar que Express aceptaba la identidad, atribuir el
error 21654 de Twilio a la ventana de 24 h (era la cuenta de prueba) e información vieja de la consola de Twilio. La
lección: lo que la IA dice de un servicio en la nube se verifica contra la pantalla o la documentación del día.
