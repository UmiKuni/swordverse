import { useState } from "react"

export function LoginPage(){
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    return(
        <form onSubmit={() => {}}>
            <h1>Login Page</h1>
            
            <label>
                Username
                <input
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    placeholder="Enter username or email"
                />
            </label>
            
            <label>
                Password
                <input
                    type="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    placeholder="Password must be at least 6 chars"
                />
            </label>

            <button type="submit">
                Login
            </button>
        </form>
    )
}