from tp3 import calcular_distancias
from grafo import Grafo

def validador_solucion_clustering(grafo, k, c, solucion): # O(E + V^2)
    if len(solucion) > k:
        return False
    
    distancias = calcular_distancias(grafo) # O(V + E)

    visitados = set()
    for cluster in solucion: # O(V^2)
        for v in cluster:
            if v not in grafo or v in visitados:
                return False
            visitados.add(v)
            for a in cluster:
                if a not in grafo or distancias[v][a] > c:
                    return False
    return len(visitados) == len(grafo)

def main():
    # Algunas pruebas:
    """
    grafo = Grafo(False, ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'])
    grafo.agregar_arista('0', '1')
    grafo.agregar_arista('0', '8')
    grafo.agregar_arista('1', '4')
    grafo.agregar_arista('1', '3')
    grafo.agregar_arista('1', '5')
    grafo.agregar_arista('1', '6')
    grafo.agregar_arista('1', '9')
    grafo.agregar_arista('2', '7')
    grafo.agregar_arista('2', '3')
    grafo.agregar_arista('2', '5')
    grafo.agregar_arista('3', '4')
    grafo.agregar_arista('3', '6')
    grafo.agregar_arista('3', '7')
    grafo.agregar_arista('3', '5')
    grafo.agregar_arista('4', '9')
    grafo.agregar_arista('5', '7')
    grafo.agregar_arista('5', '8')
    grafo.agregar_arista('6', '7')
    grafo.agregar_arista('7', '9')
    grafo.agregar_arista('8', '9')
    print(validador_solucion_clustering(grafo, 2, 2, [['0', '1', '8', '4', '3', '5', '9'], ['6', '2', '7']])) # True
    print(validador_solucion_clustering(grafo, 3, 2, [['0', '1', '8', '4', '3', '5', '9'], ['6', '2', '7']])) # True
    print(validador_solucion_clustering(grafo, 2, 3, [['0', '1', '8', '4', '3', '5', '9'], ['6', '2', '7']])) # True
    print(validador_solucion_clustering(grafo, 10, 0, [['0'], ['1'], ['8'], ['4'], ['3'], ['5'], ['9'], ['6'], ['2'], ['7']])) # True
    print(validador_solucion_clustering(grafo, 2, 1, [['0', '1', '8', '4', '3', '5', '9'], ['6', '2', '7']])) # False
    print(validador_solucion_clustering(grafo, 2, 2, [['0', '1', '8', '4', '3', '5'], ['6', '2', '7']])) # False
    print(validador_solucion_clustering(grafo, 2, 2, [['0', '1', '8', '4', '3', '5', '9'], ['6', '2'], ['7']])) # False
    print(validador_solucion_clustering(grafo, 2, 2, [['0', '1', '8', '4', '3', '5', '9'], ['6', '2', '7', '10']])) # False
    """
    
if __name__ == "__main__":
    main()
