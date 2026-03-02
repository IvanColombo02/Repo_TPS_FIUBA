import random

VERTICE_EXISTE = "El vértice ingresado ya existe"
VERTICE_NO_EXISTE = "El vértice ingresado no existe"
ARISTA_EXISTE = "La arista ingresada ya existe"
NO_UNIDOS = "Los vértices no están unidos"
ESTAN_UNIDOS = "Los vértices ya están unidos"
EXTEMOS_NO_EXISTEN = "Uno de los extremos de la arista no existe"

class Grafo:
    def __init__(self, es_dirigido = False, vertices=[]): # O(V), O(1) si se crea sin vértices
        self.es_dirigido = es_dirigido
        self.vertices = [] # Lo usamos para que obtener_vertices sea O(1)
        self.adyacencias = dict()
        self.pesos = dict()
        for v in vertices:
            self.agregar_vertice(v)

    def agregar_vertice(self, v): # O(1)
        if v in self.adyacencias:
            raise ValueError(VERTICE_EXISTE)
        self.adyacencias[v] = set()
        self.vertices.append(v)

    def borrar_vertice(self, v): # O(V)
        self._chequear_existencia_vertice(v)
        
        del self.adyacencias[v]
        self.vertices.remove(v)

        for adyacentes in self.adyacencias.values():
                adyacentes.discard(v)
        
        a_eliminar = []
        for arista in self.pesos.keys():
            if v in arista:
                a_eliminar.append(arista)
        
        for arista in a_eliminar:
            del self.pesos[arista]
                
    def agregar_arista(self, v, w, peso = 1): # O(1)
        self._chequear_existencia_vertice(v)
        self._chequear_existencia_vertice(w)
        if self.estan_unidos(v, w):
            raise ValueError(ESTAN_UNIDOS)
        
        self.adyacencias[v].add(w)
        self.pesos[(v, w)] = peso
        if not self.es_dirigido:
            self.adyacencias[w].add(v)
            self.pesos[(w, v)] = peso
    
    def actualizar_peso(self, v, w, peso):
        self._chequear_existencia_vertice(v)
        self._chequear_existencia_vertice(w)
        self._chequear_union(v, w)
        self.pesos[(v, w)] = peso
        if not self.es_dirigido and v != w:
            self.pesos[(w, v)] = peso

    def borrar_arista(self, v, w): # O(1)
        self._chequear_union(v, w)
        self.adyacencias[v].remove(w)
        del self.pesos[(v, w)]
        if not self.es_dirigido and v != w:
            self.adyacencias[w].remove(v)
            del self.pesos[(w, v)]

    def estan_unidos(self, v, w): # O(1)
        return w in self.adyacencias[v]
        
    def peso_arista(self, v, w): # O(1)
        self._chequear_union(v, w)
        return self.pesos[(v, w)]

    def obtener_vertices(self): # O(1)
        return self.vertices

    def vertice_aleatorio(self): # O(1)
        return random.choice(self.vertices)

    def adyacentes(self, v): # O(E)
        self._chequear_existencia_vertice(v)
        return list(self.adyacencias[v])
    
    def grado(self, v):
        self._chequear_existencia_vertice(v)
        return len(self.adyacencias[v])
    
    def __iter__(self): # O(1)
        return iter(self.vertices)
    
    def _chequear_existencia_vertice(self, v): # O(1)
        if v not in self.adyacencias:
            raise ValueError(VERTICE_NO_EXISTE)
    
    def _chequear_union(self, v, w): # O(1)
        if not self.estan_unidos(v, w):
            raise ValueError(NO_UNIDOS)
        
    def __len__(self): # O(1)
        return len(self.vertices)

    def __contains__(self, v): #O(1)
        return v in self.adyacencias