import argparse
from utils import *

# Resolución por Backtracking

def clusterizar_bajo_diametro(grafo, k):
    vertices = sorted(grafo.obtener_vertices(), key=lambda v: grafo.grado(v)) # Ordena por grado ascendente.
    distancias = calcular_distancias(grafo)

    sol_inicial, diam_max_inicial = hallar_aproximacion_inicial(vertices, k, distancias)
    if diam_max_inicial == 0 or (diam_max_inicial == 1 and k <= len(vertices)): # Casos en los que la aproximación siempre es correcta.
        return sol_inicial, diam_max_inicial

    sol_par, seteados_inicialmente = hallar_clusters_iniciales(vertices, k, grafo)
    return clusterizar_bajo_diametro_bt(1, vertices, k, distancias, sol_par, seteados_inicialmente, 0, sol_inicial, diam_max_inicial)
    
def clusterizar_bajo_diametro_bt(act, vertices, k, distancias, sol_par, seteados_inicialmente, diam_max_par, sol_opt, diam_max_opt):
    if act == len(vertices):
        return [cluster[:] for cluster in sol_par], diam_max_par
    
    if vertices[act] in seteados_inicialmente:
        return clusterizar_bajo_diametro_bt(act + 1, vertices, k, distancias, sol_par, seteados_inicialmente, diam_max_par, sol_opt, diam_max_opt)
    
    for i in range(k):
        nuevo_diam = calcular_nuevo_diam(vertices[act], sol_par[i], distancias)

        if nuevo_diam >= diam_max_opt: # Antes de agregar el nuevo vértice chequea si empeoraría el diámetro máximo del clúster, en ese caso evita probarlo.
            continue

        sol_par[i].append(vertices[act])
        sol_opt, diam_max_opt = clusterizar_bajo_diametro_bt(act + 1, vertices, k, distancias, sol_par, seteados_inicialmente, max(diam_max_par, nuevo_diam), sol_opt, diam_max_opt)
        sol_par[i].pop()

        if len(sol_par[i]) == 0:
            break
    
    return sol_opt, diam_max_opt

def main():

    parser = argparse.ArgumentParser()
    parser.add_argument("grafo", type=str, help="Nombre del archivo con aristas")
    parser.add_argument("k", type=int, help="Cantidad de clusters")
    args = parser.parse_args()

    ejecucion_principal(clusterizar_bajo_diametro, procesar_archivo(args.grafo), args.k)
    
if __name__ == MAIN:
    main()