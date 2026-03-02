import random
import string
import argparse

ENCODING = "UTF-8"
ESCRITURA = "w"
LECTURA = 'r'
MAIN = "__main__"
LARGO_MIN = 3
LARGO_MAX = 15
MULTIPLICADOR_VOL = 5

def generar_palabra_random():
    largo = random.randint(LARGO_MIN, LARGO_MAX)
    caracteres = string.ascii_lowercase
    return ''.join(random.choice(caracteres) for _ in range(largo))

def tomar_random_del_diccionario(diccionario):
    return random.choice(diccionario).strip()

def tomar_random_cortas_del_diccionario(diccionario):
    palabra = random.choice(diccionario).strip()
    while len(palabra) > 6:
        palabra = random.choice(diccionario).strip()
    return palabra   

def tomar_random_largas_del_diccionario(diccionario):
    palabra = random.choice(diccionario).strip()
    while len(palabra) <= 14:
        palabra = random.choice(diccionario).strip()
    return palabra   

def generar_datos(cant_palabras, volumen, ruta1, ruta2, ruta3, tipo_generador):

    generadores = {
        "RANDOM": generar_palabra_random,
        "RANDOM_DICT": tomar_random_del_diccionario,
        "CORTAS": tomar_random_cortas_del_diccionario,
        "LARGAS": tomar_random_largas_del_diccionario
    }

    if tipo_generador not in generadores:
        raise ValueError(f"Tipo de generador inválido")

    generador = generadores[tipo_generador]

    with open(ruta1, ESCRITURA, encoding=ENCODING) as cadenas, open(ruta2, ESCRITURA, encoding=ENCODING) as dicc:
        with open(ruta3, LECTURA, encoding=ENCODING) as archivo:
            palabras_dict = archivo.readlines()

        # Genero cadenas y agrego sus palabras a archivo diccionario
            for _ in range(volumen):
                palabras = []
                set_palabras = set()

                for _ in range(cant_palabras):
                    # palabra = generar_palabra_random()
                    # palabra = tomar_random_del_diccionario(palabras_dict)
                    palabra = generador() if generador == generar_palabra_random else generador(palabras_dict)

                    if palabra not in set_palabras:
                        dicc.write(f'{palabra}\n')
                        set_palabras.add(palabra)

                    palabras.append(palabra)

                cadenas.write(f"{''.join(palabras)}\n")


            for _ in range(volumen * MULTIPLICADOR_VOL):
                # palabra = generar_palabra_random()
                # palabra = tomar_random_del_diccionario(palabras_dict)
                palabra = generador() if generador == generar_palabra_random else generador(palabras_dict)
                while palabra in set_palabras:
                    # palabra = generar_palabra_random()
                    # palabra = tomar_random_del_diccionario(palabras_dict)
                    palabra = generador() if generador == generar_palabra_random else generador(palabras_dict)

                dicc.write(f'{palabra}\n')
                set_palabras.add(palabra)

def main():
    parser = argparse.ArgumentParser(description="Genera archivos de prueba con soluciones compatibles.")
    parser.add_argument("cant_palabras", type=int, help="Cantidad de palabras por cadena.")
    parser.add_argument("volumen", type=int, help="Cantidad de cadenas a generar.")
    parser.add_argument("ruta1", type=str, help="Nombre del archivo de cadenas.")
    parser.add_argument("ruta2", type=str, help="Nombre del archivo de palabras.")
    parser.add_argument("ruta3", type=str, help="Nombre del archivo diccionario.")
    parser.add_argument("tipo_generador", type=str, choices=["RANDOM", "RANDOM_DICT", "CORTAS", "LARGAS"], help="Tipo de generador de palabras: RANDOM, RANDOM_DICT, CORTAS, LARGAS.")
    args = parser.parse_args()

    generar_datos(args.cant_palabras, args.volumen, args.ruta1, args.ruta2, args.ruta3, args.tipo_generador)

if __name__ == MAIN:
    main()