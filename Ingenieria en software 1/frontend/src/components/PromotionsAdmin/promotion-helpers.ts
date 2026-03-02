import type { ProductDTO } from "@/lib/api/products"
import type { Combo } from "@/models/Combo"

type SelectableItem = (ProductDTO | Combo) & { isCombo: boolean }


export function createFindItemById(products: ProductDTO[], combos: Combo[]) {
  const allItems: SelectableItem[] = [
    ...products.map((p) => ({ ...p, isCombo: false })),
    ...combos.map((c) => ({ ...c, isCombo: true })),
  ]

  return (itemId: number | undefined): SelectableItem | null => {
    if (!itemId) return null
    return allItems.find((i) => i.id === itemId) || null
  }
}

export function getAvailableCategories(products: ProductDTO[], combos: Combo[]): string[] {
  const categories = new Set<string>()
  products.forEach((p) => p.categories?.forEach((c) => categories.add(c)))
  combos.forEach((c) => c.categories?.forEach((cat) => categories.add(cat)))
  return Array.from(categories).sort()
}

export function getAvailableTypes(products: ProductDTO[], combos: Combo[]): string[] {
  const types = new Set<string>()
  products.forEach((p) => p.type && types.add(p.type))
  combos.forEach((c) => c.types?.forEach((t) => types.add(t)))
  return Array.from(types).sort()
}

