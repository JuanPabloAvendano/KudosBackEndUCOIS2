# UCO Kudos · cómo funciona (evidencia del despliegue, 10 y 11-nov-2026)

Capturas de Juan durante el montaje en Azure y la prueba de punta a punta. Cada paso dice qué ocurre y qué captura lo
muestra.

## 1 · Publicar un Kudo, de punta a punta
1. **Entrar.** La persona abre https://storagekudouco.z15.web.core.windows.net/ y entra con su cuenta del tenant
   `ucokudos` (Microsoft Entra ID). El token trae su id, nombre, correo, teléfono y rol institucional.
   `entra-tenant-ucokudos.png`, `entra-app-kudos-front.png`, `entra-kudos-front-permisos-consentidos.png`,
   `entra-usuarios-con-rol.png`, `entra-claim-email-opcional.png`, `entra-kudos-api-token-v2.png`,
   `entra-kudos-api-scope.png`, `entra-app-kudos-api.png`, `addedRolesToIdentityProvider.png`.
2. **Llamar a la API.** Toda llamada pasa por API Management, que valida el token, permite solo el origen del front y
   agrega la cabecera secreta del gateway; sin token responde 401. `apim-gateway-consumption.png`.
3. **Publicar.** kudos-service valida de nuevo el token, guarda el Kudo, su adjunto (Blob) y el evento en una sola
   transacción, limpia la caché del feed (Redis) y responde 201. El Kudo aparece en el feed de ambas personas, con el
   adjunto por un enlace temporal. `e2e-kudo-con-adjunto-dos-usuarios.png`.
4. **Notificar.** El relevo publica `KudoPublicado` (CloudEvents 1.0) en Event Grid; la suscripción lo entrega a la
   Logic App; si la entrega falla, el evento va al contenedor `eventos-fallidos`. `eventgrid.png`,
   `eventgrid-suscripcion.png`, `eventgrid-suscripcion-logicapp.png`, `eventgrid-deadletter.png`,
   `eventgrid-metricas-dead-letter.png`.
5. **Enviar por tres canales.** La Logic App reserva cada canal en la tabla `procesados` (evento + canal, para no
   repetir), envía y registra el resultado en la tabla `auditoria`. `logicapp-flujo.png`, `logicapp-leer-evento.png`,
   `logicapp-roles.png`, `logicapp-ejecuciones-whatsapp-ok.png`.
   - Correo con Azure Communication Services: `acs-dominio.png`, `acs-correo-prueba.jpg`.
   - Aviso en tiempo real con Web PubSub (el destinatario ve el aviso sin recargar): `webpubsubazure.png`,
     `webpubsub-roles.png`.
   - WhatsApp con Twilio (cuenta de prueba: plantilla de Twilio y unión con `join twilio-trial`):
     `twilio-sandbox-join.jpg`, `twilio-unirse-sandbox.png`.
   - WAF con ModSecurity + OWASP CRS delante de APIM (ADR-023): `waf-pruebas.txt` (21 pruebas sin token: SQLi, XSS,
     traversal, comandos, Log4Shell y escáner bloqueados con 403; JSON, adjunto y CORS legítimos llegan a APIM).
   - Observabilidad: tablero de Grafana en Azure Monitor (Monitor → Dashboards with Grafana → Import)
     `grafana-kudos.json` (importado: `grafana-tablero.png`): resumen del flujo, WAF y APIM, kudos-service, Event Grid, Logic App, Web PubSub,
     PostgreSQL, Redis y Blob.

## 2 · Plataforma
- **Servicio:** Container App en modo Express con identidad administrada, secretos y parámetros por variables.
  `container-app-identidad-asignada.png`, `container-app-secretos.png`, `container-app-variables-jwt.png`,
  `express-sin-referencias-keyvault.png`.
- **Datos:** PostgreSQL 18 con firewall para servicios de Azure; Redis solo TLS; secretos en Key Vault.
  `postgresql-instance.png`, `postgresql-red-acceso-publico.png`, `postgresql-firewall-servicios-azure.png`,
  `redis.png`, `redis-ajustes-avanzados.png`, `redis-acceso-publico.png`, `keyvault-secretos.png`.
- **Despliegue sin llaves:** GitHub Actions con credencial federada (OIDC) y rol limitado a la app.
  `oidc-credencial-federada-escenarios.png`, `iam-contributor-id-kudos-github.png`.
- **Costo:** presupuesto de USD 10/mes con alertas. `budget.png`.

## 3 · Fallos encontrados al desplegar y cómo se corrigieron
| Captura | Fallo | Causa | Corrección |
|---|---|---|---|
| `incidente-variables-secreto-como-manual.png` | PostgreSQL rechazaba la clave | Variables de secreto con origen *Manual* | Origen *Secret* |
| `incidente-claim-email-faltante.png` | 400 «El correo es obligatorio» | Usuarios sin correo de contacto | Correo en cada usuario de Entra |
| `incidente-arranque-en-frio-503.png` | 503 en el primer acceso | La app escala a cero | Una réplica mínima durante la demo |
| `incidente-logicapp-leer-evento.png` | Canales omitidos | Esquema que no admitía teléfono vacío | Campos que admiten `null` |
| `incidente-logicapp-expresion-sin-arroba.png` | InvalidJSON | Expresión escrita como texto | `@triggerBody()?['data']` |
| `incidente-logicapp-reservar-get.png` | WhatsApp rechazado | Acción copiada con método y URL equivocados | POST a la tabla `procesados` |

## 4 · Cómo repetir la prueba
1. Container App → *Scale* → una réplica mínima (evita el arranque en frío).
2. Cada teléfono que va a recibir WhatsApp envía `join twilio-trial` a +1 737 250 8034 (la unión vence a los 3 días).
3. Entrar como `docente1@ucokudos.onmicrosoft.com` y, en una ventana privada, como `estudiante1@ucokudos.onmicrosoft.com`.
4. Docente publica un Kudo para Estudiante 1. Debe verse en ambos feeds, aparecer el aviso en la ventana de Estudiante
   y llegar el correo y el WhatsApp.

## 5 · Evidencias que pide el enunciado
| Evidencia | Dónde |
|---|---|
| CI | `cicd-sonar-trivy.txt` (runs verdes de `backend-ci` y `frontend-ci`, con enlace) |
| CD | `cicd-sonar-trivy.txt` (job `cd` con aprobación y OIDC), `oidc-credencial-federada-escenarios.png`, `iam-contributor-id-kudos-github.png` |
| Sonar | `cicd-sonar-trivy.txt` (quality gate PASSED, cobertura 95,4 %, 0 bugs, 0 vulnerabilidades) |
| Trivy | `cicd-sonar-trivy.txt` (0 vulnerabilidades HIGH/CRITICAL en imagen, jar y binarios) |
| Publicación | `cicd-sonar-trivy.txt` (comprobación en vivo: front 200, 401 sin token, 403 sin gateway) |
| Despliegue | §2 de este archivo (Container App, datos, Key Vault, OIDC, presupuesto) |
| Correo | `acs-dominio.png`, `acs-correo-prueba.jpg`, `logicapp-flujo.png` |
| WhatsApp | `logicapp-ejecuciones-whatsapp-ok.png`, `twilio-sandbox-join.jpg`, `twilio-unirse-sandbox.png` |
| Push | `webpubsubazure.png`, `webpubsub-roles.png`, `e2e-kudo-con-adjunto-dos-usuarios.png` |
| Auditoría | `logicapp-flujo.png` (acción Auditar en verde, tabla `auditoria`) |
| Observabilidad | `grafana-kudos.json`, `grafana-tablero.png`, `eventgrid-metricas-dead-letter.png` |
| Pruebas | `cicd-sonar-trivy.txt` (74 del back, 8 del front), `waf-pruebas.txt` (21 del WAF) |
