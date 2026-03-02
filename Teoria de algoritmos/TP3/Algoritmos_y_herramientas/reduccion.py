from grafo import Grafo

# Maqueta de código de reducción:
def k_coloreo(grafo, k):
    grafo_complemento = Grafo(False, grafo.obtener_vertices())
    
    for v in grafo:
        for w in grafo:
            if v == w:
                continue
            if not grafo.estan_unidos(v, w):
                grafo_complemento.agregar_arista(v, w)
    
    return clusterizar_bajo_diametro(grafo_complemento, k, 1)