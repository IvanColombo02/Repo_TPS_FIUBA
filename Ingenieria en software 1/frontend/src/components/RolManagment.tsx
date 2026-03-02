import { useEffect, useState, useRef } from "react";
import { useAccessTokenGetter } from "@/services/TokenContext";
import { BASE_API_URL } from "@/config/app-query-client";

interface User {
    id: number;
    firstName: string;
    lastName: string;
    age: number;
    gender: string;
    address: string;
    base64Image: string | null;
    username: string;
    email: string;
    role: string;
}

export default function AdminGestionRoles() {
    const [users, setUsers] = useState<User[]>([]);
    const [currentUserEmail, setCurrentUserEmail] = useState<string | null>(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const getAccessToken = useAccessTokenGetter();
    const getAccessTokenRef = useRef(getAccessToken);

    getAccessTokenRef.current = getAccessToken;

    useEffect(() => {
        let cancelled = false;

        const fetchUsers = async () => {
            try {
                setLoading(true);
                setError(null);
                const token = await getAccessTokenRef.current();

                if (cancelled) return;

                const response = await fetch(`${BASE_API_URL}/users`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });

                if (cancelled) return;

                if (!response.ok) {
                    if (response.status === 401) {
                        throw new Error("No autorizado. Inicia sesión nuevamente.");
                    }
                    if (response.status === 403) {
                        throw new Error("No tienes permisos para acceder a los usuarios.");
                    }
                    if (response.status === 404) {
                        throw new Error("El endpoint de usuarios no está disponible.");
                    }
                    throw new Error(`Error al cargar usuarios: ${response.status}`);
                }

                const data: User[] = await response.json();
                if (!cancelled && Array.isArray(data)) {
                    setUsers(data);
                }
            } catch {
                if (!cancelled) {
                    setError("Error al cargar usuarios");
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        const fetchCurrentUser = async () => {
            try {
                const token = await getAccessTokenRef.current();
                const response = await fetch(`${BASE_API_URL}/users/me`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });

                if (response.ok) {
                    const user = await response.json();
                    setCurrentUserEmail(user.email);
                }
            } catch {
                console.error("Error fetching current user");
            }
        };

        fetchUsers();
        fetchCurrentUser();

        return () => { cancelled = true };
    }, []);

    const handleRoleChange = async (id: number, newRole: string) => {
        try {
            const token = await getAccessTokenRef.current();
            const response = await fetch(`${BASE_API_URL}/users/${id}/role`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ role: newRole }),
            });

            if (!response.ok) {
                throw new Error("Error al actualizar rol");
            }

            setUsers((prev) =>
                prev.map((u) => (u.id === id ? { ...u, role: newRole } : u))
            );
        } catch {
            alert("Error al actualizar rol");
        }
    };

    if (loading) return <p className="p-4 text-white">Cargando usuarios...</p>;
    if (error) {
        return (
            <div className="p-4">
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                    <p className="font-semibold">Error</p>
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="p-4">
            <h2 className="text-xl font-semibold mb-4 text-white">Gestión de Roles</h2>
            <table className="w-full border-collapse border border-gray-300 text-sm">
                <thead>
                <tr className="bg-gray-800">
                    <th className="border p-2 text-white">ID</th>
                    <th className="border p-2 text-white">Nombre</th>
                    <th className="border p-2 text-white">Email</th>
                    <th className="border p-2 text-white">Rol</th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => {
                    const isCurrentUser = user.email === currentUserEmail;

                    return (
                        <tr key={user.id} className="bg-gray-900">
                            <td className="border p-2 text-white">{user.id}</td>
                            <td className="border p-2 text-white">
                                {user.firstName && user.lastName
                                    ? `${user.firstName} ${user.lastName}`
                                    : user.username}
                            </td>
                            <td className="border p-2 text-white">{user.email}</td>

                            <td className="border p-2">
                                <select
                                    value={user.role}
                                    onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                    disabled={isCurrentUser}
                                    className={`border rounded p-1 text-white bg-gray-800 
                                            ${isCurrentUser ? "opacity-60 cursor-not-allowed" : ""}`}
                                >
                                    <option value="ROLE_USER">Usuario</option>
                                    <option value="ROLE_EMPLOYEE">Empleado</option>
                                    <option value="ROLE_ADMIN">Administrador</option>
                                </select>
                            </td>
                        </tr>
                    );
                })}
                </tbody>
            </table>
        </div>
    );
}

