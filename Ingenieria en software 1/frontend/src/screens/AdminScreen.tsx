import { ArrowLeft, Settings, ChefHat, Package, Layers, Users, Tag } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Link } from "wouter"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import { IngredientsAdmin } from "@/components/IngredientsAdmin/IngredientsAdmin"
import { ProductsAdmin } from "@/components/ProductsAdmin/ProductsAdmin"
import { CombosAdmin } from "@/components/CombosAdmin/CombosAdmin"
import { PromotionsAdmin } from "@/components/PromotionsAdmin/PromotionsAdmin"
import { useRef, useState } from "react"
import RolManagment from "@/components/RolManagment"

export const AdminScreen = () => {
  const [activeTab, setActiveTab] = useState("ingredientes");
  const refreshProductsRef = useRef<((ingredientId?: number) => Promise<number[]>) | null>(null);
  const refreshCombosRef = useRef<((productIds?: number[]) => void) | null>(null);

  const handleIngredientStockChange = async (ingredientId?: number) => {
    if (refreshProductsRef.current) {
      const updatedProductIds = await refreshProductsRef.current(ingredientId);
      if (refreshCombosRef.current && updatedProductIds.length > 0) {
        await refreshCombosRef.current(updatedProductIds);
      }
    }
  };

  return (
    <div className="min-h-screen bg-background dark">
      <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-3">
            <Settings className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold text-foreground">Administración</h1>
          </div>
          <Link href="/">
            <Button variant="outline" className="flex items-center gap-2 text-foreground">
              <ArrowLeft className="h-5 w-5" />
              <span className="hidden sm:inline">Volver al Menú</span>
            </Button>
          </Link>
        </div>
      </header>

      <main className="container mx-auto px-4 py-6">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <TabsList className="grid !w-full grid-cols-5">
            <TabsTrigger value="ingredientes" className="flex items-center gap-2">
              <ChefHat className="h-4 w-4" />
              Ingredientes
            </TabsTrigger>
            <TabsTrigger value="productos" className="flex items-center gap-2">
              <Package className="h-4 w-4" />
              Productos
            </TabsTrigger>
            <TabsTrigger value="combos" className="flex items-center gap-2">
              <Layers className="h-4 w-4" />
              Combos
            </TabsTrigger>
            <TabsTrigger value="promociones" className="flex items-center gap-2">
              <Tag className="h-4 w-4" />
              Promociones
            </TabsTrigger>
            <TabsTrigger value="roles" className="flex items-center gap-2">
              <Users className="h-4 w-4" />
              Roles
            </TabsTrigger>
          </TabsList>

          <TabsContent value="ingredientes" className="space-y-4">
            <IngredientsAdmin onIngredientStockChange={handleIngredientStockChange} />
          </TabsContent>

          <TabsContent value="productos" className="space-y-4" forceMount hidden={activeTab !== "productos"}>
            <ProductsAdmin
              onRefreshRef={refreshProductsRef}
              onProductUpdate={async (productIds?: number[]) => {
                if (refreshCombosRef.current && productIds && productIds.length > 0) {
                  await refreshCombosRef.current(productIds);
                }
              }}
            />
          </TabsContent>

          <TabsContent value="combos" className="space-y-4" forceMount hidden={activeTab !== "combos"}>
            <CombosAdmin onRefreshRef={refreshCombosRef} />
          </TabsContent>

          <TabsContent value="promociones" className="space-y-4" forceMount hidden={activeTab !== "promociones"}>
            <PromotionsAdmin />
          </TabsContent>

          <TabsContent value="roles">
            <RolManagment />
          </TabsContent>

        </Tabs>
      </main>
    </div>
  );
};
