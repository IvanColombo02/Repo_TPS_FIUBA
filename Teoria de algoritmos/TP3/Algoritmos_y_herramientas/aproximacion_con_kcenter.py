import argparse
from utils import *

def clusterizar_bajo_diametro(grafo, k):
    distancias = calcular_distancias(grafo)

    # Se crea un diccionario en el que cada centro es la clave y sus valores
    # son los vertices mas cercanos. Luego, estos formaran clusters.
    centros = {c: set() for c in k_center(distancias, grafo, k)}

    # Se itera cada vertice y se los asigna al centro con < distancia.
    for v in grafo:
        if v in centros:
            continue

        dist_min = float('inf')
        asignacion = None
        for c in centros.keys():
            if distancias[v][c] < dist_min:
                dist_min = distancias[v][c]
                asignacion = c
        
        centros[asignacion].add(v)

    # Se le da el formato correcto a los clusters y se calcula la distancia
    # maxima entre dos vertices.
    solucion = [[] for _ in range(k)]
    dist_max = 0
    for i, (c, asignados) in enumerate(centros.items()):
        solucion[i].append(c)
        for v in asignados:
            for a in solucion[i]:
                dist_max = distancias[v][a] if distancias[v][a] > dist_max else dist_max
            solucion[i].append(v)

    return solucion, dist_max

def k_center(distancias, grafo, k):
    vertices = grafo.obtener_vertices()

    # Asigna inicialmente un vertice aleatorio como centro inicial.
    centros = {grafo.vertice_aleatorio()}

    # Itera todos los vertices y asigna como centros a todos aquellos
    # cuya distancia minima sea maxima, hasta llega a tener K centros.
    while len(centros) < k:
        dist_min_max, min_max = -1, None

        for v in vertices:
            if v in centros:
                continue

            min_dist_a_centros = min(distancias[v][c] for c in centros)
            if min_dist_a_centros > dist_min_max:
                dist_min_max = min_dist_a_centros
                min_max = v
        
        if min_max is None: # Si no llega a poder asignar K centros
            break
        centros.add(min_max)

    return centros

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("grafo", type=str, help="Nombre del archivo con aristas")
    parser.add_argument("k", type=int, help="Cantidad de clusters")
    args = parser.parse_args()

    ejecucion_principal(clusterizar_bajo_diametro, procesar_archivo(args.grafo), args.k)
    
if __name__ == MAIN:
    main()





