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
    ├── layouts/            -> Layout components for different pages   
    │
    ├── pages/              -> Page components for different routes
    │
    └── styles/