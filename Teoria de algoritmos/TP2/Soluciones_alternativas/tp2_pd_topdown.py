MSG_NO_ES = "No es un mensaje"
MAIN = '__main__'

def validar_mensaje(cadena, palabras):
    mensaje = []
    if _validar_mensaje_dp(cadena, 0, palabras, mensaje, set()):
        return " ".join(mensaje)
    return MSG_NO_ES


def _validar_mensaje_dp(cadena, inicio, palabras, mensaje, memoization):
    if inicio == len(cadena):
        return True
    
    if inicio in memoization:
        return False

    for fin in range(inicio + 1, len(cadena) + 1):
        palabra = cadena[inicio:fin]
        if palabra in palabras:
            mensaje.append(palabra)
            if _validar_mensaje_dp(cadena, fin, palabras, mensaje, memoization):
                return True
            mensaje.pop()
    
    memoization.add(inicio)
    return False

def main():
    print(validar_mensaje("estanoche", {"es", "tano", "ano", "anoche", "noche", "no", "che", "estan", "esta"}))
    print(validar_mensaje("universidaddebuenosaires", {"aire", "aires", "buen", "buenos", "colegio", "de", "es", "manzana", "no", "nos", "pelota", "si", "universidad", "ver"}))
    print(validar_mensaje("", {"hola", "chau", "racing", "club"}))
    print(validar_mensaje("noformamensaje", {"hola", "chau", "racing", "club"}))

if __name__ == MAIN:
    main()