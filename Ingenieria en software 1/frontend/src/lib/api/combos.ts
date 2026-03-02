import { Combo, ComboCreateForm, ComboStockForm } from '@/models/Combo';
import { BASE_API_URL } from "../../config/app-query-client";
import { buildQueryString, PaginatedResponse, toPaginatedResponse } from './helpers';

const API_BASE_URL = BASE_API_URL;

interface ProductWithQuantity {
  id: number;
  name: string;
  price?: number;
  quantity: number;
}


const parseComboProducts = (productsData: unknown): ProductWithQuantity[] => {
  if (!productsData) return [];


  const products: ProductWithQuantity[] = [];

  for (const [key, quantity] of Object.entries(productsData as Record<string, unknown>)) {
    if (typeof key === 'string' && key.startsWith('ProductDTO[')) {
      const idMatch = key.match(/id=(\d+)/);
      const nameMatch = key.match(/name=([^,\]]+)/);
      const priceMatch = key.match(/price=([0-9]+(?:\.[0-9]+)?)/);

      if (idMatch && nameMatch) {
        products.push({
          id: parseInt(idMatch[1]),
          name: nameMatch[1],
          price: priceMatch ? parseFloat(priceMatch[1]) : undefined,
          quantity: quantity as number,
        });
      }
    } else if (typeof key === 'object' && key !== null) {
      const productDto = key as { id?: number; name?: string; price?: number };
      if (productDto && (productDto.id || productDto.name)) {
        products.push({
          id: productDto.id ?? 0,
          name: productDto.name ?? '',
          price: productDto.price !== undefined ? Number(productDto.price) : undefined,
          quantity: quantity as number,
        });
      }
    }
  }

  return products;
};
const normalizeCombo = (comboData: unknown): Combo => {
  const typedCombo = comboData as Record<string, unknown>;
  const products = parseComboProducts((typedCombo as { products?: unknown }).products);

  let computedPrice = Number(typedCombo.price ?? 0);

  if ((!computedPrice || Number.isNaN(computedPrice)) && products.length > 0) {
    const sum = products.reduce((acc, product) => {
      const productPrice = product.price ?? 0;
      return acc + productPrice * (product.quantity || 0);
    }, 0);

    if (sum > 0) {
      computedPrice = sum;
    }
  }

  // Normalizar el campo de imagen (puede venir como base64Image o base64image)
  const base64Image = typedCombo.base64Image || typedCombo.base64image || null;

  return {
    ...typedCombo,
    price: computedPrice,
    products,
    base64Image: base64Image as string | undefined,
  } as Combo;
};

export interface ComboQueryParams {
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

export const fetchCombosPage = async (
  accessToken: string,
  params: ComboQueryParams = {},
): Promise<PaginatedResponse<Combo>> => {
  const query = buildQueryString(params as Record<string, unknown>);
  const response = await fetch(`${API_BASE_URL}/combos${query}`, {
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
      throw new Error('No tienes permisos para acceder a los combos.');
    }
    throw new Error(`Error al obtener combos: ${response.statusText}`);
  }

  const raw = await response.json();
  const page = toPaginatedResponse<unknown>(raw);

  const normalizedContent = page.content.map((combo) => normalizeCombo(combo));

  return {
    ...page,
    content: normalizedContent,
  };
};

export interface ComboSimpleDTO {
  id: number
  name: string
  description: string
  price: number
  stock: number
  categories: string[]
  types: string[]
  base64Image?: string
}

export const fetchCombosSimplePage = async (
  accessToken: string,
  params: ComboQueryParams = {},
): Promise<PaginatedResponse<ComboSimpleDTO>> => {
  const query = buildQueryString(params as Record<string, unknown>);
  const response = await fetch(`${API_BASE_URL}/combos${query}`, {
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
      throw new Error('No tienes permisos para acceder a los combos.');
    }
    throw new Error(`Error al obtener combos: ${response.statusText}`);
  }

  const raw = await response.json();
  const page = toPaginatedResponse<unknown>(raw);
  const normalizedContent = page.content.map((combo) => {
    const typedCombo = combo as Record<string, unknown>;
    const base64Image = typedCombo.base64Image || typedCombo.base64image || null;
    return {
      ...(typedCombo as Record<string, unknown>),
      base64Image: base64Image as string | undefined,
    } as ComboSimpleDTO;
  });

  return {
    ...page,
    content: normalizedContent,
  };
}

export const fetchComboById = async (accessToken: string, id: number): Promise<Combo> => {
  const response = await fetch(`${API_BASE_URL}/combos/${id}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
  });
  if (!response.ok) {
    if (response.status === 404) throw new Error('Combo no encontrado');
    throw new Error(`Error: ${response.status}`);
  }
  const raw = await response.json();
  return normalizeCombo(raw);
}

export const fetchCombos = async (
  accessToken: string,
  params: Omit<ComboQueryParams, 'page'> = {},
): Promise<Combo[]> => {
  const { size = 100, ...rest } = params;
  const collected: Combo[] = [];
  let page = 0;
  let hasMore = true;

  while (hasMore) {
    const response = await fetchCombosPage(accessToken, { ...rest, page, size });
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
};

export const createCombo = async (accessToken: string, comboData: ComboCreateForm): Promise<Combo> => {

  const transformedData = {
    name: comboData.name,
    description: comboData.description,
    price: comboData.price,
    categories: comboData.categories || [],
    types: comboData.types || [],
    productsIds: Object.fromEntries(
      Object.entries(comboData.productsIds).map(([key, value]) => [parseInt(key), value])
    ),
    // Asegurar que base64Image siempre se incluya (incluso si está vacío, ya que es requerido en el backend)
    base64Image: comboData.base64Image || ""
  };

  const response = await fetch(`${API_BASE_URL}/combos`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(transformedData),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para crear combos.');
    }
    if (response.status === 409) {
      throw new Error('Ya existe un combo con ese nombre.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para crear el combo.');
    }
    const errorData = await response.json().catch(() => null);
    throw new Error(errorData?.message || `Error al crear combo: ${response.statusText}`);
  }

  const rawCombo = await response.json();
  return normalizeCombo(rawCombo);
};

export interface ComboUpdateData {
  name?: string;
  description?: string;
  price?: number;
  categories?: string[];
  types?: string[];
  base64Image?: string;
  addProducts?: Record<string, number>;
  deleteProducts?: string[];
}

export const updateCombo = async (accessToken: string, id: number, comboData: ComboUpdateData): Promise<Combo> => {

  const transformedData: Record<string, unknown> = {
    name: comboData.name,
    description: comboData.description,
    price: comboData.price,
    categories: comboData.categories,
    types: comboData.types,
  };

  // Solo incluir base64Image si está presente (permite eliminar la imagen enviando cadena vacía)
  if (comboData.base64Image !== undefined) {
    transformedData.base64Image = comboData.base64Image;
  }

  if (comboData.addProducts) {
    transformedData.addProducts = Object.fromEntries(
      Object.entries(comboData.addProducts).map(([key, value]) => [parseInt(key), value])
    );
  }

  if (comboData.deleteProducts && Array.isArray(comboData.deleteProducts)) {
    transformedData.deleteProducts = comboData.deleteProducts.map((id: string) => parseInt(id));
  }

  const response = await fetch(`${API_BASE_URL}/combos/${id}`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(transformedData),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para actualizar combos.');
    }
    if (response.status === 404) {
      throw new Error('El combo no existe.');
    }
    if (response.status === 409) {
      throw new Error('Ya existe un combo con ese nombre.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para actualizar el combo.');
    }
    const errorData = await response.json().catch(() => null);
    throw new Error(errorData?.message || `Error al actualizar combo: ${response.statusText}`);
  }

  const rawCombo = await response.json();
  return normalizeCombo(rawCombo);
};

export const deleteCombo = async (accessToken: string, id: number): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/combos/${id}`, {
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
      throw new Error('No tienes permisos de administrador para eliminar combos.');
    }
    if (response.status === 404) {
      throw new Error('El combo no existe.');
    }
    const errorData = await response.json().catch(() => null);
    throw new Error(errorData?.message || `Error al eliminar combo: ${response.statusText}`);
  }
};

export const updateComboStock = async (accessToken: string, id: number, stockData: ComboStockForm): Promise<Combo> => {
  const response = await fetch(`${API_BASE_URL}/combos/${id}/stock`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(stockData),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Inicia sesión nuevamente.');
    }
    if (response.status === 403) {
      throw new Error('No tienes permisos de administrador para actualizar el stock de combos.');
    }
    if (response.status === 404) {
      throw new Error('El combo no existe.');
    }
    if (response.status === 422) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Datos inválidos para actualizar el stock del combo.');
    }
    const errorData = await response.json().catch(() => null);
    throw new Error(errorData?.message || `Error al actualizar stock del combo: ${response.statusText}`);
  }

  const rawCombo = await response.json();
  return normalizeCombo(rawCombo);
};