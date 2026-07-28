import { createBrowserRouter } from "react-router-dom";
import { AuthLayout } from "../layouts/AuthLayout";
import { LoginPage } from "../pages/LoginPage";
import { RootLayout } from "../layouts/RootLayout";

export const router = createBrowserRouter([
    {
        element: <RootLayout/>,
        children: [
            {
                index: true,
                element: <div></div>
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
                        element: <div>Register page</div>
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