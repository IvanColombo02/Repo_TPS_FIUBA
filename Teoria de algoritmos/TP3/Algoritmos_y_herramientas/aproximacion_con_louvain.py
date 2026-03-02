import argparse
import time
from utils import *
from api_louvain import louvain

def calcular_diam_maximo(clusters, distancias):
    diam_max = 0
    for cluster in clusters:
        if len(cluster) <= 1:
            continue

        diam_cluster_actual = 0
        for i in range(len(cluster)):
            for j in range(i + 1, len(cluster)):
                dist = distancias[cluster[i]][cluster[j]]
                if dist > diam_cluster_actual:
                    diam_cluster_actual = dist
        
        if diam_cluster_actual > diam_max:
            diam_max = diam_cluster_actual
            
    return diam_max

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("grafo", type=str, help="Nombre del archivo con aristas")
    parser.add_argument("k", type=int, help="Cantidad de clusters")
    args = parser.parse_args()

    grafo = procesar_archivo(args.grafo)
    k = args.k

    t_ini = time.time()
    comunidades = louvain(grafo, k)
    t_fin = time.time()

    distancias = calcular_distancias(grafo)
    diam_max = calcular_diam_maximo(comunidades, distancias)
    print(formatear_salida(comunidades, diam_max))
    print(f"Tardó {t_fin - t_ini: } segundos")

if __name__ == "__main__":
    main()
