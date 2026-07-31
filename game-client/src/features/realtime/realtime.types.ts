export type RealtimeConnectionState = "disconnected" | "connecting" | "general_connected" | "error";

export interface SystemStatusEvent {
    application: string;
    status: string;
    serverTime: string;
}