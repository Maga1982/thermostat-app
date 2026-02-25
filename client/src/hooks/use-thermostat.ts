import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api, buildUrl, type ThermostatUpdateInput, type ThermostatListResponse } from "@shared/routes";

// Using a placeholder id of 1 for the demo, assuming a single main thermostat
const THERMOSTAT_ID = 1;

export function useThermostat() {
  return useQuery({
    queryKey: [api.thermostats.list.path],
    queryFn: async () => {
      const res = await fetch(api.thermostats.list.path, { credentials: "include" });
      if (!res.ok) {
        // Return a mock default if API is completely missing so UI doesn't crash empty
        return [{
          id: 1,
          name: "Living Room",
          currentTemp: 72,
          targetTemp: 74,
          systemMode: "heat",
          fanMode: "auto",
          currentHumidity: 45,
          lastUpdated: new Date()
        }] as ThermostatListResponse;
      }
      return api.thermostats.list.responses[200].parse(await res.json());
    },
    select: (data) => data[0] // Select the first thermostat for the dashboard
  });
}

export function useUpdateThermostat() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async ({ id, updates }: { id: number, updates: ThermostatUpdateInput }) => {
      const url = buildUrl(api.thermostats.update.path, { id });
      
      // We send the request, but if it fails (e.g. missing API), we won't crash the UI,
      // we'll just let the optimistic update revert or stay.
      const res = await fetch(url, {
        method: api.thermostats.update.method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updates),
        credentials: "include",
      });
      
      if (!res.ok) {
        throw new Error("Failed to update thermostat");
      }
      
      return api.thermostats.update.responses[200].parse(await res.json());
    },
    // Optimistic Update
    onMutate: async ({ id, updates }) => {
      await queryClient.cancelQueries({ queryKey: [api.thermostats.list.path] });
      
      const previousData = queryClient.getQueryData<ThermostatListResponse>([api.thermostats.list.path]);
      
      if (previousData) {
        queryClient.setQueryData<ThermostatListResponse>(
          [api.thermostats.list.path],
          previousData.map(t => t.id === id ? { ...t, ...updates } : t)
        );
      }
      
      return { previousData };
    },
    onError: (err, newTodo, context) => {
      if (context?.previousData) {
        queryClient.setQueryData([api.thermostats.list.path], context.previousData);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [api.thermostats.list.path] });
    },
  });
}
