import OpenAI from "openai";

const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });
const MODEL = process.env.OPENAI_MODEL || "gpt-6-astra";

const headers = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Content-Type": "application/json"
};

const json = (body, status = 200) => new Response(JSON.stringify(body), { status, headers });

function clientSchema() {
  return {
    type: "object",
    properties: {
      proximaCompra: { type: "string", description: "Fecha estimada YYYY-MM-DD o 'No estimable'" },
      probabilidad: { type: "string", enum: ["alta", "media", "baja", "no estimable"] },
      ticketEstimado: { type: "number" },
      productosProbables: { type: "array", items: { type: "string" } },
      contactarAhora: { type: "boolean" },
      motivo: { type: "string" }
    },
    required: ["proximaCompra", "probabilidad", "ticketEstimado", "productosProbables", "contactarAhora", "motivo"],
    additionalProperties: false
  };
}

function daySchema() {
  return {
    type: "object",
    properties: {
      resumen: { type: "string" },
      prioridades: {
        type: "array",
        items: {
          type: "object",
          properties: {
            id: { type: "integer" },
            nombre: { type: "string" },
            score: { type: "integer", minimum: 0, maximum: 100 },
            accion: { type: "string" },
            motivo: { type: "string" }
          },
          required: ["id", "nombre", "score", "accion", "motivo"],
          additionalProperties: false
        }
      }
    },
    required: ["resumen", "prioridades"],
    additionalProperties: false
  };
}

export default async function handler(req) {
  if (req.method === "OPTIONS") return new Response(null, { status: 204, headers });
  if (req.method !== "POST") return json({ error: "Método no permitido" }, 405);
  if (!process.env.OPENAI_API_KEY) return json({ error: "Falta OPENAI_API_KEY en el servidor." }, 500);

  try {
    const body = await req.json();
    if (body.action === "ping") return json({ ok: true, model: MODEL });

    if (body.action === "client") {
      const c = body.client;
      if (!c || !c.nombre) return json({ error: "Cliente inválido" }, 400);

      const response = await client.responses.create({
        model: MODEL,
        input: [
          {
            role: "system",
            content: "Eres un analista comercial para un vendedor mayorista en Chile. Analiza únicamente los datos entregados. Estima la próxima compra usando historial, intervalos y frecuencia declarada. No inventes productos ni compras. Si hay pocos datos, marca la probabilidad como no estimable o baja. La fecha actual debe tomarse del campo fechaAnalisis si existe. Devuelve una recomendación práctica y breve."
          },
          {
            role: "user",
            content: JSON.stringify({ fechaAnalisis: new Date().toISOString().slice(0, 10), cliente: c })
          }
        ],
        text: { format: { type: "json_schema", name: "client_prediction", strict: true, schema: clientSchema() } }
      });

      return json({ ok: true, result: JSON.parse(response.output_text) });
    }

    if (body.action === "day") {
      const clients = Array.isArray(body.clients) ? body.clients.slice(0, 80) : [];
      if (!clients.length) return json({ error: "No hay clientes con compras para analizar." }, 400);

      const response = await client.responses.create({
        model: MODEL,
        input: [
          {
            role: "system",
            content: "Eres un analista de ventas mayoristas. Ordena clientes por prioridad comercial para contactar hoy. Prioriza recompra atrasada, proximidad a recompra, ticket histórico alto, frecuencia consistente y oportunidades claras. No inventes datos. El score es una prioridad relativa de 0 a 100, no una probabilidad estadística. Devuelve como máximo 10 clientes y conserva sus IDs."
          },
          {
            role: "user",
            content: JSON.stringify({ fechaAnalisis: new Date().toISOString().slice(0, 10), clientes })
          }
        ],
        text: { format: { type: "json_schema", name: "sales_day", strict: true, schema: daySchema() } }
      });

      return json({ ok: true, result: JSON.parse(response.output_text) });
    }

    return json({ error: "Acción no reconocida" }, 400);
  } catch (error) {
    console.error(error);
    return json({ error: error?.message || "Error interno de IA" }, 500);
  }
}
