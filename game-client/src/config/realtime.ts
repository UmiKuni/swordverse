const realtimeUrl = import.meta.env.VITE_REALTIME_URL

if(!realtimeUrl) {
    throw new Error("VITE_REALTIME_URL is required")
}

export const REALTIME_URL = realtimeUrl;