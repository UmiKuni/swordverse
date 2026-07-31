import { Client } from "@stomp/stompjs";
import { REALTIME_URL } from "../../config/realtime";
import type { RealtimeConnectionState, SystemStatusEvent } from "./realtime.types";

interface RealtimeCallbacks {
    onStateChange: (state: RealtimeConnectionState) => void;
    onSystemStatus: (event: SystemStatusEvent) => void;
}

function isSystemStatusEvent(value: unknown): value is SystemStatusEvent {
    if(typeof value !== "object" || value === null){
        return false;
    }

    const event = value as Record<string, unknown>;

    return (
        typeof event.application === "string" &&
        typeof event.status === "string" &&
        typeof event.serverTime === "string"
    );
}

export function connectRealtime(
    accessToken: string,
    callbacks: RealtimeCallbacks,
) {
    let disposed = false;

    const client = new Client({
        brokerURL: REALTIME_URL,
        connectHeaders: {
            Authorization: `Bearer ${accessToken}`,
        },
        reconnectDelay: 0,

        onConnect: () => {
            callbacks.onStateChange("general_connected");


            client.subscribe("/topic/system/status", (message) => {
                const event: unknown = JSON.parse(message.body);

                if (isSystemStatusEvent(event)) {
                    callbacks.onSystemStatus(event);
                }
            });

            client.publish({
                destination: "/app/system/status"
            })
        },

        onStompError: () => {
            if(!disposed) {
                callbacks.onStateChange("error")
            }
        },

        onWebSocketClose: () => {
            if(!disposed) {
                callbacks.onStateChange("disconnected")
            }
        }
    })

    callbacks.onStateChange("connecting");
    client.activate();

    return () => {
        disposed = true;
        void client.deactivate();
    }
}