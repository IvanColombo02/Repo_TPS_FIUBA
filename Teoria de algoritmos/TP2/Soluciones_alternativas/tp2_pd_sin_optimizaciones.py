MSG_NO_ES = "No es un mensaje"

ERROR_PROCESAMIENTO_ARCHIVO = "Error en el procesamiento del archivo."
HELP_ARCHIVO ='ruta del archivo a procesar'
MSG_SIN_ARCHIVO = 'No se proporcionó una ruta, se correrá con un ejemplo por defecto'
MAIN = '__main__'
MODO_LECTURA = 'r'
RUTA_NO_ENCONTRADA = 'La ruta {} no fue encontrada.'

def validar_mensaje(cadena, palabras): # O(n^3)
    if len(cadena) == 0:
        return ""

    matriz = [[False for _ in range(len(cadena))] for _ in range(len(cadena))]

    for i in range(len(cadena)):
        for j in range(i, len(cadena)):
            if matriz[i - 1][j]:
                matriz[i][j] = True

            palabra = cadena[i : j + 1]
            if palabra in palabras and (i == 0 or matriz[i - 1][i - 1]):
                matriz[i][j] = True

    if matriz[-1][-1] == False:
        return MSG_NO_ES

    return reconstruir_mensaje(matriz, cadena)

def reconstruir_mensaje(matriz, cadena): # O(n)
    mensaje = []
    i, j = len(matriz) - 1, len(matriz[0]) - 1

    while i >= 0:
        if i > 0 and matriz[i][j] == matriz[i - 1][j]:
            i -= 1
            continue
        palabra = cadena[i : j + 1]
        mensaje.append(palabra)
        i, j = i - 1, i - 1
    
    return " ".join(mensaje[::-1])
    

def procesar_archivo(ruta):
    try:
        with open(ruta, MODO_LECTURA, encoding="UTF-8") as archivo:
            cadenas = []
            for palabra in archivo:
                cadenas.append(palabra.strip())
            
            return cadenas

    except FileNotFoundError:
        raise FileNotFoundError(RUTA_NO_ENCONTRADA.format(ruta))
    except ValueError:
        raise ValueError(ERROR_PROCESAMIENTO_ARCHIVO)

def main():
    print(validar_mensaje("estanoche", {"es", "tano", "ano", "anoch", "noche", "no", "che", "estan", "esta"}))
    print(validar_mensaje("universidaddebuenosaires", {"aire", "aires", "buen", "buenos", "colegio", "de", "es", "manzana", "no", "nos", "pelota", "si", "universidad", "ver"}))
    print(validar_mensaje("", {"hola", "chau", "racing", "club"}))
    print(validar_mensaje("noformamensaje", {"hola", "chau", "racing", "club"}))

if __name__ == MAIN:
    main()