import { useMutation } from "@tanstack/react-query";

import { BASE_API_URL } from "@/config/app-query-client";
import { LoginRequest, SignupRequest } from "@/models/Login";
import { useToken } from "@/services/TokenContext";

export interface UserProfile {
  // from JWT:
  username: string;
  email: string;
  role: string;

  // from backend:
  firstName: string;
  lastName: string;
  age: number;
  gender: "Masculino" | "Femenino" | "Prefiero no decir";
  address: string;
  base64Image?: string | null; // base64
}

import { auth } from "@/lib/api/auth-client";

export { InvalidCredentialsError, EmailNotValidatedError, UserAlreadyExistsError } from "@/lib/api/auth-client";


export function calculateAge(birthDate: string): number {
  const birth = new Date(birthDate);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }
  return age;
}

export function decodeJWT(token: string): { username: string; email: string; role: string | string[] } | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(jsonPayload);
    return {
      username: payload.sub || payload.username || '',
      email: payload.email || '',
      role: payload.role || payload.authorities || 'ROLE_USER'
    };
  } catch {
    return null;
  }
}

export function getJWTRole(token: string): string | string[] | null {
  const data = decodeJWT(token);
  return data?.role ?? null;
}

export function getRoleName(role: string | string[] | null): string {
  if (!role) return 'Estudiante';

  if (Array.isArray(role)) {
    role = role[0] || 'ROLE_USER';
  }

  switch (role) {
    case 'ROLE_ADMIN':
      return 'Administrador';
    case 'ROLE_EMPLOYEE':
      return 'Empleado';
    case 'ROLE_USER':
    default:
      return 'Usuario';
  }
}

export async function fetchUserProfile(accessToken: string): Promise<UserProfile> {
  const response = await fetch(BASE_API_URL + '/users/me', {
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    }
  });

  if (!response.ok) {
    throw new Error(`Error al obtener perfil: ${response.status}`);
  }

  const userData = await response.json();

  const jwtData = decodeJWT(accessToken);

  return {
    username: userData.username,
    email: userData.email,
    role: (Array.isArray(jwtData?.role) ? jwtData?.role[0] : jwtData?.role) || 'ROLE_USER',
    firstName: userData.firstName,
    lastName: userData.lastName,
    age: userData.age,
    gender: userData.gender as 'Masculino' | 'Femenino' | 'Prefiero no decir',
    address: userData.address,
    base64Image: userData.base64Image || undefined
  };
}

export async function updateUserProfileBackend(accessToken: string, data: Partial<Omit<UserProfile, 'role'> & { base64Image?: string | null }>): Promise<UserProfile> {
  const response = await fetch(BASE_API_URL + '/users/me', {
    method: 'PATCH',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    },
    body: JSON.stringify(data)
  });

  if (!response.ok) {
    throw new Error(`Error al actualizar perfil: ${response.status}`);
  }

  const userData = await response.json();

  const jwtData = decodeJWT(accessToken);

  return {
    username: userData.username,
    email: userData.email,
    role: (Array.isArray(jwtData?.role) ? jwtData?.role[0] : jwtData?.role) || 'ROLE_USER',
    firstName: userData.firstName,
    lastName: userData.lastName,
    age: userData.age,
    gender: userData.gender as 'Masculino' | 'Femenino' | 'Prefiero no decir',
    address: userData.address,
    base64Image: userData.base64Image || undefined
  };
}

export function useLogin() {
  const [, setToken] = useToken();
  return useMutation({
    mutationFn: async (req: LoginRequest) => {
      const backendRequest = {
        username: req.email,
        password: req.password,
      };
      const tokens = await auth("POST", "/sessions", backendRequest);
      setToken({ state: "LOGGED_IN", tokens });
    },
  });
}

export function useRefresh() {
  const [tokenState, setToken] = useToken();
  return useMutation({
    mutationFn: async () => {
      if (tokenState.state !== "LOGGED_IN") {
        return;
      }
      try {
        const refreshToken = tokenState.tokens.refreshToken;
        const tokenPromise = auth("PUT", "/sessions", { refreshToken });
        setToken({ state: "REFRESHING", tokenPromise });
        setToken({ state: "LOGGED_IN", tokens: await tokenPromise });
      } catch (err) {
        setToken({ state: "LOGGED_OUT" });
        throw err;
      }
    },
  });
}


export function useSignup() {
  return useMutation({
    mutationFn: async (req: SignupRequest) => {
      const age = calculateAge(req.birthDate);

      const backendRequest = {
        username: req.username,
        email: req.email,
        password: req.password,
        role: req.role,
        firstName: req.firstName,
        lastName: req.lastName,
        age: age,
        gender: req.gender,
        address: req.address,
        base64Image: req.profileImage,
      };
      await auth("POST", "/users", backendRequest);
      return {
        message: "Usuario registrado. Revisa tu email para activar tu cuenta.",
        userData: req
      };
    },
  });
}



export async function requestPasswordReset(email: string): Promise<void> {
  const url = BASE_API_URL + '/users/forgot-password';
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email }),
  });
  if (!response.ok) {
    if (response.status === 404) {
      throw new Error('EMAIL_NOT_FOUND');
    }
    const text = await response.text();
    throw new Error(`Error (${response.status}): ${text}`);
  }
}

export async function resetPassword(token: string, newPassword: string): Promise<void> {
  const url = BASE_API_URL + '/users/reset-password';
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ token, newPassword }),
  });
  if (!response.ok) {
    const text = await response.text();
    if (response.status === 409 && text === 'PASSWORD_SAME_AS_OLD') {
      throw new Error('PASSWORD_SAME_AS_OLD');
    }
    if (response.status === 400 && text === 'PASSWORD_INVALID') {
      throw new Error('PASSWORD_INVALID');
    }
    if (response.status === 400 && text === 'TOKEN_INVALID') {
      throw new Error('TOKEN_INVALID');
    }
    throw new Error(`Error (${response.status}): ${text}`);
  }
}
