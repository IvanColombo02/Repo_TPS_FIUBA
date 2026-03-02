import argparse
import sys

MSG_NO_ES = "No es un mensaje"
ENCODING = "UTF-8"
ERROR_PROCESAMIENTO_ARCHIVO = "Error en el procesamiento del archivo."
HELP_ARCHIVO ='ruta del archivo a procesar'
MSG_SIN_ARCHIVO = 'No se proporcionó una ruta, se correrá con un ejemplo por defecto'
MAIN = '__main__'
MODO_LECTURA = 'r'
RUTA_NO_ENCONTRADA = 'La ruta {} no fue encontrada.'

def validar_mensaje(cadena, palabras, largo_min = None, largo_max = None): # O(k + n^3)
    if len(cadena) == 0:
        return ""

    validacion = [None] * len(cadena)

    if largo_min is None or largo_max is None:
        largo_min, largo_max = buscar_largos_extremos(palabras)

    for ini in range(len(cadena)):
        for i in range(ini + largo_min - 1, min(ini + largo_max, len(cadena))):
            fin = i + 1

            if validacion[i] is None:
                if cadena[ini:fin] in palabras and (ini == 0 or validacion[ini - 1] is not None):
                    validacion[i] = ini

        if validacion[-1] is not None: # Ya encontré palabras para la totalidad de la cadena.
            break

    if validacion[-1] is None:
        return MSG_NO_ES

    return reconstruir_mensaje(validacion, cadena)

def reconstruir_mensaje(validacion, cadena): # O(n)
    i = len(validacion) - 1
    palabras = []

    while i >= 0:
        palabras.append(cadena[validacion[i] : i + 1])
        i = validacion[i] - 1
    
    return " ".join(palabras[::-1])

def buscar_largos_extremos(palabras): # O(k)
    min, max = float('inf'), 0

    for palabra in palabras:
        if len(palabra) < min:
            min = len(palabra)
        if len(palabra) > max:
            max = len(palabra)
    
    return min, max

def procesar_archivo(ruta):
    try:
        with open(ruta, MODO_LECTURA, encoding=ENCODING) as archivo:
            cadenas = []
            for palabra in archivo:
                cadenas.append(palabra.strip())
            return cadenas

    except FileNotFoundError:
        raise FileNotFoundError(RUTA_NO_ENCONTRADA.format(ruta))
    except ValueError:
        raise ValueError(ERROR_PROCESAMIENTO_ARCHIVO)

def main():

    parser = argparse.ArgumentParser()
    parser.add_argument("palabras", type=str, help="Nombre del archivo diccionario")
    args = parser.parse_args()

    palabras = set(procesar_archivo(args.palabras))
    largo_min, largo_max = buscar_largos_extremos(palabras)

    for linea in sys.stdin:
        cadena = linea.strip()
        if cadena:
            print(validar_mensaje(cadena, palabras, largo_min, largo_max))

if __name__ == MAIN:
    main()