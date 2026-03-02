from pulp import LpProblem, LpVariable, LpMinimize, lpSum, value, PULP_CBC_CMD
import argparse
from utils import *

MAIN = '__main__'

def clusterizar_bajo_diametro(grafo, cant_clusters):
    vertices = grafo.obtener_vertices()
    distancias = calcular_distancias(grafo)
    indices_seteo_inicial = hallar_seteo_inicial(vertices, cant_clusters, grafo)
    _, dist_max_aprox = hallar_aproximacion_inicial(vertices, cant_clusters, distancias)

    problema = LpProblem("determinar_clusters", LpMinimize) # Definición del problema

    variables = [] # Fila indica cluster, columna indica vértice
    # Cada variable índica ¿El i-ésimo vértice está en el j-ésimo cluster?:
    for cluster in range(cant_clusters):
        variables.append([LpVariable(f'{v}_{cluster}', cat = "Binary") for v in vertices])

    dist_max = LpVariable(f'dist_max', lowBound = 0, cat = "Continuous") # Máxima distancia
    problema += dist_max <= dist_max_aprox

    # Seteo inicial de vértices que no podrían estar en un mismo clúster:
    for cluster, i in enumerate(indices_seteo_inicial):
        problema += variables[i][cluster] == 1

    for i, v in enumerate(vertices):
        # Cada vértice debe estar en un único cluster:
        problema += lpSum(variables[cluster][i] for cluster in range(cant_clusters)) == 1
        
        for j in range(i + 1, len(vertices)):
            w = vertices[j]

            for cluster in range(cant_clusters):
                # Si los dos vértices pertenecen al mismo clúster, dist_max deberá ser >= a la distancia entre ellos
                problema += distancias[v][w] - distancias[v][w] * (2 - variables[cluster][i] - variables[cluster][j]) <= dist_max
                
    problema += dist_max # Función objetivo (se desea minimizar la distancia máxima)
    problema.solve(PULP_CBC_CMD(msg = False))
    
    # Interpretación de los resultados:
    clusters = []
    for cluster in range(cant_clusters):
        clusters.append([vertices[i] for i in range(len(vertices)) if value(variables[cluster][i]) == 1])
    return clusters, int(value(dist_max))

def hallar_seteo_inicial(vertices, k, grafo):
    seteados_inicialmente = []
    visitados = set()
    for i, v in enumerate(vertices):
        if v not in visitados:
            seteados_inicialmente.append(i)
            if len(seteados_inicialmente) == k:
                break

            visitados.add(v)
            cola = deque([v])
            while cola:
                w = cola.popleft()
                for a in grafo.adyacentes(w):
                    if a not in visitados:
                        visitados.add(a)
                        cola.append(a)
                    
    return seteados_inicialmente

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("grafo", type=str, help="Nombre del archivo con aristas")
    parser.add_argument("k", type=int, help="Cantidad de clusters")
    args = parser.parse_args()

    ejecucion_principal(clusterizar_bajo_diametro, procesar_archivo(args.grafo), args.k)
    
if __name__ == MAIN:
    main()