import { motion } from "framer-motion";
import { Flame, Snowflake, Leaf, Power, Fan, RefreshCw } from "lucide-react";
import { cn } from "@/lib/utils";

interface ControlsProps {
  systemMode: string;
  fanMode: string;
  onSystemModeChange: (mode: string) => void;
  onFanModeChange: (mode: string) => void;
}

export function Controls({ systemMode, fanMode, onSystemModeChange, onFanModeChange }: ControlsProps) {
  
  const systemModes = [
    { id: 'heat', icon: Flame, label: 'Heat', color: 'text-orange-500', activeBg: 'bg-orange-500/20' },
    { id: 'cool', icon: Snowflake, label: 'Cool', color: 'text-cyan-500', activeBg: 'bg-cyan-500/20' },
    { id: 'auto', icon: Leaf, label: 'Auto', color: 'text-purple-500', activeBg: 'bg-purple-500/20' },
    { id: 'off', icon: Power, label: 'Off', color: 'text-zinc-400', activeBg: 'bg-zinc-500/20' },
  ];

  const fanModes = [
    { id: 'auto', icon: RefreshCw, label: 'Auto' },
    { id: 'on', icon: Fan, label: 'On' },
  ];

  return (
    <div className="w-full max-w-sm mx-auto space-y-6">
      
      {/* System Mode Group */}
      <div className="space-y-3">
        <h3 className="text-sm font-medium text-white/50 uppercase tracking-widest pl-1">System</h3>
        <div data-testid="control-system-mode" className="grid grid-cols-4 gap-2 p-1.5 glass-panel rounded-2xl">
          {systemModes.map((mode) => {
            const isActive = systemMode === mode.id;
            const Icon = mode.icon;
            return (
              <button
                key={mode.id}
                data-testid={`button-mode-${mode.id}`}
                onClick={() => onSystemModeChange(mode.id)}
                className={cn(
                  "relative flex flex-col items-center justify-center py-3 rounded-xl transition-all duration-300",
                  isActive ? mode.activeBg : "hover:bg-white/5"
                )}
              >
                {isActive && (
                  <motion.div 
                    layoutId="activeSystemMode" 
                    className="absolute inset-0 rounded-xl border border-white/10 shadow-lg"
                    transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                  />
                )}
                <Icon size={20} className={cn("mb-1.5 relative z-10", isActive ? mode.color : "text-white/40")} />
                <span className={cn(
                  "text-xs font-medium relative z-10 transition-colors",
                  isActive ? "text-white" : "text-white/40"
                )}>
                  {mode.label}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Fan Mode Group */}
      <div className="space-y-3">
        <h3 className="text-sm font-medium text-white/50 uppercase tracking-widest pl-1">Fan</h3>
        <div data-testid="control-fan-mode" className="grid grid-cols-2 gap-2 p-1.5 glass-panel rounded-2xl">
          {fanModes.map((mode) => {
            const isActive = fanMode === mode.id;
            const Icon = mode.icon;
            return (
              <button
                key={mode.id}
                data-testid={`button-fan-${mode.id}`}
                onClick={() => onFanModeChange(mode.id)}
                className={cn(
                  "relative flex items-center justify-center gap-3 py-3 rounded-xl transition-all duration-300",
                  isActive ? "bg-white/10" : "hover:bg-white/5"
                )}
              >
                {isActive && (
                  <motion.div 
                    layoutId="activeFanMode" 
                    className="absolute inset-0 rounded-xl border border-white/10 shadow-lg"
                    transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                  />
                )}
                <Icon size={18} className={cn("relative z-10 transition-colors", isActive ? "text-white" : "text-white/40")} />
                <span className={cn(
                  "text-sm font-medium relative z-10 transition-colors",
                  isActive ? "text-white" : "text-white/40"
                )}>
                  {mode.label}
                </span>
              </button>
            );
          })}
        </div>
      </div>
      
    </div>
  );
}
