import random
import sys
sys.setrecursionlimit(20000)

RUTA_DICCIONARIO = "../Archivos/supergigante.txt"
RUTA_SALIDA = "prueba_generada.txt"
ENCODING = "UTF-8"

def generar_cadenas_por_tamano(tamanios):
    with open(RUTA_DICCIONARIO, "r", encoding=ENCODING) as diccionario:
        palabras = sorted([palabra.strip() for palabra in diccionario.readlines()], key=len, reverse=True)  # Orden descendente

    with open(RUTA_SALIDA, "w", encoding=ENCODING) as salida:
        for tam in tamanios:
            cadena = []
            longitud_actual = 0

            if _bt(cadena, palabras, tam, longitud_actual):
                salida.write("".join(cadena) + "\n")
                print(f"Cadena de tamaño {tam} generada")
            else:
                print(f"No se pudo generar {tam}")

def _bt(cadena, palabras, tam, longitud_actual):
    if longitud_actual == tam:
        return True

    espacio_restante = tam - longitud_actual
    posibles_palabras = [p for p in palabras if len(p) <= espacio_restante]
    random.shuffle(posibles_palabras)

    for palabra in posibles_palabras:
        cadena.append(palabra)
        if _bt(cadena, palabras, tam, longitud_actual + len(palabra)):
            return True
        cadena.pop()

    return None

def main():
    tamanios = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100,
        125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500,
        525, 550, 575, 600, 625, 650, 675, 700, 725, 750, 775, 800, 825, 850, 875, 900,
        925, 950, 975, 1000,
        1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000,
        2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900, 3000,
        3100, 3200, 3300, 3400, 3500, 3600, 3700, 3800, 3900, 4000,
        4100, 4200, 4300, 4400, 4500, 4600, 4700, 4800, 4900, 5000,
        5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000]

    generar_cadenas_por_tamano(tamanios)

if __name__ == "__main__":
    main()