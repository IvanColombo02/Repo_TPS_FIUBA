import random
import os
import argparse

ENCODING = "UTF-8"
ESCRITURA = "w"
INCREMENTO_T = 10
INCREMENTO_E = 5
LECTURA = "r"
MAIN = "__main__"
MSG_INICIAL = "# Primero viene la cantidad (n) de timestamps para ambos, luego n líneas que son un timestamp aproximado cada uno separado por una coma (',') del error, y luego n lineas de las transacciones del sospechoso\n"
TMP_INTERVALOS = ".intervalos.tmp"
TMP_TIMESTAMPS = ".timestamps.tmp"

def generar_listas(t_inicial, e_inicial, volumen, ruta):
    # Usa archivos temporales para evitar saturar la memoria si el volumen es muy grande
    try:
        with open(TMP_INTERVALOS, ESCRITURA, encoding=ENCODING) as intervalos, open(TMP_TIMESTAMPS, "w", encoding=ENCODING) as timestamps:
            
            for i in range(volumen):
                t = t_inicial + i * INCREMENTO_T 
                e = e_inicial + i * INCREMENTO_E 
                s = random.randint(t - e, t + e)

                intervalos.write(f'{t},{e}\n')
                timestamps.write(f'{s}\n')

        with open(ruta, "w", encoding=ENCODING) as archivo, open(TMP_INTERVALOS, LECTURA, encoding=ENCODING) as intervalos, open(TMP_TIMESTAMPS, "r", encoding=ENCODING) as timestamps:
            archivo.write(MSG_INICIAL)
            archivo.write(f'{volumen}\n')

            for t in intervalos:
                archivo.write(t)

            for s in timestamps:
                archivo.write(s)

    finally:
        if os.path.exists(TMP_INTERVALOS):
            os.remove(TMP_INTERVALOS)
        if os.path.exists(TMP_TIMESTAMPS):
            os.remove(TMP_TIMESTAMPS)

def main():
    parser = argparse.ArgumentParser(description="Genera archivos de prueba con soluciones compatibles.")
    parser.add_argument("volumen", type=int, help="Número de transacciones a generar.")
    parser.add_argument("ruta", type=str, help="Nombre del archivo de salida.")
    args = parser.parse_args()

    # Valores iniciales predeterminados
    t_inicial = 100
    e_inicial = 10

    generar_listas(t_inicial, e_inicial, args.volumen, args.ruta)

if __name__ == "__main__":
    main()