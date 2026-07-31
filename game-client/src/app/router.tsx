import { createBrowserRouter } from "react-router-dom";
import { AuthLayout } from "../layouts/AuthLayout";
import { LoginPage } from "../pages/LoginPage";
import { RootLayout } from "../layouts/RootLayout";
import { RegisterPage } from "../pages/RegisterPage";
import { ProtectedRoute } from "../features/auth/ProtetedRoute";

export const router = createBrowserRouter([
    {
        element: <RootLayout/>,
        children: [
            {
                index: true,
                element: <div>Home Page</div>
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
                        element: <div>LOBBY</div>
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