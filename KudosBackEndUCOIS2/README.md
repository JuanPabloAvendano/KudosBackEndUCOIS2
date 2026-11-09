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
