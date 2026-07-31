import { useState, type SubmitEvent} from "react"
import { useLoginMutation } from "../features/auth/authApi"
import { Link, useNavigate,  } from "react-router-dom"
import { toApiError } from "../lib/apiError";

export function LoginPage(){
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [login, { isLoading, error: rawError }] = useLoginMutation();
    const apiError = rawError ? toApiError(rawError) : null;
    const navigate = useNavigate();

    async function handleLogin(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();

        try {
            await login({ username, password }).unwrap();
            navigate("/");
        } catch {
            // RTK Query places the failed request in `rawError`.
        } finally {
            setPassword("");
        }
    }

    return(
        <form onSubmit={handleLogin}>
            <h1>Login Page</h1>
            
            <label>
                Username: 
                <input
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    placeholder="Enter username"
                    required
                />
            </label>
            
            <label>
                Password: 
                <input
                    type="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    placeholder="Password must be at least 6 chars"
                    required
                />
            </label>

            <p>
                Need a Sword ? <Link to="/register">Find here</Link>
            </p>

            <button type="submit" disabled={isLoading}>
                {isLoading ? "Logging in..." : "Login"}
            </button>

            {apiError ? (
                <p role="alert">
                    {apiError.code}: {apiError.message}
                </p>
            ) : null}
        </form>
    )
}