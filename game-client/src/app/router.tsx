import { createBrowserRouter } from "react-router-dom";
import { AuthLayout } from "../layouts/AuthLayout";
import { LoginPage } from "../pages/LoginPage";
import { RootLayout } from "../layouts/RootLayout";
import { RegisterPage } from "../pages/RegisterPage";

export const router = createBrowserRouter([
    {
        element: <RootLayout/>,
        children: [
            {
                index: true,
                element: <div>Client Page</div>
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
                path: "*",
                element: <div>Not found</div>
            }
        ]
    }
])