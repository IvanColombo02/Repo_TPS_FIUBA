"use client"

import { User, ChevronDown } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Link } from "wouter"
import { useToken } from "@/services/TokenContext"
import { getJWTRole, getRoleName } from "@/services/UserServices"

interface UserProfileDropdownProps {
  userName: string
  userRole?: string | string[]
}

export function UserProfileDropdown({ userName, userRole }: UserProfileDropdownProps) {
  const [tokenState, setTokenState] = useToken()

  let role: string | string[] | undefined = userRole
  if (!role && tokenState.state === "LOGGED_IN") {
    role = getJWTRole(tokenState.tokens.accessToken) || undefined
  }

  const roleName = getRoleName(role || 'ROLE_USER')

  const handleLogout = () => {
    setTokenState({ state: "LOGGED_OUT" })
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="flex items-center gap-2 text-foreground hover:text-foreground">
          <User className="h-5 w-5" />
          <span className="hidden sm:inline">Mi Perfil</span>
          <ChevronDown className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel className="font-normal">
          <div className="flex flex-col space-y-1">
            <p className="text-sm font-medium leading-none text-foreground">@{userName || "Usuario"}</p>
            <p className="text-xs leading-none text-muted-foreground">{roleName}</p>
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem asChild>
          <Link href="/profile" className="cursor-pointer">
            Ver perfil
          </Link>
        </DropdownMenuItem>
        <DropdownMenuItem onClick={handleLogout} className="cursor-pointer">
          Cerrar sesión
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
