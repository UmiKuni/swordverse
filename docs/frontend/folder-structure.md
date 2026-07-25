# Folder Structure
game-client/
├── public/                 -> Static assets served by the web server
│   └── assets/ 
│
└── src/                    -> Application source code
    ├── main.tsx
    │
    ├── app/                -> Application entry point and global state
    │   ├── App.tsx
    │
    ├── api/                -> HTTP/RTK query API definitions and types
    │
    ├── realtime/           -> WebSocket/Socket.IO connection by STOMP
    │
    ├── features/           -> Feature-specific state management and API logic
    │
    ├── pages/              -> React components for each route/page
    │
    ├── shared/             -> Shared components, hooks, and utilities
    │
    └── styles/