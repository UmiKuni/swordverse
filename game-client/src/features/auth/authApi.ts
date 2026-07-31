import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react"
import type { RootState } from "../../app/store"
import type { LoginCredentials, AuthSession, RegisterCredentials, AuthUser } from "./auth.type";
import { authenticated, signedOut } from "./authSlice";
import { API_BASE_URL } from "../../config/api";

export const authApi = createApi({
    reducerPath: "authApi",
    baseQuery: fetchBaseQuery({
        baseUrl: API_BASE_URL,
        credentials: "include",
        prepareHeaders: (headers, {getState}) => {
            const accessToken = (getState() as RootState).auth.session?.accessToken;

            if (accessToken) {
                headers.set("Authorization", `Bearer ${accessToken}`)
            }

            return headers;
        }
    }),
    endpoints: (builder) => ({
        login: builder.mutation<AuthSession, LoginCredentials>({
            query: (credentials) => ({
                url: "/auth/login",
                method: "POST",
                body: credentials,
            }),
            async onQueryStarted(_credentials, { dispatch, queryFulfilled}) {
                try {
                    const { data } = await queryFulfilled;
                    dispatch(authenticated(data));
                } catch {

                }
            }
        }),

        register: builder.mutation<AuthSession, RegisterCredentials>({
            query: (credentials) => ({
                url: "/auth/register",
                method: "POST",
                body: credentials,
            }),
            async onQueryStarted(_credentials, { dispatch, queryFulfilled}) {
                try {
                    const { data } = await queryFulfilled;
                    dispatch(authenticated(data));
                } catch {

                }
            }
        }),

        refresh: builder.mutation<AuthSession, void>({
            query: () => ({
                url: "/auth/refresh",
                method: "POST",
            }),
            async onQueryStarted(_arg, { dispatch, queryFulfilled}) {
                try {
                    const { data } = await queryFulfilled;
                    dispatch(authenticated(data));
                }
                catch {
                    dispatch(signedOut());
                }
            }
        }),

        logout: builder.mutation<void, void>({
            query: () => ({
                url: "/auth/logout",
                method: "POST",
            }),
            async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
                try {
                    await queryFulfilled;
                }
                finally {
                    dispatch(signedOut());
                }
            }
        }),

        me: builder.query<AuthUser, void>({
            query: () => ({
                url: "/auth/me",
                method: "GET",
            })
        }),
    })
})

export const {
    useLoginMutation,
    useRegisterMutation,
    useRefreshMutation,
    useLogoutMutation,
    useMeQuery
} = authApi;