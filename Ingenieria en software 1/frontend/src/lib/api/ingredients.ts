import { buildQueryString, PaginatedResponse, toPaginatedResponse } from "./helpers";
import { BASE_API_URL } from "../../config/app-query-client";

export interface IngredientDTO {
  id: number;
  name: string;
  stock: number;
  base64Image: string;
}

export interface IngredientCreateDTO {
  name: string;
  stock: number;
  base64Image: string;
}

export interface IngredientUpdateDTO {
  name: string;
  stock: number;
  base64Image?: string;
}

export interface IngredientStockDTO {
  stock: number;
}

export interface IngredientQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  name?: string;
  stockAsc?: boolean;
}

const API_BASE_URL = BASE_API_URL;

export async function fetchIngredientsPage(
  accessToken: string,
  params: IngredientQueryParams = {},
): Promise<PaginatedResponse<IngredientDTO>> {
  const query = buildQueryString(params as Record<string, unknown>);
  const response = await fetch(`${API_BASE_URL}/ingredients${query}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos para acceder a los ingredientes.');
    }
    throw new Error(`Error al cargar ingredientes: ${response.status}`);
  }

  const raw = await response.json();
  return toPaginatedResponse<IngredientDTO>(raw);
}

export async function fetchIngredients(
  accessToken: string,
  params: Omit<IngredientQueryParams, 'page'> = {},
): Promise<IngredientDTO[]> {
  const { size = 200, ...rest } = params;
  const collected: IngredientDTO[] = [];
  let page = 0;
  let hasMore = true;

  while (hasMore) {
    const response = await fetchIngredientsPage(accessToken, { ...rest, page, size });
    collected.push(...response.content);

    if (response.last || collected.length >= response.totalElements) {
      hasMore = false;
    } else {
      page += 1;
      if (page >= response.totalPages) {
        hasMore = false;
      }
    }
  }

  return collected;
}

export async function createIngredient(
  accessToken: string,
  data: IngredientCreateDTO
): Promise<IngredientDTO> {
  const response = await fetch(`${API_BASE_URL}/ingredients`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para crear ingredientes.');
    }
    if (response.status === 409) {
      throw new Error('Ya existe un ingrediente con ese nombre.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para crear el ingrediente.');
    }
    throw new Error(`Error al crear ingrediente: ${response.status}`);
  }

  return response.json();
}

export async function updateIngredient(
  accessToken: string,
  id: number,
  data: IngredientUpdateDTO
): Promise<IngredientDTO> {
  const response = await fetch(`${API_BASE_URL}/ingredients/${id}`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para actualizar ingredientes.');
    }
    if (response.status === 404) {
      throw new Error('El ingrediente no existe.');
    }
    if (response.status === 409) {
      throw new Error('Ya existe un ingrediente con ese nombre.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para actualizar el ingrediente.');
    }
    throw new Error(`Error al actualizar ingrediente: ${response.status}`);
  }

  return response.json();
}

export async function deleteIngredient(accessToken: string, id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/ingredients/${id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para eliminar ingredientes.');
    }
    if (response.status === 404) {
      throw new Error('El ingrediente no existe.');
    }
    if (response.status === 409) {
      throw new Error('No se puede eliminar este ingrediente porque es el único en al menos un producto.');
    }
    throw new Error(`Error al eliminar ingrediente: ${response.status}`);
  }
}

export async function updateIngredientStock(
  accessToken: string,
  id: number,
  data: IngredientStockDTO
): Promise<IngredientDTO> {
  const response = await fetch(`${API_BASE_URL}/ingredients/${id}/stock`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para actualizar el stock de ingredientes.');
    }
    if (response.status === 404) {
      throw new Error('El ingrediente no existe.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para actualizar el stock del ingrediente.');
    }
    throw new Error(`Error al actualizar stock del ingrediente: ${response.status}`);
  }

  return response.json();
}
