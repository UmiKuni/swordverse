import { useNavigate } from "react-router-dom"
import { useAppSelector } from "../app/hooks"

export function LobbyPage() {
    const {session} = useAppSelector((state) => state.auth);
    const navigate = useNavigate();
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

        <button type="button" onClick={() => navigate("/")}>Back to home</button>
        </>
    )
}