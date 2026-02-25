import { getDb } from "./firebase";
import type { Thermostat, InsertThermostat, UpdateThermostatRequest } from "@shared/schema";

const COLLECTION = "thermostats";

function docToThermostat(id: string, data: FirebaseFirestore.DocumentData): Thermostat {
  return {
    id: parseInt(id),
    name: data.name,
    currentTemp: data.currentTemp,
    targetTemp: data.targetTemp,
    systemMode: data.systemMode,
    fanMode: data.fanMode,
    currentHumidity: data.currentHumidity,
    lastUpdated: data.lastUpdated?.toDate() ?? new Date(),
  };
}

export interface IStorage {
  getThermostats(): Promise<Thermostat[]>;
  getThermostat(id: number): Promise<Thermostat | undefined>;
  createThermostat(thermostat: InsertThermostat): Promise<Thermostat>;
  updateThermostat(id: number, updates: UpdateThermostatRequest): Promise<Thermostat>;
}

export class FirebaseStorage implements IStorage {
  async getThermostats(): Promise<Thermostat[]> {
    const snapshot = await getDb().collection(COLLECTION).get();
    return snapshot.docs.map((doc) => docToThermostat(doc.id, doc.data()));
  }

  async getThermostat(id: number): Promise<Thermostat | undefined> {
    const doc = await getDb().collection(COLLECTION).doc(String(id)).get();
    if (!doc.exists) return undefined;
    return docToThermostat(doc.id, doc.data()!);
  }

  async createThermostat(thermostat: InsertThermostat): Promise<Thermostat> {
    const db = getDb();
    const snapshot = await db.collection(COLLECTION).get();
    const newId = snapshot.size + 1;
    const data = {
      ...thermostat,
      lastUpdated: new Date(),
    };
    await db.collection(COLLECTION).doc(String(newId)).set(data);
    return docToThermostat(String(newId), data);
  }

  async updateThermostat(id: number, updates: UpdateThermostatRequest): Promise<Thermostat> {
    const ref = getDb().collection(COLLECTION).doc(String(id));
    const updateData = { ...updates, lastUpdated: new Date() };
    await ref.update(updateData);
    const updated = await ref.get();
    return docToThermostat(updated.id, updated.data()!);
  }
}

export const storage = new FirebaseStorage();
