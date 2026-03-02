/* eslint-disable react-refresh/only-export-components */
import React, { Dispatch, useCallback, useContext, useState } from "react";

import type { AuthResponse } from "@/models/Login";
import { auth } from "@/lib/api/auth-client";

type TokenContextData =
  | {
    state: "LOGGED_OUT";
  }
  | {
    state: "REFRESHING";
    tokenPromise: Promise<AuthResponse>;
  }
  | {
    state: "LOGGED_IN";
    tokens: AuthResponse;
  };

const TOKEN_STORAGE_KEY = "authTokens";

const TokenContext = React.createContext<[TokenContextData, Dispatch<TokenContextData>, () => Promise<AuthResponse | void>] | null>(null);

export const TokenProvider = ({ children }: React.PropsWithChildren) => {
  const [state, setInternalState] = useState<TokenContextData>(getInitialTokenState);
  const setState = useCallback(
    (state: TokenContextData) => {
      setInternalState(state);
      // Sincronizar con localStorage
      if (state.state === "LOGGED_IN") {
        localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(state.tokens));
      } else if (state.state === "LOGGED_OUT") {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
      }
    },
    [setInternalState],
  );

  const refreshingPromiseRef = React.useRef<Promise<AuthResponse> | null>(null);

  const refreshTokens = useCallback(async () => {
    if (state.state !== "LOGGED_IN") {
      return;
    }

    if (refreshingPromiseRef.current) {
      return refreshingPromiseRef.current;
    }

    try {
      const refreshToken = state.tokens.refreshToken;

      const promise = auth("PUT", "/sessions", { refreshToken });
      refreshingPromiseRef.current = promise;


      setInternalState(prev => ({ ...prev, state: "REFRESHING", tokenPromise: promise }));

      const newTokens = await promise;

    
      setInternalState({ state: "LOGGED_IN", tokens: newTokens });

      localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(newTokens));

      return newTokens;
    } catch (err) {
      console.error("Refresh failed", err);
      setInternalState({ state: "LOGGED_OUT" });
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      throw err;
    } finally {

      refreshingPromiseRef.current = null;
    }
  }, [state, setInternalState]);

  return <TokenContext.Provider value={[state, setState, refreshTokens]}>{children}</TokenContext.Provider>;
};

export function useToken() {
  const context = useContext(TokenContext);
  if (context === null) {
    throw new Error("React tree should be wrapped in TokenProvider");
  }
  return context;
}

export function useAccessTokenGetter() {
  const [tokenState] = useToken();

  return async function getAccessToken() {
    switch (tokenState.state) {
      case "LOGGED_OUT":
        throw new Error("Auth needed for service");
      case "REFRESHING":
        return (await tokenState.tokenPromise).accessToken;
      case "LOGGED_IN":
        return tokenState.tokens.accessToken;
      default:
        // Make the compiler check this is unreachable
        return tokenState satisfies never;
    }
  };
}

export function useHandleResponse() {
  const [, , refreshTokens] = useToken();

  return async function handleResponse<T>(response: Response, parse: (json: unknown) => T) {
    if (response.status === 401) {
      await refreshTokens();
      throw new Error("Attempting token refresh");
    } else if (response.ok) {
      return parse(await response.json());
    } else {
      throw new Error(`Failed with status ${response.status}: ${await response.text()}`);
    }
  };
}

const getInitialTokenState = (): TokenContextData => {
  try {
    const storedTokens = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (storedTokens) {
      const tokens: AuthResponse = JSON.parse(storedTokens);
      if (tokens.accessToken && tokens.refreshToken) {
        return { state: "LOGGED_IN", tokens };
      }
    }
  } catch (error) {
    console.error("Error reading token from localStorage:", error);
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }
  return { state: "LOGGED_OUT" };
};
