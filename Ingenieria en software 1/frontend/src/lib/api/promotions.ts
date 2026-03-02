import { buildQueryString, PaginatedResponse, toPaginatedResponse } from "./helpers"

import { BASE_API_URL } from "../../config/app-query-client";

const API_BASE_URL = BASE_API_URL

export interface PromotionDTO {
  id: number
  name: string
  description: string
  fromDate: string
  toDate: string
  base64Image: string
  expression: string // JSON string
  priority: number
}

export interface PromotionCreateDTO {
  name: string
  description: string
  fromDate: string
  toDate: string
  base64Image: string
  expression: string // JSON string
  priority?: number
}

export interface PromotionUpdateDTO {
  name?: string
  description?: string
  fromDate?: string
  toDate?: string
  base64Image?: string
  expression?: string
}

export interface PromotionPriorityUpdateDTO {
  orderedPromotionIds: number[]
}

export interface PromotionQueryParams extends Record<string, unknown> {
  page?: number
  size?: number
  name?: string
  sort?: string | string[]
}

async function fetchWithAuth(url: string, accessToken: string, options: RequestInit = {}) {
  if (!accessToken) {
    throw new Error("No access token available")
  }

  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
      ...options.headers,
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error("No autorizado. Inicia sesión nuevamente.")
    }
    if (response.status === 403) {
      throw new Error("No tienes permisos para realizar esta acción.")
    }
    const errorText = await response.text()
    throw new Error(`Error: ${response.status} ${response.statusText} - ${errorText}`)
  }

  return response
}

export async function fetchPromotionsPage(
  accessToken: string,
  params: PromotionQueryParams = {}
): Promise<PaginatedResponse<PromotionDTO>> {
  const baseParams: PromotionQueryParams = {
    ...params,
  }

  if (!baseParams.sort) {
    baseParams.sort = ["priority,asc", "id,asc"]
  }

  const queryString = buildQueryString(baseParams)
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions${queryString}`, accessToken)
  const data = await response.json()
  return toPaginatedResponse<PromotionDTO>(data)
}

export async function fetchAllPromotions(
  accessToken: string,
  params: PromotionQueryParams = {}
): Promise<PromotionDTO[]> {
  const queryParams: PromotionQueryParams = {
    page: params.page ?? 0,
    size: params.size ?? 1000,
    sort: params.sort ?? ["priority,asc", "id,asc"],
    ...params,
  }

  const queryString = buildQueryString(queryParams)
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions${queryString}`, accessToken)
  const data = await response.json()
  return toPaginatedResponse<PromotionDTO>(data).content
}

export async function fetchPromotionById(accessToken: string, id: number): Promise<PromotionDTO> {
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions/${id}`, accessToken)
  return response.json()
}

export async function fetchActivePromotions(accessToken: string): Promise<PromotionDTO[]> {
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions/active`, accessToken)
  return response.json()
}

export async function createPromotion(
  accessToken: string,
  data: PromotionCreateDTO
): Promise<PromotionDTO> {
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions`, accessToken, {
    method: "POST",
    body: JSON.stringify(data),
  })
  return response.json()
}

export async function updatePromotion(
  accessToken: string,
  id: number,
  data: PromotionUpdateDTO
): Promise<PromotionDTO> {
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions/${id}`, accessToken, {
    method: "PATCH",
    body: JSON.stringify(data),
  })
  return response.json()
}

export async function deletePromotion(accessToken: string, id: number): Promise<void> {
  await fetchWithAuth(`${API_BASE_URL}/promotions/${id}`, accessToken, {
    method: "DELETE",
  })
}

export async function updatePromotionPriorities(
  accessToken: string,
  data: PromotionPriorityUpdateDTO
): Promise<PromotionDTO[]> {
  const response = await fetchWithAuth(`${API_BASE_URL}/promotions/priorities`, accessToken, {
    method: "PUT",
    body: JSON.stringify(data),
  })
  return response.json()
}


