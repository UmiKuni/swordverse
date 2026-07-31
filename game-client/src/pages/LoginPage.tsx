import { useState, type SubmitEvent} from "react"
import { useLoginMutation } from "../features/auth/authApi"
import { Link, useNavigate,  } from "react-router-dom"
import { type ClientApiError, toApiError } from "../lib/apiError";

export function LoginPage(){
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [formError, setFormError] = useState<ClientApiError | null>(null);

    const [login, { isLoading, error }] = useLoginMutation();
    const navigate = useNavigate();

    async function handleLogin(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();

        try {
            await login({ username, password }).unwrap();
            navigate("/");
        } catch(reason) {
            setFormError(toApiError(reason))
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

            {formError ? (
                <p role="alert">
                    {formError.code}: {formError.message}
                </p>
            ) : null}
        </form>
    )
}