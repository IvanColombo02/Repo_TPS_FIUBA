"use client"

import { useState, useEffect } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog"
import { Order, PaymentMethod } from "@/lib/types"
import { Button } from "./ui/button"
import { CheckCircle2, Receipt } from "lucide-react"
import { Separator } from "@radix-ui/react-select"
import { format } from "date-fns"
import { es } from "date-fns/locale"

interface PaymentDetailsDialogProps {
    order: Order | null
    open: boolean
    onOpenChange: (open: boolean) => void
}

interface PaymentContentProps {
    timeLeft: number
    order: Order
}

const MercadoPagoContent = () =>
(
    <div className="flex flex-col items-center space-y-4 py-4">
        <div className="bg-white p-4 rounded-lg shadow-sm">
            <img src={`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent("https://campusgrado.fi.uba.ar/course/view.php?id=1327")}`} alt="QR code for digital wallet" />
        </div>
        <p className="text-center text-sm text-muted-foreground">Escanea el código QR con tu dispositivo móvil para completar el pago.</p>
    </div>
)


const CashContent = ({ timeLeft }: { timeLeft: number }) => (
    <div className="flex flex-col items-center space-y-6 py-8">
        <div className="h-40 w-40 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center">

            <img src="/Payment-cash.jpg" alt="payment cash" />
        </div>
        <div className="text-center space-y-2">
            <h3 className="text-xl font-semibold">Pedido Registrado Correctamente</h3>
            <p className="text-lg text-foreground font-medium">Acercarce a caja para pagar </p>

            <p className="text-sm text-muted-foreground">Cerrando automaticamente en {timeLeft} segundos     </p>
        </div>
    </div>
)

const CardContent = ({ order }: { order: Order }) => (

    <div className="space-y-6">
        <div className="flex flex-col items-center space-y-2">
            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                <CheckCircle2 className="h-6 w-6 text-primary" />
            </div>
            <h3 className="font-semibold text-lg"> Pago con tarjeta </h3>
        </div>

        <div className="bg-muted/50 p-4 rounded-lg space-y-4 border border-border dashed">
            <div className="flex justify-between items-center">
                <span className="font-mono text-sm text-muted-foreground"> TICKET #{order.id}</span>
                <Receipt className="h-4 w-4 text-muted-foreground" />
            </div>
            <Separator className="bg-border/50" />

            <div className="space-y-2">
                <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">fecha</span>

                    <span> {format(order.createdAt, "PP p", { locale: es })}</span>
                </div>
                <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Estado</span>
                    <span className="capitalize">{order.status.toLowerCase()}</span>
                </div>
                <Separator className="bg-border/50" />
                <div className="space-y-2">
                    {order.items.map((item, index) => (
                        <div key={index} className="flex justify-between text-sm">
                            <span className="truncate max-w-[200px]">{item.quantity}x {item.itemName}</span>
                            <span>${(item.itemPrice * item.quantity).toFixed(2)} ARS</span>
                        </div>
                    ))}
                </div>
                <Separator className="bg-border/50" />
                <div className="flex justify-between font-semibold">
                    <span>Total</span>
                    <span>${order.totalPrice.toFixed(2)} ARS</span>
                </div>
            </div>
        </div>

    </div>
)



const PAYMENT_CONTENT_MAP: Record<PaymentMethod, React.FC<PaymentContentProps>> = {
    CASH: ({ timeLeft }) => <CashContent timeLeft={timeLeft} />,
    MP: MercadoPagoContent,
    CARD: ({ order }) => <CardContent order={order} />
}

export function PaymentDetailsDialog({ order, open, onOpenChange }: PaymentDetailsDialogProps) {

    const [timeLeft, setTimeLeft] = useState(10)

    useEffect(() => {
        if (open && order?.paymentMethod == "CASH") {
            setTimeLeft(10)
            const timer = setInterval(() => {
                setTimeLeft((prev) => {
                    if (prev <= 1) {
                        clearInterval(timer)
                        onOpenChange(false)
                        return 0
                    }
                    return prev - 1
                })
            }, 1000)

            return () => clearInterval(timer)
        }
    }, [open, order, onOpenChange])

    if (!order) return null

    const ContentComponent = PAYMENT_CONTENT_MAP[order.paymentMethod]

    const getDialogTitle = (method: PaymentMethod) => {
        switch (method) {
            case "MP":
                return "Pago con Mercado Pago"
            case "CARD":
                return "Comprobante de Pago"
            default:
                return "Detalles del Pago"
        }
    }


    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md">

                <DialogHeader>
                    <DialogTitle className="text-center">{getDialogTitle(order.paymentMethod)}</DialogTitle>
                    <DialogDescription className="text-center text-muted-foreground text-md">
                        Orden #{order.id}
                    </DialogDescription>
                </DialogHeader>


                <ContentComponent timeLeft={timeLeft} order={order} />

                {order.paymentMethod !== "CASH" && (
                    <DialogFooter className="sm:justify-center">
                        <Button onClick={() => onOpenChange(false)} variant="outline" className="w-full sm:w-auto">Cerrar</Button>
                    </DialogFooter>
                )}

            </DialogContent>
        </Dialog>
    )
}