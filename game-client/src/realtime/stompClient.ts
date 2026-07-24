import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import type { SystemStatusEvent } from "./messageTypes";

const SYSTEM_STATUS_TOPIC = "/topic/system/status";
const SYSTEM_STATUS_COMMAND = "/app/system/status";

/** Callbacks through which the singleton realtime client reports lifecycle and domain events. */
export type RealtimeCallbacks = {
  onConnected: () => void;
  onDisconnected: () => void;
  onSystemStatus: (event: SystemStatusEvent) => void;
  onError: (message: string) => void;
};

let client: Client | null = null;
let systemStatusSubscription: StompSubscription | null = null;

/**
 * Starts the single SwordVerse STOMP lifecycle and authenticates CONNECT with an in-memory access
 * token. Calls made while another lifecycle exists are ignored.
 *
 * @param accessToken current SwordVerse JWT access token
 * @param callbacks consumers for connection, error, and system-status events
 */
export function connectRealtime(
  accessToken: string,
  callbacks: RealtimeCallbacks,
): void {
  if (client !== null) {
    return;
  }

  const nextClient = new Client({
    brokerURL: "ws://localhost:8080/ws",

    connectHeaders: {
      Authorization: `Bearer ${accessToken}`,
    },

    reconnectDelay: 0,

    debug(message) {
      console.log("[SwordVerse STOMP]", message);
    },

    onConnect() {
      if (client !== nextClient) {
        return;
      }

      callbacks.onConnected();

      systemStatusSubscription = nextClient.subscribe(
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

    onStompError() {
      if (client !== nextClient) {
        return;
      }

      callbacks.onError("SwordVerse rejected the realtime connection.");

      void disposeFailedClient(nextClient, callbacks);
    },

    onWebSocketError() {
      if (client !== nextClient) {
        return;
      }

      callbacks.onError("Could not connect to the SwordVerse realtime server.");
    },

    onWebSocketClose(event) {
      if (client !== nextClient) {
        return;
      }

      console.info(
        "[SwordVerse WebSocket] Closed",
        { code: event.code, reason: event.reason, wasClean: event.wasClean },
      );

      client = null;
      systemStatusSubscription = null;

      if (event.code === 1008) {
        callbacks.onError(
          "Your SwordVerse authentication session is no longer active.",
        );
      }

      callbacks.onDisconnected();
    },
  });

  client = nextClient;
  nextClient.activate();
}

/**
 * Disposes a rejected STOMP lifecycle before allowing another connection attempt. The client
 * reference is cleared synchronously so late callbacks from the failed instance cannot own a new
 * lifecycle.
 */
async function disposeFailedClient(
  failedClient: Client,
  callbacks: RealtimeCallbacks,
): Promise<void> {
  if (client !== failedClient) {
    return;
  }

  client = null;
  systemStatusSubscription = null;

  try {
    await failedClient.deactivate();
  } finally {
    callbacks.onDisconnected();
  }
}

/** Publishes an authenticated diagnostic request for the current SwordVerse server status. */
export function requestSystemStatus(): void {
  if (!client?.connected) {
    throw new Error("SwordVerse realtime connection is not ready.");
  }

  client.publish({
    destination: SYSTEM_STATUS_COMMAND,
    body: "{}",
  });
}

/**
 * Gracefully unsubscribes and deactivates the current STOMP client. Calling it without an active
 * client is safe.
 */
export async function disconnectRealtime(): Promise<void> {
  const currentClient = client;

  client = null;

  systemStatusSubscription?.unsubscribe();
  systemStatusSubscription = null;

  if (currentClient !== null) {
    await currentClient.deactivate();
  }
}

/** Returns whether the singleton client has completed a STOMP CONNECT handshake. */
export function isRealtimeConnected(): boolean {
  return client?.connected === true;
}
