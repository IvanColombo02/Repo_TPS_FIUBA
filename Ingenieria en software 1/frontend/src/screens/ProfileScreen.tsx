import { useEffect, useState } from "react"
import { UtensilsCrossed, ArrowLeft, User, Mail, Calendar, UsersIcon, Home, Edit, Upload, X, Lock, AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Link } from "wouter"
import { useToken } from "@/services/TokenContext"
import { fetchUserProfile, updateUserProfileBackend, UserProfile, getJWTRole, getRoleName, requestPasswordReset } from "@/services/UserServices"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { validateAndConvertImage } from "@/lib/utils/image-utils"
import { validateEmailDomain, validateMinLength } from "@/lib/utils/validation"

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

export const ProfileScreen = () => {
  const RESET_DIALOG_DURATION_SECONDS = 5
  const RESET_MESSAGES = {
    success: 'Te enviamos un correo con instrucciones para cambiar tu contraseña.',
    emailNotFound: 'Ese email no está registrado',
    genericError: 'No se pudo enviar el email de recuperación'
  } as const
  const [tokenState, setTokenState] = useToken()
  const [userData, setUserData] = useState<UserProfile | null>(null)
  const [userRoleText, setUserRoleText] = useState<string>("Estudiante")
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [editData, setEditData] = useState<Partial<UserProfile>>({})
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [imageError, setImageError] = useState<string | null>(null)
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({})
  const [imageDeleted, setImageDeleted] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [resetStatus, setResetStatus] = useState<"idle" | "loading" | "success" | "error">("idle")
  const [isResetDialogOpen, setIsResetDialogOpen] = useState(false)
  const [resetDialogSeconds, setResetDialogSeconds] = useState(5)
  const [resetDialogMessage, setResetDialogMessage] = useState<string>("")

  useEffect(() => {
    if (tokenState.state === "LOGGED_IN" && tokenState.tokens.accessToken) {
      setIsLoading(true)
      fetchUserProfile(tokenState.tokens.accessToken)
        .then(profile => {
          setUserData(profile)
          const role = getJWTRole(tokenState.tokens.accessToken)
          setUserRoleText(getRoleName(role))
        })
        .catch(error => {
          console.error('Error al cargar perfil:', error)
        })
        .finally(() => {
          setIsLoading(false)
        })
    }
  }, [tokenState])

  const handleLogout = () => {
    setTokenState({ state: "LOGGED_OUT" })
  }

  const handleEdit = () => {
    setValidationErrors({})
    setImageDeleted(false)  // Resetear flag de imagen eliminada
    if (userData) {
      setEditData({
        username: userData.username,
        email: userData.email,
        firstName: userData.firstName,
        lastName: userData.lastName,
        age: userData.age,
        gender: userData.gender,
        address: userData.address,
        base64Image: userData.base64Image
      })
      setImagePreview(userData.base64Image || null)
    }
    setIsEditDialogOpen(true)
  }

  const validateEditData = (): boolean => {
    const errors: Record<string, string> = {}

    if (!validateMinLength(editData.username || '', 3)) {
      errors.username = "El nombre de usuario debe tener al menos 3 caracteres"
    }

    if (!validateMinLength(editData.firstName || '', 2)) {
      errors.firstName = "El nombre debe tener al menos 2 caracteres"
    }

    if (!validateMinLength(editData.lastName || '', 2)) {
      errors.lastName = "El apellido debe tener al menos 2 caracteres"
    }

    if (!editData.email || !editData.email.includes('@')) {
      errors.email = "Ingresa un email válido"
    } else if (!validateEmailDomain(editData.email)) {
      errors.email = "El email debe ser @fi.uba.ar"
    }

    if (!editData.age || editData.age < 10 || editData.age > 120) {
      errors.age = "La edad debe estar entre 10 y 120 años"
    }

    if (!validateMinLength(editData.address || '', 3)) {
      errors.address = "La dirección debe tener al menos 3 caracteres"
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSaveEdit = async () => {
    if (!validateEditData()) {
      return
    }

    if (tokenState.state !== "LOGGED_IN") {
      return
    }

    try {
      setIsLoading(true)

      let photoToSave: string | null | undefined
      let shouldUpdateImage = false

      if (imageDeleted) {
        photoToSave = null
        shouldUpdateImage = true
      } else if (imagePreview) {
        photoToSave = imagePreview
        shouldUpdateImage = true
      } else {
        photoToSave = undefined
        shouldUpdateImage = false
      }

      const updateData: Partial<{
        username: string;
        email: string;
        firstName: string;
        lastName: string;
        age: number;
        gender: "Masculino" | "Femenino" | "Prefiero no decir";
        address: string;
        base64Image: string | null;
      }> = {
        username: editData.username?.trim(),
        email: editData.email?.trim(),
        firstName: editData.firstName?.trim(),
        lastName: editData.lastName?.trim(),
        age: editData.age,
        gender: editData.gender as "Masculino" | "Femenino" | "Prefiero no decir",
        address: editData.address?.trim(),
      }

      if (shouldUpdateImage) {
        updateData.base64Image = photoToSave
      }

      const updatedProfile = await updateUserProfileBackend(tokenState.tokens.accessToken, updateData)
      setUserData(updatedProfile)
      setIsEditDialogOpen(false)

      setImagePreview(null)
      setImageDeleted(false)
    } catch (error) {
      console.error('Error al actualizar perfil:', error)
    } finally {
      setIsLoading(false)
    }
  }



  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    const result = await validateAndConvertImage(file)

    if (!result.valid) {
      setImageError(result.error || 'Error al procesar la imagen')
      return
    }

    setImageError(null)
    setImagePreview(result.base64 || null)
    setImageDeleted(false)
  }

  const handleRemoveImage = () => {
    setImagePreview(null)
    setImageDeleted(true)
  }

  const openResetDialog = (message: string, seconds = RESET_DIALOG_DURATION_SECONDS) => {
    setResetDialogMessage(message)
    setResetDialogSeconds(seconds)
    setIsResetDialogOpen(true)
  }

  const handleSendResetEmail = async () => {
    if (!userData?.email) return
    try {
      setResetStatus("loading")
      await requestPasswordReset(userData.email)
      setResetStatus("success")
      openResetDialog(RESET_MESSAGES.success)
    } catch (err: unknown) {
      setResetStatus("error")
      const msg = (err as Error)?.message === 'EMAIL_NOT_FOUND' ? RESET_MESSAGES.emailNotFound : RESET_MESSAGES.genericError
      openResetDialog(msg)
    }
  }

  useEffect(() => {
    if (!isResetDialogOpen) return
    if (resetDialogSeconds <= 0) {
      setIsResetDialogOpen(false)
      setResetStatus("idle")
      return
    }
    const t = setTimeout(() => setResetDialogSeconds(s => s - 1), 1000)
    return () => clearTimeout(t)
  }, [isResetDialogOpen, resetDialogSeconds])

  if (isLoading || !userData) {
    return (
      <div className="min-h-screen bg-background dark flex items-center justify-center">
        <div className="text-center">
          <p className="text-muted-foreground">Cargando perfil...</p>
        </div>
      </div>
    )
  }

  const fullName = `${userData.firstName || ''} ${userData.lastName || ''}`.trim() || "Usuario del Sistema"

  return (
    <div className="min-h-screen bg-background dark">

      <header className="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-3">
            <UtensilsCrossed className="h-8 w-8 text-primary" />
            <h1 className="text-2xl font-bold text-foreground">Comedor FIUBA</h1>
          </div>
          <Link href="/">
            <Button variant="ghost" className="flex items-center gap-2 text-foreground hover:text-foreground">
              <ArrowLeft className="h-5 w-5" />
              <span className="hidden sm:inline">Volver al menú</span>
            </Button>
          </Link>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Mi Perfil</h2>
          <p className="text-muted-foreground">Gestiona tu información personal y preferencias</p>
        </div>

        <div className="grid gap-6">
          <Card className="border-border shadow-lg">
            <CardHeader className="pb-4">
              <div className="relative">
                <div className="flex items-center gap-4">
                  <div className="h-20 w-20 rounded-full bg-primary/10 flex items-center justify-center overflow-hidden">
                    {userData.base64Image ? (
                      <img
                        src={userData.base64Image}
                        alt="Perfil"
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <User className="h-10 w-10 text-primary" />
                    )}
                  </div>
                  <div className="flex-1">
                    <CardTitle className="text-2xl text-foreground">@{userData.username}</CardTitle>
                    <CardDescription className="text-base mt-1">{userData.email}</CardDescription>
                    <Badge variant="secondary" className="mt-2">
                      {userRoleText}
                    </Badge>
                  </div>
                </div>
              </div>
            </CardHeader>
          </Card>

          <Card className="border-border shadow-lg">
            <CardHeader>
              <CardTitle className="text-xl text-foreground">Información Personal</CardTitle>
              <CardDescription>Tus datos de contacto y perfil</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-4 p-4 bg-muted/20 rounded-lg">
                <User className="h-5 w-5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm text-muted-foreground">Nombre completo</p>
                  <p className="text-foreground font-medium">{fullName}</p>
                </div>
              </div>

              <div className="flex items-center gap-4 p-4 bg-muted/20 rounded-lg">
                <Mail className="h-5 w-5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm text-muted-foreground">Email</p>
                  <p className="text-foreground font-medium">{userData.email}</p>
                </div>
              </div>

              <div className="flex items-center gap-4 p-4 bg-muted/20 rounded-lg">
                <User className="h-5 w-5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm text-muted-foreground">Nombre de Usuario</p>
                  <p className="text-foreground font-medium">@{userData.username}</p>
                </div>
              </div>

              <div className="flex items-center gap-4 p-4 bg-muted/20 rounded-lg">
                <Calendar className="h-5 w-5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm text-muted-foreground">Edad</p>
                  <p className="text-foreground font-medium">{userData.age} años</p>
                </div>
              </div>

              <div className="flex items-center gap-4 p-4 bg-muted/20 rounded-lg">
                <UsersIcon className="h-5 w-5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm text-muted-foreground">Género</p>
                  <p className="text-foreground font-medium">{userData.gender}</p>
                </div>
              </div>

              <div className="flex items-center gap-4 p-4 bg-muted/20 rounded-lg">
                <Home className="h-5 w-5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm text-muted-foreground">Ubicación</p>
                  <p className="text-foreground font-medium">{userData.address || "No especificada"}</p>
                </div>
              </div>
            </CardContent>
          </Card>


          <div className="flex flex-col sm:flex-row gap-4">
            <Button
              variant="outline"
              className="flex-1 text-foreground bg-transparent flex items-center gap-2"
              onClick={handleEdit}
            >
              <Edit className="h-4 w-4" />
              Editar Perfil
            </Button>
            <Button
              variant="outline"
              className="flex-1 text-foreground bg-transparent flex items-center gap-2"
              onClick={handleSendResetEmail}
              disabled={resetStatus === "loading"}
            >
              <Lock className="h-4 w-4" />
              {resetStatus === "loading" ? 'Enviando correo...' : 'Cambiar Contraseña'}
            </Button>
            <Button variant="destructive" className="flex-1" onClick={handleLogout}>
              Cerrar Sesión
            </Button>
          </div>
        </div>
      </main>

      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Editar Perfil</DialogTitle>
            <DialogDescription>
              Actualiza tu información personal aquí. Los cambios se guardarán localmente.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-6 py-4">
            <div className="flex flex-col items-center space-y-4">
              <div className="relative">
                <div className="w-24 h-24 rounded-full border-4 border-border overflow-hidden bg-muted flex items-center justify-center">
                  {imagePreview ? (
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <User className="w-12 h-12 text-muted-foreground" />
                  )}
                </div>
                {imagePreview && (
                  <button
                    onClick={handleRemoveImage}
                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1.5 hover:bg-red-600 transition-colors"
                    type="button"
                  >
                    <X className="w-4 h-4" />
                  </button>
                )}
              </div>
              <Label
                htmlFor="edit-profileImage"
                className="cursor-pointer inline-flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors"
              >
                <Upload className="w-4 h-4" />
                Cambiar foto
              </Label>
              <Input
                id="edit-profileImage"
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleImageChange}
              />
              {imageError && (
                <p className="text-sm text-red-500">{imageError}</p>
              )}
              <p className="text-xs text-muted-foreground">Máximo 2MB</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-username">Nombre de Usuario</Label>
                <Input
                  id="edit-username"
                  value={editData.username || ''}
                  onChange={(e) => setEditData({ ...editData, username: e.target.value })}
                  className={validationErrors.username ? 'border-red-500' : ''}
                />
                {validationErrors.username && (
                  <p className="text-sm text-red-500 flex items-center gap-1">
                    <AlertCircle className="w-3 h-3" />
                    {validationErrors.username}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-email">Email</Label>
                <Input
                  id="edit-email"
                  type="email"
                  value={editData.email || ''}
                  onChange={(e) => setEditData({ ...editData, email: e.target.value })}
                  className={validationErrors.email ? 'border-red-500' : ''}
                />
                {validationErrors.email && (
                  <p className="text-sm text-red-500 flex items-center gap-1">
                    <AlertCircle className="w-3 h-3" />
                    {validationErrors.email}
                  </p>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-firstName">Nombre</Label>
                <Input
                  id="edit-firstName"
                  value={editData.firstName || ''}
                  onChange={(e) => setEditData({ ...editData, firstName: e.target.value })}
                  className={validationErrors.firstName ? 'border-red-500' : ''}
                />
                {validationErrors.firstName && (
                  <p className="text-sm text-red-500 flex items-center gap-1">
                    <AlertCircle className="w-3 h-3" />
                    {validationErrors.firstName}
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-lastName">Apellido</Label>
                <Input
                  id="edit-lastName"
                  value={editData.lastName || ''}
                  onChange={(e) => setEditData({ ...editData, lastName: e.target.value })}
                  className={validationErrors.lastName ? 'border-red-500' : ''}
                />
                {validationErrors.lastName && (
                  <p className="text-sm text-red-500 flex items-center gap-1">
                    <AlertCircle className="w-3 h-3" />
                    {validationErrors.lastName}
                  </p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-age">Edad</Label>
              <Input
                id="edit-age"
                type="number"
                min="10"
                max="120"
                value={editData.age ?? ''}
                onChange={(e) => {
                  const ageValue = e.target.value ? parseInt(e.target.value) : undefined
                  setEditData({ ...editData, age: ageValue })
                }}
                className={validationErrors.age ? 'border-red-500' : ''}
                placeholder="25"
              />
              {validationErrors.age && (
                <p className="text-sm text-red-500 flex items-center gap-1">
                  <AlertCircle className="w-3 h-3" />
                  {validationErrors.age}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-gender">Género</Label>
              <Select
                value={editData.gender || ''}
                onValueChange={(value) => setEditData({ ...editData, gender: value as "Masculino" | "Femenino" | "Prefiero no decir" })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecciona una opción" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Masculino">Masculino</SelectItem>
                  <SelectItem value="Femenino">Femenino</SelectItem>
                  <SelectItem value="Prefiero no decir">Prefiero no decir</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-address">Dirección</Label>
              <Input
                id="edit-address"
                value={editData.address || ''}
                onChange={(e) => setEditData({ ...editData, address: e.target.value })}
                placeholder="Av. Paseo Colón 850"
                className={validationErrors.address ? 'border-red-500' : ''}
              />
              {validationErrors.address && (
                <p className="text-sm text-red-500 flex items-center gap-1">
                  <AlertCircle className="w-3 h-3" />
                  {validationErrors.address}
                </p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleSaveEdit}>
              Guardar cambios
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isResetDialogOpen} onOpenChange={setIsResetDialogOpen}>
        <DialogContent className="sm:max-w-[480px]">
          <DialogHeader>
            <DialogTitle>Recuperación de contraseña</DialogTitle>
            <DialogDescription>
              {resetDialogMessage}
            </DialogDescription>
          </DialogHeader>
          <div className="py-2 text-sm text-muted-foreground">
            Este mensaje se cerrará automáticamente en {resetDialogSeconds} segundos.
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsResetDialogOpen(false)}>Cerrar ahora</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>


    </div>
  )
}