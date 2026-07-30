import { useState, type SubmitEvent } from "react"
import { Link, useNavigate } from "react-router-dom";
import { useRegisterMutation } from "../features/auth/authApi";

export function RegisterPage(){
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [displayName, setDisplayName] = useState("");

    const [register, { error, isLoading }] = useRegisterMutation();
    const navigate = useNavigate();

    async function handleRegister(event: SubmitEvent<HTMLFormElement>){
        event.preventDefault()

        try {
            await register({ displayName, username, password }).unwrap();
            navigate("/");
        } catch {
            // RTK Query exposes the failure through `error`.
        }
    }

    return(
        <form onSubmit={handleRegister}>
            <h1> Register Page </h1>

            <label>
                Your Display Name: 
                <input
                    value={displayName}
                    onChange={e => setDisplayName(e.target.value)}
                    placeholder="Try some fancy name!"
                    required
                />
            </label>

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
                Already have a sword ? <Link to="/login">Enter now</Link>
            </p>

            <button type="submit" disabled={isLoading}>
                {isLoading ? "Registering..." : "Register"}
            </button>

            {error ? <p role="alert">Invalid with error:</p> : null}
        </form>
    )
}