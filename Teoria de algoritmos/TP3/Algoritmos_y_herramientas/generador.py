import networkx as nx
import argparse

MODO_ESCRITURA = 'w'

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("ruta", type=str, help="Ruta donde se guardará el grafo")
    parser.add_argument("generador", type=str, help="Nombre del generador(BA, WS, ER)")
    parser.add_argument("v", type=int, help="Cantidad de vértices")
    parser.add_argument("p1", type=float, help="Primer parámetro del generador") # en BA es cantidad de aristas, en WS es cantidad de conexiones con vecinos cercanos, en ER es la probabilidad de generar una arista
    parser.add_argument("p2", type=float, nargs="?", default=0.5, help="Segundo parámetro del generador (opcional para WS)") #probabilidad de reconectar una arista, asigna 0.5 por defecto
    args = parser.parse_args()

    generador = str.upper(args.generador)
    if generador == "BA":
        grafo = nx.barabasi_albert_graph(args.v, int(args.p1))
    elif generador == "WS":
        grafo = nx.watts_strogatz_graph(args.v, int(args.p1), args.p2)
    elif generador == "ER":
        grafo = nx.erdos_renyi_graph(args.v, args.p1) #también se puede llamar con gnp_random_graph
    else:
        raise ValueError("Generador no válido, usar BA, WS o ER")

    with open(args.ruta, MODO_ESCRITURA) as archivo:
        archivo.write("# sarasa\n")
        for v, w in grafo.edges():
            archivo.write(f"{v},{w}\n")

if __name__ == "__main__":
    main()