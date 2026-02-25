import { z } from 'zod';
import { insertThermostatSchema, thermostats } from './schema';

export const errorSchemas = {
  validation: z.object({
    message: z.string(),
    field: z.string().optional(),
  }),
  notFound: z.object({
    message: z.string(),
  }),
  internal: z.object({
    message: z.string(),
  }),
};

export const api = {
  thermostats: {
    list: {
      method: 'GET' as const,
      path: '/api/thermostats' as const,
      responses: {
        200: z.array(z.custom<typeof thermostats.$inferSelect>()),
      },
    },
    get: {
      method: 'GET' as const,
      path: '/api/thermostats/:id' as const,
      responses: {
        200: z.custom<typeof thermostats.$inferSelect>(),
        404: errorSchemas.notFound,
      },
    },
    update: {
      method: 'PATCH' as const,
      path: '/api/thermostats/:id' as const,
      input: insertThermostatSchema.partial(),
      responses: {
        200: z.custom<typeof thermostats.$inferSelect>(),
        400: errorSchemas.validation,
        404: errorSchemas.notFound,
      },
    },
  },
};

export function buildUrl(path: string, params?: Record<string, string | number>): string {
  let url = path;
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (url.includes(`:${key}`)) {
        url = url.replace(`:${key}`, String(value));
      }
    });
  }
  return url;
}

export type ThermostatUpdateInput = z.infer<typeof api.thermostats.update.input>;
export type ThermostatListResponse = z.infer<typeof api.thermostats.list.responses[200]>;
