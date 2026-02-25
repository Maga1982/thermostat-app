import { useState, useEffect, useRef } from "react";
import { motion, useAnimation, useMotionValue } from "framer-motion";
import { Minus, Plus, Leaf, Flame, Snowflake, Power } from "lucide-react";
import { cn } from "@/lib/utils";

interface ThermostatRingProps {
  currentTemp: number;
  targetTemp: number;
  mode: string;
  onTargetTempChange: (temp: number) => void;
}

export function ThermostatRing({ currentTemp, targetTemp, mode, onTargetTempChange }: ThermostatRingProps) {
  const [localTarget, setLocalTarget] = useState(targetTemp);
  const [isDragging, setIsDragging] = useState(false);
  const debounceTimer = useRef<NodeJS.Timeout | null>(null);

  // Sync local state when server state changes (and we aren't dragging)
  useEffect(() => {
    if (!isDragging) {
      setLocalTarget(targetTemp);
    }
  }, [targetTemp, isDragging]);

  const handleTempChange = (newTemp: number) => {
    // Clamp between 50 and 90 degrees
    const clamped = Math.min(Math.max(newTemp, 50), 90);
    setLocalTarget(clamped);
    
    // Debounce the actual API call to prevent spamming
    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      onTargetTempChange(clamped);
    }, 400); // 400ms debounce
  };

  // Determine colors based on mode
  const getModeColor = () => {
    switch (mode) {
      case "heat": return "hsl(var(--mode-heat))";
      case "cool": return "hsl(var(--mode-cool))";
      case "auto": return "hsl(var(--mode-auto))";
      default: return "hsl(var(--mode-off))";
    }
  };

  const modeColor = getModeColor();
  const isOn = mode !== "off";

  // SVG Arc calculations
  const size = 320;
  const strokeWidth = 12;
  const radius = (size - strokeWidth) / 2;
  const cx = size / 2;
  const cy = size / 2;
  const circumference = 2 * Math.PI * radius;
  
  // Create an arc from 135deg to 405deg (270 degree sweep)
  const minTemp = 50;
  const maxTemp = 90;
  const percentage = (localTarget - minTemp) / (maxTemp - minTemp);
  
  // 75% of the circle is the actual track (270 degrees)
  const trackLength = circumference * 0.75;
  const dashoffset = circumference - (percentage * trackLength);

  return (
    <div className="relative flex flex-col items-center justify-center w-full max-w-sm mx-auto">
      
      {/* Dynamic Background Glow */}
      <div 
        className="absolute inset-0 rounded-full blur-[100px] opacity-20 transition-colors duration-1000"
        style={{ backgroundColor: modeColor, transform: 'scale(0.8)' }}
      />

      <div className="relative flex items-center justify-center" style={{ width: size, height: size }}>
        
        {/* The SVG Ring */}
        <svg 
          width={size} 
          height={size} 
          viewBox={`0 0 ${size} ${size}`} 
          className="absolute inset-0 -rotate-[225deg]" // Start bottom-left
        >
          {/* Background Track */}
          <circle
            cx={cx}
            cy={cy}
            r={radius}
            fill="none"
            stroke="rgba(255,255,255,0.05)"
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={circumference * 0.25} // Remove 25% to make it an arc
            strokeLinecap="round"
          />
          
          {/* Active Track */}
          {isOn && (
            <motion.circle
              cx={cx}
              cy={cy}
              r={radius}
              fill="none"
              stroke={modeColor}
              strokeWidth={strokeWidth}
              strokeDasharray={circumference}
              strokeDashoffset={dashoffset}
              strokeLinecap="round"
              className="drop-shadow-lg"
              animate={{
                strokeDashoffset: dashoffset,
                stroke: modeColor
              }}
              transition={{ type: "spring", bounce: 0, duration: 0.5 }}
            />
          )}
        </svg>

        {/* Center Content */}
        <div className="absolute inset-0 flex flex-col items-center justify-center rounded-full z-10">
          
          <motion.div 
            className="flex flex-col items-center justify-center"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.5 }}
          >
            {/* Mode Icon */}
            <div className="mb-2 text-white/50 flex items-center gap-2 text-sm font-medium uppercase tracking-wider">
              {mode === 'heat' && <Flame size={16} style={{ color: modeColor }} />}
              {mode === 'cool' && <Snowflake size={16} style={{ color: modeColor }} />}
              {mode === 'auto' && <Leaf size={16} style={{ color: modeColor }} />}
              {mode === 'off' && <Power size={16} className="text-white/30" />}
              <span style={{ color: isOn ? modeColor : 'inherit' }}>{mode}</span>
            </div>

            {/* Huge Target Temp */}
            <div className="flex items-start">
              <span className={cn(
                "font-display font-light text-white tracking-tighter leading-none",
                isOn ? "text-8xl" : "text-7xl text-white/30"
              )}>
                {isOn ? localTarget : '--'}
              </span>
              {isOn && <span className="text-3xl font-light text-white/50 mt-2">°</span>}
            </div>

            {/* Current Temp Context */}
            <div className="mt-4 flex flex-col items-center">
              <span className="text-white/40 text-sm font-medium">Indoor</span>
              <span className="text-white/80 font-display text-xl">{currentTemp}°</span>
            </div>
          </motion.div>
        </div>

        {/* Floating +/- Controls (if On) */}
        {isOn && (
          <>
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={() => handleTempChange(localTarget - 1)}
              className="absolute left-4 bottom-12 w-14 h-14 rounded-full glass-panel flex items-center justify-center text-white/70 hover:text-white shadow-xl z-20"
            >
              <Minus size={24} />
            </motion.button>
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={() => handleTempChange(localTarget + 1)}
              className="absolute right-4 bottom-12 w-14 h-14 rounded-full glass-panel flex items-center justify-center text-white/70 hover:text-white shadow-xl z-20"
            >
              <Plus size={24} />
            </motion.button>
          </>
        )}
      </div>

      {/* Accessible Slider for rapid adjustment */}
      {isOn && (
        <div className="w-full mt-10 px-8" style={{ '--slider-color': modeColor } as React.CSSProperties}>
          <input 
            type="range" 
            min={minTemp} 
            max={maxTemp} 
            value={localTarget}
            onChange={(e) => {
              setIsDragging(true);
              setLocalTarget(parseInt(e.target.value));
            }}
            onMouseUp={() => {
              setIsDragging(false);
              handleTempChange(localTarget);
            }}
            onTouchEnd={() => {
              setIsDragging(false);
              handleTempChange(localTarget);
            }}
            className="thermostat-slider"
          />
        </div>
      )}
    </div>
  );
}
