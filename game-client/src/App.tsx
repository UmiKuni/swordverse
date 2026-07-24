/** Root application shell. It will render application providers and routes. */
import { useEffect, useState } from "react";
import "./App.css";
import {
  connectRealtime,
  disconnectRealtime,
  requestSystemStatus,
} from "./realtime/stompClient";
import type { SystemStatusEvent } from "./realtime/messageTypes";

type ConnectionStatus = "DISCONNECTED" | "CONNECTING" | "CONNECTED";

function App() {
  const [connectionStatus, setConnectionStatus] =
    useState<ConnectionStatus>("DISCONNECTED");

  const [accessToken, setAccessToken] = useState("");

  const [systemStatus, setSystemStatus] = useState<SystemStatusEvent | null>(
    null,
  );

  const [error, setError] = useState<string | null>(null);

  function connect() {
    setError(null);

    if (accessToken.trim() === "") {
      setError("Access token is required.");
      return;
    }

    setConnectionStatus("CONNECTING");

    connectRealtime(accessToken.trim(), {
      onConnected() {
        setConnectionStatus("CONNECTED");
      },

      onDisconnected() {
        setConnectionStatus("DISCONNECTED");
      },

      onSystemStatus(event) {
        setSystemStatus(event);
      },

      onError(message) {
        setError(message);
      },
    });
  }

  function requestStatus() {
    setError(null);

    try {
      requestSystemStatus();
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Could not request SwordVerse status.",
      );
    }
  }

  async function disconnect() {
    setError(null);
    await disconnectRealtime();
    setConnectionStatus("DISCONNECTED");
  }

  useEffect(() => {
    return () => {
      void disconnectRealtime();
    };
  }, []);

  return (
    <main className="realtime-page">
      <section className="realtime-panel">
        <h1>SwordVerse Realtime</h1>

        <p>
          Connection: <strong>{connectionStatus}</strong>
        </p>

        <label className="token-field">
          <span>Access token</span>

          <textarea
            value={accessToken}
            onChange={(event) => {
              setAccessToken(event.target.value);
            }}
            disabled={connectionStatus !== "DISCONNECTED"}
            rows={5}
            placeholder="Paste the SwordVerse access token"
          />
        </label>

        <div className="actions">
          <button
            type="button"
            onClick={connect}
            disabled={connectionStatus !== "DISCONNECTED"}
          >
            Connect realtime
          </button>

          <button
            type="button"
            onClick={requestStatus}
            disabled={connectionStatus !== "CONNECTED"}
          >
            Request server status
          </button>

          <button
            type="button"
            onClick={() => void disconnect()}
            disabled={connectionStatus === "DISCONNECTED"}
          >
            Disconnect
          </button>
        </div>

        {systemStatus !== null && (
          <section className="server-status">
            <h2>Server status</h2>

            <dl>
              <dt>Application</dt>
              <dd>{systemStatus.application}</dd>

              <dt>Status</dt>
              <dd>{systemStatus.status}</dd>

              <dt>Server time</dt>
              <dd>{systemStatus.serverTime}</dd>
            </dl>
          </section>
        )}

        {error !== null && (
          <p role="alert" className="error">
            {error}
          </p>
        )}
      </section>
    </main>
  );
}

export default App;
