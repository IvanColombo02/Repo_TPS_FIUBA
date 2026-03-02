import argparse
import os
from TP1.lista_doblemente_enlazada import ListaDoblementeEnlazada

ERROR_PROCESAMIENTO_ARCHIVO = "Error en el procesamiento del archivo."
HELP_ARCHIVO ='ruta del archivo a procesar'
MSG_SIN_ARCHIVO = 'No se proporcionó una ruta, se correrá con un ejemplo por defecto'
MSG_SOSP_INCORRECTO = 'El sospechoso no es quién se estaba buscando :('
MAIN = '__main__'
MODO_LECTURA = 'r'
RUTA_NO_ENCONTRADA = 'La ruta {} no fue encontrada.'
S_DEFAULT = [125, 280, 320, 398, 509, 647, 730, 850, 980, 1000]
T_DEFAULT = [(100, 30), (200, 200), (300, 60), (400, 5), (500, 23), (600, 56), (700, 70), (800, 300), (900, 54), (1000, 10)]

# Complejidad: O(n^2)
def verificar_coincidencia_timestamps(t,s):
    solucion = []
    t_ord = ListaDoblementeEnlazada(sorted(t, key = lambda x: x[0] + x[1]))
    
    for timestamp in s:
        flag = False
        
        for nodo in t_ord:
            aproximacion, error = nodo.get_valor()
            inicio = aproximacion - error
            fin = aproximacion + error

            if inicio <= timestamp <= fin:
                flag = True
                t_ord.borrar_elemento(nodo)
                solucion.append((timestamp, aproximacion, error))
                break
        
        if flag == False: # No se seleccionó intervalo, el problema no tiene solución.
            return None

    return solucion

def procesar_archivo_entrada(ruta):
    try:
        with open(ruta, MODO_LECTURA) as archivo:
            lines = archivo.readlines() # Salteo la primera linea.

        n = int(lines[1].strip()) # Tamaño de la lista a crear.

        t = [tuple(map(int, x.strip().split(','))) for x in lines[2:2 + n]]
        s = [int(x) for x in lines[2 + n:2 + 2 * n]]
        return t, s

    except FileNotFoundError:
        raise FileNotFoundError(RUTA_NO_ENCONTRADA.format(ruta))
    except ValueError:
        raise ValueError(ERROR_PROCESAMIENTO_ARCHIVO)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('archivo', type=str, nargs='?', help=HELP_ARCHIVO)
    args = parser.parse_args()
    if not args.archivo:
        print(MSG_SIN_ARCHIVO)
        t, s = T_DEFAULT, S_DEFAULT
    else:
        t, s = procesar_archivo_entrada(os.path.abspath(args.archivo))

    solucion = verificar_coincidencia_timestamps(t, s)
    if not solucion:
        print(MSG_SOSP_INCORRECTO)
    else:
        for i in solucion:
            print(f"{i[0]} --> {i[1]} ± {i[2]}")

if __name__ == MAIN:
    main()