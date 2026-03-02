from utils import *
import argparse

def hallar_aproximacion_greedy(grafo, k):
    vertices = sorted(grafo.obtener_vertices(), key=lambda v: grafo.grado(v)) # Ordena por grado ascendente
    distancias = calcular_distancias(grafo)
    solucion, max_diam = [[] for _ in range(k)], 0

    for v in vertices:
        min_diam, min_i = float('inf'), 0
        for i in range(k):
            nuevo_diam = calcular_nuevo_diam(v, solucion[i], distancias)
            if nuevo_diam < min_diam:
                min_diam, min_i = nuevo_diam, i
        max_diam = min_diam if min_diam > max_diam else max_diam
        solucion[min_i].append(v)

    return solucion, max_diam

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("grafo", type=str, help="Nombre del archivo con aristas")
    parser.add_argument("k", type=int, help="Cantidad de clusters")
    args = parser.parse_args()

    ejecucion_principal(hallar_aproximacion_greedy, procesar_archivo(args.grafo), args.k)
    
if __name__ == MAIN:
    main()