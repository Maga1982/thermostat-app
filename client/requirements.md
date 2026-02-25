## Packages
framer-motion | Essential for fluid, premium animations and interactive gestures
lucide-react | Beautiful, consistent iconography
clsx | Conditional class merging
tailwind-merge | Utility to cleanly merge Tailwind classes without conflicts

## Notes
- Assume `/api/thermostats` returns an array of devices. We will control the first one in the list.
- Implements optimistic UI updates for temperature changes with a 500ms debounce to prevent API spamming.
- The UI features a dynamic, mode-responsive color system (Heat = Orange, Cool = Blue, Auto = Purple, Off = Gray).
- Designed completely mobile-first but scales beautifully to larger displays as a dashboard.
