import { useEffect, useRef, type ReactNode } from "react";
import { useRefreshMutation } from "./authApi";

/**
 * This is the wrapper for run refresh once when the app starts
 */
export function AuthBootstrap({ children }: {children: ReactNode}) {
    const [refresh] = useRefreshMutation();
    const started = useRef(false);

    useEffect(() => {
        if(started.current) {
            return;
        }

        started.current = true;
        void refresh();
    }, [refresh]);

    return children;
}