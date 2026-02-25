import { pgTable, text, serial, integer, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod";

// === TABLE DEFINITIONS ===
export const thermostats = pgTable("thermostats", {
  id: serial("id").primaryKey(),
  name: text("name").notNull(),
  currentTemp: integer("current_temp").notNull(),
  targetTemp: integer("target_temp").notNull(),
  systemMode: text("system_mode").notNull(), // 'heat', 'cool', 'off', 'auto'
  fanMode: text("fan_mode").notNull(), // 'auto', 'on'
  currentHumidity: integer("current_humidity").notNull(),
  lastUpdated: timestamp("last_updated").defaultNow(),
});

// === BASE SCHEMAS ===
export const insertThermostatSchema = createInsertSchema(thermostats).omit({ 
  id: true, 
  lastUpdated: true 
});

// === EXPLICIT API CONTRACT TYPES ===
export type Thermostat = typeof thermostats.$inferSelect;
export type InsertThermostat = z.infer<typeof insertThermostatSchema>;
export type UpdateThermostatRequest = Partial<InsertThermostat>;
export type ThermostatResponse = Thermostat;
export type ThermostatListResponse = Thermostat[];
