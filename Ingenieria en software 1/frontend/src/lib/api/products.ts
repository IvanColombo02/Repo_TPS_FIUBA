import { buildQueryString, PaginatedResponse, toPaginatedResponse } from "./helpers"
import { BASE_API_URL } from "../../config/app-query-client";


function normalizeCategories(categories: unknown): string[] {
  if (Array.isArray(categories)) return categories as string[];
  if (typeof categories === 'string') return [categories];
  return [];
}
export interface ProductDTO {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  categories: string[];
  type: string;
  estimatedTime: number;
  ingredients: Record<string, number>;
  base64Image: string;
}

export interface ProductSimpleDTO {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  categories: string[];
  type: string;
  estimatedTime: number;
  base64Image?: string;
}

export interface ProductCreateDTO {
  name: string;
  description: string;
  price: number;
  categories: string[];
  type: string;
  estimatedTime: number;
  ingredientsIds: Record<string, number>;
  base64Image: string;
}

export interface ProductUpdateDTO {
  name: string;
  description: string;
  price: number;

  categories: string[];
  type: string;
  estimatedTime: number;
  ingredientsIds: Record<string, number>;
  base64Image?: string;
}

export interface ProductStockDTO {
  stock: number;
}

const API_BASE_URL = BASE_API_URL;

export interface ProductQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  name?: string;
  priceMin?: number;
  priceMax?: number;
  stockAsc?: boolean;
  priceAsc?: boolean;
  categories?: string[];
  type?: string[];
}

export async function fetchProductsPage(
  accessToken: string,
  params: ProductQueryParams = {},
): Promise<PaginatedResponse<ProductDTO>> {
  const query = buildQueryString(params as Record<string, unknown>);
  const response = await fetch(`${API_BASE_URL}/products${query}`, {
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
      throw new Error('No tienes permisos para acceder a los productos.');
    }
    throw new Error(`Error al cargar productos: ${response.status}`);
  }

  const raw = await response.json();
  const page = toPaginatedResponse<unknown>(raw);

  const normalizedContent = page.content.map((product) => ({
    ...(product as Record<string, unknown>),
    categories: normalizeCategories((product as { categories?: unknown }).categories),
    ingredients: ((product as { ingredients?: unknown }).ingredients ?? {}) as Record<string, number>,
  })) as ProductDTO[];

  return {
    ...page,
    content: normalizedContent,
  };
}

export async function fetchProductsSimplePage(
  accessToken: string,
  params: ProductQueryParams = {},
): Promise<PaginatedResponse<ProductSimpleDTO>> {
  const query = buildQueryString(params as Record<string, unknown>);
  const response = await fetch(`${API_BASE_URL}/products${query}`, {
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
      throw new Error('No tienes permisos para acceder a los productos.');
    }
    throw new Error(`Error al cargar productos: ${response.status}`);
  }

  const raw = await response.json();
  const page = toPaginatedResponse<unknown>(raw);

  const normalizedContent = page.content.map((product) => ({
    ...(product as Record<string, unknown>),
    categories: normalizeCategories((product as { categories?: unknown }).categories),
  })) as ProductSimpleDTO[];

  return {
    ...page,
    content: normalizedContent,
  };
}

export async function fetchProductById(accessToken: string, id: number): Promise<ProductDTO> {
  const response = await fetch(`${API_BASE_URL}/products/${id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    if (response.status === 401) throw new Error('No autorizado');
    if (response.status === 403) throw new Error('No tienes permisos');
    if (response.status === 404) throw new Error('Producto no encontrado');
    throw new Error(`Error: ${response.status}`);
  }

  const raw = await response.json();
  const normalized = {
    ...(raw as Record<string, unknown>),
    categories: normalizeCategories((raw as { categories?: unknown }).categories),
    ingredients: ((raw as { ingredients?: unknown }).ingredients ?? {}) as Record<string, number>,
  } as ProductDTO;
  return normalized;
}

export async function fetchAllProducts(
  accessToken: string,
  params: Omit<ProductQueryParams, 'page'> = {},
): Promise<ProductDTO[]> {
  const { size = 200, ...rest } = params;
  const collected: ProductDTO[] = [];
  let page = 0;
  let hasMore = true;

  while (hasMore) {
    const response = await fetchProductsPage(accessToken, { ...rest, page, size });
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

export async function createProduct(
  accessToken: string,
  data: ProductCreateDTO
): Promise<ProductDTO> {
  const response = await fetch(`${API_BASE_URL}/products`, {
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
      throw new Error('No tienes permisos de administrador para crear productos.');
    }
    if (response.status === 409) {
      throw new Error('Ya existe un producto con ese nombre.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para crear el producto.');
    }
    throw new Error(`Error al crear producto: ${response.status}`);
  }

  return response.json();
}

export async function updateProduct(
  accessToken: string,
  id: number,
  data: ProductUpdateDTO
): Promise<ProductDTO> {
  const response = await fetch(`${API_BASE_URL}/products/${id}`, {
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
      throw new Error('No tienes permisos de administrador para actualizar productos.');
    }
    if (response.status === 404) {
      throw new Error('El producto no existe.');
    }
    if (response.status === 409) {
      throw new Error('Ya existe un producto con ese nombre.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para actualizar el producto.');
    }
    throw new Error(`Error al actualizar producto: ${response.status}`);
  }

  return response.json();
}

export async function updateProductStock(
  accessToken: string,
  id: number,
  data: ProductStockDTO
): Promise<ProductDTO> {
  const response = await fetch(`${API_BASE_URL}/products/${id}/stock`, {
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
      throw new Error('No tienes permisos de administrador para actualizar el stock.');
    }
    if (response.status === 404) {
      throw new Error('El producto no existe.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para actualizar el stock.');
    }
    throw new Error(`Error al actualizar stock: ${response.status}`);
  }

  return response.json();
}

export async function deleteProduct(accessToken: string, id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/products/${id}`, {
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
      throw new Error('No tienes permisos de administrador para eliminar productos.');
    }
    if (response.status === 404) {
      throw new Error('El producto no existe.');
    }
    if (response.status === 409) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || 'No se puede eliminar este producto porque está siendo utilizado.');
    }
    throw new Error(`Error al eliminar producto: ${response.status}`);
  }
}