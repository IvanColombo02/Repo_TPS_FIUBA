import { QueryClient } from "@tanstack/react-query";

const runtimeBaseApi = typeof window !== "undefined" ? window._env_?.baseApiUrl : undefined;
export const BASE_API_URL = runtimeBaseApi || import.meta.env.VITE_BASE_API_URL;

export const appQueryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5,
            refetchOnWindowFocus: false,
            retry: 1,
        },
    },
});
