import type { Express } from "express";
import type { Server } from "http";
import { storage } from "./storage";
import { getDb } from "./firebase";
import { api } from "@shared/routes";
import { z } from "zod";

export async function registerRoutes(
  httpServer: Server,
  app: Express
): Promise<Server> {

  // GET all thermostats (seeds DB if empty)
  app.get(api.thermostats.list.path, async (req, res) => {
    let list = await storage.getThermostats();
    if (list.length === 0) {
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

  // GET single thermostat
  app.get(api.thermostats.get.path, async (req, res) => {
    const thermostat = await storage.getThermostat(Number(req.params.id));
    if (!thermostat) {
      return res.status(404).json({ message: 'Thermostat not found' });
    }
    res.json(thermostat);
  });

  // PATCH update thermostat (used by app and device to report changes)
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

  // ─────────────────────────────────────────────────────
  // POLLING ENDPOINT
  // The device calls this every N seconds with a timestamp.
  // The server checks if the thermostat was updated AFTER that timestamp.
  // If nothing changed → 304 (no data sent, saves bandwidth)
  // If something changed → 200 + full thermostat JSON
  //
  // Device usage example:
  //   GET /api/thermostats/1/poll?since=1708900000000
  // ─────────────────────────────────────────────────────
  app.get('/api/thermostats/:id/poll', async (req, res) => {
    const thermostat = await storage.getThermostat(Number(req.params.id));
    if (!thermostat) {
      return res.status(404).json({ message: 'Thermostat not found' });
    }

    const sinceParam = req.query.since as string;

    if (sinceParam) {
      const sinceMs = Number(sinceParam);
      const lastUpdatedMs = thermostat.lastUpdated
        ? new Date(thermostat.lastUpdated).getTime()
        : 0;

      // Nothing changed since the device last checked
      if (lastUpdatedMs <= sinceMs) {
        return res.status(304).end();
      }
    }

    // Something changed — send the latest data
    res.json(thermostat);
  });

  // ─────────────────────────────────────────────────────
  // SSE LISTENER ENDPOINT (Server-Sent Events)
  // The device opens ONE long-lived connection here.
  // The server pushes a message the INSTANT something changes in Firebase.
  // No repeated polling needed — Firebase calls us, we forward it.
  //
  // Device usage example:
  //   GET /api/thermostats/1/listen
  //   → connection stays open
  //   → device receives pushed events like:
  //       data: {"targetTemp":72,"systemMode":"heat"}
  //
  // Event types:
  //   "update"  – thermostat data changed (device should act on it)
  //   "ping"    – keep-alive every 30s so the connection doesn't drop
  // ─────────────────────────────────────────────────────
  app.get('/api/thermostats/:id/listen', (req, res) => {
    const id = req.params.id;

    // Set SSE headers — tells the client this is a streaming response
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.flushHeaders();

    // Send an initial confirmation that the connection is established
    res.write(`event: connected\ndata: {"message":"Listening for changes to thermostat ${id}"}\n\n`);

    // Attach a Firebase real-time listener to the Firestore document.
    // Firebase calls this callback EVERY TIME the document changes.
    const unsubscribe = getDb()
      .collection('thermostats')
      .doc(id)
      .onSnapshot((snapshot) => {
        if (!snapshot.exists) return;
        const data = snapshot.data();
        // Push the updated data to the device immediately
        res.write(`event: update\ndata: ${JSON.stringify(data)}\n\n`);
      });

    // Keep-alive ping every 30 seconds to prevent connection timeout
    const keepAlive = setInterval(() => {
      res.write(`event: ping\ndata: {"time":${Date.now()}}\n\n`);
    }, 30000);

    // Cleanup when the device disconnects
    req.on('close', () => {
      unsubscribe();           // Stop Firebase listener
      clearInterval(keepAlive); // Stop ping timer
      res.end();
    });
  });

  return httpServer;
}
