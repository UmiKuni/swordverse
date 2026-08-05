import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAppSelector } from "../../app/hooks"

/**
 * The wrapper to protect the route that need authentication
 */
export function ProtectedRoute(){
    const { status, session } = useAppSelector((state) => state.auth);
    const location = useLocation()

    if(status == "checking") {
        return <p>Checking identity...</p>
    }

    if(status !== "authenticated" || !session) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: location }}
            />
        )
    }

    return <Outlet />
}