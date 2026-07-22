/** Owns the single SockJS/STOMP client and its connect, disconnect, and reconnect lifecycle. */
import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import type { SystemStatusEvent } from "./messageTypes";

const SYSTEM_STATUS_TOPIC = "/topic/system/status";
const SYSTEM_STATUS_COMMAND = "/app/system/status";

export type RealtimeCallbacks = {
  onConnected: () => void;
  onDisconnected: () => void;
  onSystemStatus: (event: SystemStatusEvent) => void;
  onError: (message: string) => void;
};

let client: Client | null = null;
let systemStatusSubscription: StompSubscription | null = null;

export function connectRealtime(callbacks: RealtimeCallbacks): void {
  if (client?.active) {
    return;
  }

  client = new Client({
    brokerURL: "ws://localhost:8080/ws",

    reconnectDelay: 0,

    debug(message) {
      console.log("[SwordVerse STOMP]", message);
    },

    onConnect() {
      callbacks.onConnected();

      systemStatusSubscription = client!.subscribe(
        SYSTEM_STATUS_TOPIC,
        (message: IMessage) => {
          try {
            const event = JSON.parse(message.body) as SystemStatusEvent;

            callbacks.onSystemStatus(event);
          } catch {
            callbacks.onError(
              "SwordVerse returned an invalid system status message.",
            );
          }
        },
      );
    },

    onStompError(frame) {
      callbacks.onError(
        frame.headers.message ?? "SwordVerse STOMP connection failed.",
      );
    },

    onWebSocketError() {
      callbacks.onError("Could not connect to the SwordVerse realtime server.");
    },

    onWebSocketClose() {
      systemStatusSubscription = null;
      callbacks.onDisconnected();
    },
  });

  client.activate();
}

export function requestSystemStatus(): void {
  if (!client?.connected) {
    throw new Error("SwordVerse realtime connection is not ready.");
  }

  client.publish({
    destination: SYSTEM_STATUS_COMMAND,
    body: "{}",
  });
}

export async function disconnectRealtime(): Promise<void> {
  systemStatusSubscription?.unsubscribe();
  systemStatusSubscription = null;

  if (client !== null) {
    await client.deactivate();
    client = null;
  }
}

export function isRealtimeConnected(): boolean {
  return client?.connected === true;
}
