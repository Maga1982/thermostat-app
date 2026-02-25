import { motion } from "framer-motion";
import { Droplets, MapPin, MoreVertical } from "lucide-react";
import { useThermostat, useUpdateThermostat } from "@/hooks/use-thermostat";
import { ThermostatRing } from "@/components/ThermostatRing";
import { Controls } from "@/components/Controls";

export default function Dashboard() {
  const { data: thermostat, isLoading, isError } = useThermostat();
  const updateMutation = useUpdateThermostat();

  // Loading Skeleton
  if (isLoading) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center p-6">
        <div className="w-64 h-64 rounded-full border-4 border-white/5 animate-pulse mb-12"></div>
        <div className="w-full max-w-sm h-24 rounded-2xl bg-white/5 animate-pulse mb-6"></div>
        <div className="w-full max-w-sm h-16 rounded-2xl bg-white/5 animate-pulse"></div>
      </div>
    );
  }

  // Error State
  if (isError || !thermostat) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center p-6 text-center">
        <div className="w-16 h-16 rounded-full bg-destructive/20 flex items-center justify-center text-destructive mb-4">
          <MoreVertical size={32} />
        </div>
        <h2 className="text-2xl font-display font-bold text-white mb-2">Connection Lost</h2>
        <p className="text-muted-foreground">Unable to communicate with the thermostat. Please check your network connection.</p>
      </div>
    );
  }

  const handleTargetTempChange = (temp: number) => {
    updateMutation.mutate({ 
      id: thermostat.id, 
      updates: { targetTemp: temp } 
    });
  };

  const handleSystemModeChange = (mode: string) => {
    updateMutation.mutate({ 
      id: thermostat.id, 
      updates: { systemMode: mode } 
    });
  };

  const handleFanModeChange = (mode: string) => {
    updateMutation.mutate({ 
      id: thermostat.id, 
      updates: { fanMode: mode } 
    });
  };

  return (
    <div data-testid="dashboard" className="min-h-screen relative overflow-hidden bg-background">
      
      {/* Ambient Top Glow */}
      <div className="absolute top-0 inset-x-0 h-64 bg-gradient-to-b from-white/5 to-transparent pointer-events-none" />

      <div className="relative z-10 max-w-md mx-auto px-6 py-8 flex flex-col min-h-screen">
        
        {/* Header */}
        <header className="flex items-center justify-between mb-12">
          <motion.div 
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="flex items-center gap-2 text-white/80"
          >
            <div className="p-2 rounded-full glass-panel text-white">
              <MapPin size={18} />
            </div>
            <div>
              <h1 data-testid="text-thermostat-name" className="font-display font-medium leading-none">{thermostat.name}</h1>
              <span data-testid="status-online" className="text-xs text-white/40 flex items-center gap-1 mt-1">
                <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span> Online
              </span>
            </div>
          </motion.div>
          
          <motion.button 
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            className="w-10 h-10 rounded-full glass-button flex items-center justify-center text-white/70"
          >
            <MoreVertical size={20} />
          </motion.button>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 flex flex-col">
          
          {/* The Crown Jewel: Thermostat Ring */}
          <motion.div 
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.1, duration: 0.5 }}
            className="flex-1 flex flex-col justify-center"
          >
            <ThermostatRing
              currentTemp={thermostat.currentTemp}
              targetTemp={thermostat.targetTemp}
              mode={thermostat.systemMode}
              onTargetTempChange={handleTargetTempChange}
            />
          </motion.div>

          {/* Environmental Context (Humidity) */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="mt-8 mb-8 flex justify-center"
          >
            <div className="glass-panel px-6 py-3 rounded-full flex items-center gap-3">
              <Droplets size={18} className="text-blue-400" />
              <div className="flex flex-col">
                <span className="text-xs text-white/50 font-medium uppercase tracking-wider">Humidity</span>
                <span data-testid="text-humidity" className="text-sm font-semibold text-white">{thermostat.currentHumidity}%</span>
              </div>
            </div>
          </motion.div>

          {/* Controls Bottom Sheet */}
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="pb-6"
          >
            <Controls 
              systemMode={thermostat.systemMode}
              fanMode={thermostat.fanMode}
              onSystemModeChange={handleSystemModeChange}
              onFanModeChange={handleFanModeChange}
            />
          </motion.div>

        </main>
      </div>
    </div>
  );
}
