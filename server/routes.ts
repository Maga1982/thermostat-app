import type { Express } from "express";
import type { Server } from "http";
import { storage } from "./storage";
import { api } from "@shared/routes";
import { z } from "zod";

export async function registerRoutes(
  httpServer: Server,
  app: Express
): Promise<Server> {
  
  app.get(api.thermostats.list.path, async (req, res) => {
    let list = await storage.getThermostats();
    if (list.length === 0) {
      // Seed database if empty
      await storage.createThermostat({
        name: "Living Room",
        currentTemp: 72,
        targetTemp: 70,
        systemMode: "cool",
        fanMode: "auto",
        currentHumidity: 45
      });
      list = await storage.getThermostats();
    }
    res.json(list);
  });

  app.get(api.thermostats.get.path, async (req, res) => {
    const thermostat = await storage.getThermostat(Number(req.params.id));
    if (!thermostat) {
      return res.status(404).json({ message: 'Thermostat not found' });
    }
    res.json(thermostat);
  });

  app.patch(api.thermostats.update.path, async (req, res) => {
    try {
      const input = api.thermostats.update.input.parse(req.body);
      const thermostat = await storage.updateThermostat(Number(req.params.id), input);
      res.json(thermostat);
    } catch (err) {
      if (err instanceof z.ZodError) {
        return res.status(400).json({
          message: err.errors[0].message,
          field: err.errors[0].path.join('.'),
        });
      }
      throw err;
    }
  });

  return httpServer;
}
