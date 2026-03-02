from grafo import Grafo
from collections import deque
import time

ENCODING = "UTF-8"
ERROR_PROCESAMIENTO_ARCHIVO = "Error en el procesamiento del archivo."
MAIN = '__main__'
MODO_LECTURA = 'r'
RUTA_NO_ENCONTRADA = 'La ruta {} no fue encontrada.'

def calcular_distancias(grafo):
    distancias = {}
    for v in grafo:
        distancias[v] = _bfs(grafo, v)
    return distancias

def _bfs(grafo, origen):
    distancia = {origen: 0}
    cola = deque([origen])

    while cola:
        v = cola.popleft()
        for a in grafo.adyacentes(v):
            if a not in distancia:
                distancia[a] = distancia[v] + 1
                cola.append(a)
        
    for v in grafo:
        if v not in distancia:
            distancia[v] = float('inf')
    
    return distancia

def hallar_aproximacion_inicial(vertices, k, distancias):
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

def hallar_clusters_iniciales(vertices, k, grafo):
    seteados_inicialmente = set()
    clusters = [[] for _ in range(k)]
    ult = 0
    visitados = set()
    for v in vertices:
        if v not in visitados:
            seteados_inicialmente.add(v)
            clusters[ult].append(v)
            ult += 1
            if ult == k:
                break

            visitados.add(v)
            cola = deque([v])
            while cola:
                w = cola.popleft()
                for a in grafo.adyacentes(w):
                    if a not in visitados:
                        visitados.add(a)
                        cola.append(a)
    return clusters, seteados_inicialmente

def calcular_nuevo_diam(v, cluster, distancias):
    max = 0
    for a in cluster:
        max = distancias[v][a] if distancias[v][a] > max else max
    return max

def formatear_salida(sol_parcial, max_diam):
    salida = []
    for cluster_id, vertices in enumerate(sol_parcial):
        lista_vertices = [str(vertice) for vertice in vertices]
        salida.append(f"Cluster {cluster_id} : {lista_vertices}")
    if max_diam:
        salida.append(f"Maxima distancia dentro del cluster: {max_diam}")
    return "\n".join(salida)

def procesar_archivo(ruta):
    try:
        with open(ruta, MODO_LECTURA, encoding=ENCODING) as archivo:
            grafo = Grafo()
            for linea in archivo:
                if linea.startswith("#"): 
                    continue
                v1, v2 = linea.strip().split(',')
                if v1 not in grafo:
                    grafo.agregar_vertice(v1)
                if v2 not in grafo:
                    grafo.agregar_vertice(v2)
                grafo.agregar_arista(v1, v2)
            return grafo

    except FileNotFoundError:
        raise FileNotFoundError(RUTA_NO_ENCONTRADA.format(ruta))
    except ValueError:
        raise ValueError(ERROR_PROCESAMIENTO_ARCHIVO)

def ejecucion_principal(funcion, grafo, k):
    t_ini = time.time()
    sol, diam_max = funcion(grafo, k)
    t_fin = time.time()

    print(formatear_salida(sol, diam_max))
    print(f"Tardó {t_fin - t_ini: } segundos")