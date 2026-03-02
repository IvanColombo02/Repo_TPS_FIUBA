export const PRODUCTS_PER_PAGE = 6
export const COMBOS_PER_PAGE = 6
export const DEFAULT_PAGE_SIZE = PRODUCTS_PER_PAGE + COMBOS_PER_PAGE

export interface CombinedPagePlan {
  productStart: number
  productCount: number
  comboStart: number
  comboCount: number
}

export function computePagePlan(totalProducts: number, totalCombos: number, pageIndex: number): CombinedPagePlan {
  if (totalProducts <= 0 && totalCombos <= 0) {
    return { productStart: 0, productCount: 0, comboStart: 0, comboCount: 0 }
  }

  const combosStart = pageIndex * COMBOS_PER_PAGE
  const combosRemaining = Math.max(totalCombos - combosStart, 0)
  const comboCount = Math.min(COMBOS_PER_PAGE, combosRemaining)

  let productStart = 0
  for (let page = 0; page < pageIndex; page++) {
    const prevCombosStart = page * COMBOS_PER_PAGE
    const prevCombosRemaining = Math.max(totalCombos - prevCombosStart, 0)
    const prevComboCount = Math.min(COMBOS_PER_PAGE, prevCombosRemaining)
    const productSlots = DEFAULT_PAGE_SIZE - prevComboCount
    productStart += productSlots
  }

  const productRemaining = Math.max(totalProducts - productStart, 0)
  const productSlotsThisPage = DEFAULT_PAGE_SIZE - comboCount
  const productCount = Math.min(productSlotsThisPage, productRemaining)

  return {
    productStart,
    productCount,
    comboStart: combosStart,
    comboCount,
  }
}

