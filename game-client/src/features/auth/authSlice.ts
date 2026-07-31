import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { AuthState, AuthSession } from "./auth.type";

const initialState: AuthState = {
    status: "checking",
    session: null,
    isSubmitting: false,
    error: null,
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        authenticated(state, action: PayloadAction<AuthSession>) {
            state.status = "authenticated";
            state.session = action.payload;
            state.error = null
        },
        signedOut(state){
            state.status = "anonymous";
            state.session = null;
            state.isSubmitting = false;
            state.error = null
        },
        clearAuthError(state){
            state.error = null
        }
    }
})

export const {authenticated, signedOut, clearAuthError } = authSlice.actions;
export default authSlice.reducer;