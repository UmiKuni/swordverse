export interface AuthUser {
    userId: string;
    username: string;
    displayName: string;
}

export interface LoginCredentials {
    username: string;
    password: string;
}

export interface RegisterCredentials {
    displayName: string;
    username: string;
    password: string;
}

export interface AuthSession {
    accessToken: string;
    accessTokenExpiresAt: number;
    refreshTokenExpiresAt: number;
    sessionId: string;
    user: AuthUser
}

export type AuthStatus = "checking" | "anonymous" | "authenticated";

export interface AuthState {
    status: AuthStatus;
    session: AuthSession | null;
    isSubmitting: boolean;
    error: string | null;
}