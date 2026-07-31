import { useNavigate } from "react-router-dom"
import { useAppSelector } from "../app/hooks"
import { useEffect, useState } from "react";
import type { RealtimeConnectionState, SystemStatusEvent } from "../features/realtime/realtime.types";
import { connectRealtime } from "../features/realtime/realtimeClient";

export function LobbyPage() {
    const {session} = useAppSelector((state) => state.auth);
    const navigate = useNavigate();

    const accessToken = session?.accessToken;
    const [ connectionState, setConnectionState] = useState<RealtimeConnectionState>("disconnected")
    const [ systemStatus, setSystemStatus] = useState<SystemStatusEvent | null>(null);

    useEffect(() => {
        if(!accessToken) {
            return;
        }

        return connectRealtime(accessToken, {
            onStateChange: setConnectionState,
            onSystemStatus: setSystemStatus,
        })
    }, [accessToken])

    return (
        <>
        <h1>Lobby</h1>

        <h2>Profile</h2>

        <p>
            Display name: <span>{session?.user.displayName}</span>
        </p>

        <p>
            Username: <span>{session?.user.username}</span>
        </p>

        <p>
            ID: <span>{session?.user.userId}</span>
        </p>

        <p>Realtime: {connectionState}</p>

        {systemStatus ? (
        <p>
            Server: {systemStatus.application} - {systemStatus.status}
        </p>
        ) : null}

        <button type="button" onClick={() => navigate("/")}>Back to home</button>
        </>
    )
}