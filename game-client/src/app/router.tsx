import { createBrowserRouter } from "react-router-dom";
import { AuthLayout } from "../layouts/AuthLayout";
import { LoginPage } from "../pages/LoginPage";
import { RootLayout } from "../layouts/RootLayout";
import { RegisterPage } from "../pages/RegisterPage";
import { ProtectedRoute } from "../features/auth/ProtetedRoute";
import { HomePage } from "../pages/HomePage";
import { LobbyPage } from "../pages/LobbyPage";

export const router = createBrowserRouter([
    {
        element: <RootLayout/>,
        children: [
            {
                index: true,
                element: <HomePage/>
            },
            {
                element: <AuthLayout/>,
                children: [
                    {
                        path: "login",
                        element: <LoginPage/>
                    },
                    {
                        path: "register",
                        element: <RegisterPage/>
                    }
                ]
            },
            {
                element: <ProtectedRoute />,
                children: [
                    {
                        path: "lobby",
                        element: <LobbyPage/>
                    }
                ]
            },
            {
                path: "*",
                element: <div>Not found</div>
            }
        ]
    }
])