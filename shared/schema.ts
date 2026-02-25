import { z } from "zod";

export const insertThermostatSchema = z.object({
  name: z.string(),
  currentTemp: z.number(),
  targetTemp: z.number(),
  systemMode: z.string(),
  fanMode: z.string(),
  currentHumidity: z.number(),
});

export type InsertThermostat = z.infer<typeof insertThermostatSchema>;

export type Thermostat = InsertThermostat & {
  id: number;
  lastUpdated: Date | null;
};

export type UpdateThermostatRequest = Partial<InsertThermostat>;
export type ThermostatResponse = Thermostat;
export type ThermostatListResponse = Thermostat[];
