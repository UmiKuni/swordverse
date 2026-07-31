import { RouterProvider } from "react-router-dom";
import { router } from "./router";
import { AuthBootstrap } from "../features/auth/AuthBootstrap";

export function App(){
    return (
        <AuthBootstrap>
            <RouterProvider router={router}></RouterProvider>
        </AuthBootstrap>
    )
}