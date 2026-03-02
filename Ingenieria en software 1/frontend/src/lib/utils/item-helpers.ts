import type { ProductDTO, ProductSimpleDTO } from "@/lib/api/products"
import type { Combo } from "@/models/Combo"
import type { ComboSimpleDTO } from "@/lib/api/combos"
import type { MenuItem, Ingredient, FoodCategory } from "@/lib/types"

const DEFAULT_COMBO_PREPARATION_TIME = 20

export function parseIngredients(ingredientsData: Record<string, number>): Ingredient[] {
  if (!ingredientsData) return []

  const ingredients: Ingredient[] = []

  if (Array.isArray(ingredientsData)) {
    for (const item of ingredientsData as unknown as Array<Record<string, unknown>>) {
      const id = (item.id ?? item["id"]) as string | number
      const name = (item.name ?? item["name"]) as string
      const quantity = (item.quantity ?? item["quantity"]) as number
      if (id && name) {
        ingredients.push({
          id: String(id),
          name: String(name),
          quantity: Number(quantity) || 0,
        })
      }
    }
    return ingredients
  }

  for (const [key, quantity] of Object.entries(ingredientsData)) {
    if (typeof key === "string" && key.startsWith("IngredientDTO[")) {
      const idMatch = key.match(/id=(\d+)/)
      const nameMatch = key.match(/name=([^,\]]+)/)

      if (idMatch && nameMatch) {
        ingredients.push({
          id: idMatch[1],
          name: nameMatch[1],
          quantity,
        })
      }
    }
  }
  return ingredients
}

function calculateComboPreparationTime(
  combo: Partial<Combo>,
  availableProducts: (ProductDTO | ProductSimpleDTO)[] = []
): number {
  if (!combo.products || combo.products.length === 0) {
    return DEFAULT_COMBO_PREPARATION_TIME
  }

  let maxTime = 0

  for (const comboProduct of combo.products) {
    const product = availableProducts.find((p) => p.id === comboProduct.id)
    if (product && product.estimatedTime) {
      maxTime = Math.max(maxTime, product.estimatedTime)
    }
  }
  return maxTime > 0 ? maxTime : DEFAULT_COMBO_PREPARATION_TIME
}

export function mapProductToMenuItem(product: ProductDTO | ProductSimpleDTO): MenuItem {
  const categories = (product.categories || []) as FoodCategory[]
  return {
    id: product.id,
    name: product.name,
    description: product.description,
    price: product.price,
    category: categories[0] || "",
    categories: categories,
    type: product.type,
    image: product.base64Image || "/placeholder.svg",
    available: product.stock > 0,
    preparationTime: product.estimatedTime,
    ingredients: parseIngredients((product as Partial<ProductDTO>).ingredients || {}),
  }
}

export function mapComboToMenuItem(combo: Combo | ComboSimpleDTO, availableProducts: (ProductDTO | ProductSimpleDTO)[] = []): MenuItem {
  const categories = (combo.categories || []) as FoodCategory[]
  const comboAsPartial = combo as Partial<Combo>
  return {
    id: `combo-${combo.id}`,
    name: combo.name,
    description: combo.description,
    price: combo.price,
    category: categories[0] || "",
    categories: categories,
    type: combo.types && combo.types[0],
    image: combo.base64Image || "/placeholder.svg",
    available: combo.stock > 0,
    preparationTime: calculateComboPreparationTime(comboAsPartial, availableProducts),
    isCombo: true,
    comboId: combo.id,
    comboProducts: (comboAsPartial.products || []).map(p => {

      const prod = p as { id: number; name?: string; productName?: string; quantity?: number; qty?: number }
      let name = prod.name || prod.productName || undefined
      if (!name) {
        const found = availableProducts.find(ap => ap.id === prod.id)
        if (found) name = found.name
      }
      return {
        id: prod.id,
        name: name || `Producto #${prod.id}`,
        quantity: prod.quantity || prod.qty || 1,
      }
    }),
  }
}

export function findItemById(itemId: number, items: MenuItem[]): MenuItem | null {
  const product = items.find((p) => p.id === itemId)
  if (product) return product

  const combo = items.find((c) => {
    const comboId = typeof c.id === "string" ? parseInt(c.id.replace("combo-", "")) : c.id
    return comboId === itemId
  })
  if (combo) return combo

  return null
}

