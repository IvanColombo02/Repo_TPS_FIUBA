MSG_NO_ES = "No es un mensaje"
MAIN = '__main__'

def validar_mensaje(cadena, palabras):
    mensaje = []
    if _validar_mensaje_bt(cadena, 0, palabras, mensaje):
        return " ".join(mensaje)
    return MSG_NO_ES


def _validar_mensaje_bt(cadena, inicio, palabras, mensaje):
    if inicio == len(cadena):
        return True

    for fin in range(inicio + 1, len(cadena) + 1):
        palabra = cadena[inicio:fin]
        if palabra in palabras:
            mensaje.append(palabra)
            if _validar_mensaje_bt(cadena, fin, palabras, mensaje):
                return True
            mensaje.pop()
    return False

def main():
    print(validar_mensaje("estanoche", {"es", "tano", "ano", "anoche", "noche", "no", "che", "estan", "esta"}))
    print(validar_mensaje("universidaddebuenosaires", {"aire", "aires", "buen", "buenos", "colegio", "de", "es", "manzana", "no", "nos", "pelota", "si", "universidad", "ver"}))
    print(validar_mensaje("", {"hola", "chau", "racing", "club"}))
    print(validar_mensaje("noformamensaje", {"hola", "chau", "racing", "club"}))

if __name__ == MAIN:
    main()