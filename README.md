# CRM Clientes IA

Versión del CRM original con integración de IA para:

- Predicción de próxima compra por cliente.
- Probabilidad cualitativa de recompra.
- Ticket estimado.
- Productos probables.
- Recomendación de contacto.
- Generación de prioridades para el día de ventas.

## 1. Compilar el APK en GitHub

1. Crea un repositorio nuevo en GitHub.
2. Sube todos los archivos de este proyecto.
3. Ve a **Actions**.
4. Abre **Compilar APK**.
5. Pulsa **Run workflow**.
6. Al terminar, abre la ejecución y descarga el artefacto `CRM-Clientes-IA-debug`.
7. Dentro estará `app-debug.apk`.

## 2. Activar la IA

La APK NO contiene una API key de OpenAI.

El backend de IA está en `api/ai.js` y está pensado para desplegarse en Vercel.

En Vercel crea estas variables:

- `OPENAI_API_KEY` = tu clave privada de OpenAI.
- `OPENAI_MODEL` = `gpt-6-astra` (o un modelo habilitado en tu cuenta).

Despliega el proyecto y copia la URL final, por ejemplo:

`https://tu-proyecto.vercel.app/api/ai`

Abre la pestaña **🤖 IA** dentro de la APK, pega esa URL, pulsa **Guardar conexión** y después **Probar IA**.

## Seguridad

Nunca pongas `OPENAI_API_KEY` dentro de `index.html`, `MainActivity.java`, GitHub público ni dentro de la APK. La clave vive solamente como variable de entorno del backend.
