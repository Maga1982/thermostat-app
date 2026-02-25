import { db } from "./db";
import {
  thermostats,
  type Thermostat,
  type InsertThermostat,
  type UpdateThermostatRequest,
  type ThermostatListResponse,
  type ThermostatResponse
} from "@shared/schema";
import { eq } from "drizzle-orm";

export interface IStorage {
  getThermostats(): Promise<ThermostatListResponse>;
  getThermostat(id: number): Promise<ThermostatResponse | undefined>;
  createThermostat(thermostat: InsertThermostat): Promise<ThermostatResponse>;
  updateThermostat(id: number, updates: UpdateThermostatRequest): Promise<ThermostatResponse>;
}

export class DatabaseStorage implements IStorage {
  async getThermostats(): Promise<ThermostatListResponse> {
    return await db.select().from(thermostats);
  }

  async getThermostat(id: number): Promise<ThermostatResponse | undefined> {
    const [thermostat] = await db.select().from(thermostats).where(eq(thermostats.id, id));
    return thermostat;
  }

  async createThermostat(thermostat: InsertThermostat): Promise<ThermostatResponse> {
    const [created] = await db.insert(thermostats).values(thermostat).returning();
    return created;
  }

  async updateThermostat(id: number, updates: UpdateThermostatRequest): Promise<ThermostatResponse> {
    const [updated] = await db.update(thermostats)
      .set(updates)
      .where(eq(thermostats.id, id))
      .returning();
    return updated;
  }
}

export const storage = new DatabaseStorage();
