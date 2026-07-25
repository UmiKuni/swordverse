import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

const rootElement = document.getElementById('root')

if (!rootElement) {
    throw new Error('Root element #root was not found')
}

createRoot(rootElement).render(
    <StrictMode>
        <main>
            <h1>Swordverse</h1>
            <p>This is client :D</p>
        </main>
    </StrictMode>
)