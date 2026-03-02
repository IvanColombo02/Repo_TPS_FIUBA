export interface PaginatedResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

interface RawPaginatedResponse<T> extends Partial<PaginatedResponse<T>> {
  content?: T[]
}

export function toPaginatedResponse<T>(raw: unknown): PaginatedResponse<T> {
  if (Array.isArray(raw)) {
    const content = raw as T[]
    return {
      content,
      totalPages: 1,
      totalElements: content.length,
      size: content.length,
      number: 0,
      first: true,
      last: true,
      empty: content.length === 0,
    }
  }

  if (raw && typeof raw === "object" && "content" in raw) {
    const page = raw as RawPaginatedResponse<T>

    if (!Array.isArray(page.content)) {
      throw new Error("Respuesta inesperada del servidor: el campo 'content' debe ser un array.")
    }

    const content = page.content
    return {
      content,
      totalPages: page.totalPages ?? 1,
      totalElements: page.totalElements ?? content.length,
      size: page.size ?? content.length,
      number: page.number ?? 0,
      first: page.first ?? (page.number ?? 0) === 0,
      last: page.last ?? true,
      empty: page.empty ?? content.length === 0,
    }
  }

  throw new Error(
    "Respuesta inesperada del servidor: se esperaba un objeto paginado o un array de resultados.",
  )
}

export function buildQueryString(params?: Record<string, unknown>): string {
  if (!params) {
    return ""
  }

  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null) {
      return
    }

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item !== undefined && item !== null) {
          searchParams.append(key, String(item))
        }
      })
      return
    }

    searchParams.append(key, String(value))
  })

  const query = searchParams.toString()
  return query ? `?${query}` : ""
}
