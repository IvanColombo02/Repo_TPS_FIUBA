from grafo import Grafo
from collections import deque
from heapq import heapify, heappop

CANCIONES = "canciones"
CO_AMORTIGUACION = 0.85
ITERACIONES = 9
MSG_CANCION_NO_EXISTE = "La canción no existe"
MSG_CANCIONES_NO_EXISTEN = "Tanto el origen como el destino deben ser canciones"
RANK_INICIAL = 1
SIN_CAMINO = "No se encontro recorrido"
USUARIOS = "usuarios"

class SistemaMusical:
    def __init__(self):
        self.user_canciones = Grafo(es_dirigido=False) # Grafo bipartito que conecta usuarios-canciones.
        self.proyeccion = None # Proyección del grafo user_canciones, este conecta canciones entre si.

        self.canciones = set() # Canciones
        self.usuarios = set() # IDs de usuario
        self.playlists = {} # ID de playlist: nombre de playlist
        self.aristas_playlist = {} # (ID de usuario, canción): playlist que los conecta (solo se guarda una, los saltos no varían)
        self.rank_canciones = [] # Max heap de canciones según importancia

    def procesar_entrada(self, id_usuario, cancion, artista, id_playlist, nombre_playlist):
        """
        Procesa los datos de entrada guardandolos y formatenadolos para su posterior uso.
         Además, arma el grafo de canciones y usuarios.
        """
        self._procesar_usuario(id_usuario)
        self._procesar_playlist(id_playlist, nombre_playlist)
        cancion_procesada = self._procesar_cancion(cancion, artista)

        if not self.user_canciones.estan_unidos(id_usuario, cancion_procesada):
            self.user_canciones.agregar_arista(id_usuario, cancion_procesada)
            self.aristas_playlist[(id_usuario, cancion_procesada)] = id_playlist

    def camino_minimo(self, cancion1, cancion2):
        """
        Imprime una lista con la cual se conecta (en la menor cantidad de pasos posibles) una canción con otra.
        """
        if not self._chequear_existencia_canciones(cancion1, cancion2):
            return

        padres = {cancion1: None}
        _bfs(cancion1, self.user_canciones, padres=padres, destino=cancion2)

        camino = self._reconstruir_camino(cancion1, cancion2, padres)
        self._imprimir_camino(camino)

    def rango(self, cancion, saltos):
        """
        Permite obtener la cantidad de canciones que se encuenten a exactamente n saltos desde la cancion
        pasada por parámetro.
        """
        if not self._chequear_existencia_cancion(cancion):
            return

        if not self._existe_proyeccion():
            self._hallar_proyeccion()

        contador = [0]
        _bfs(cancion, self.proyeccion, saltos=saltos, contador=contador)
        print(contador[0])

    def ciclo_canciones(self, origen, largo):
        """
        Permite obtener un ciclo de largo n (dentro de la red de canciones) que comience en la canción indicada.
        """
        if not self._chequear_existencia_cancion(origen):
            return

        if not self._existe_proyeccion():
            self._hallar_proyeccion()

        ciclo = self._buscar_ciclo(origen, origen, largo - 1, set(), {origen: None})
        if ciclo is None:
            print(SIN_CAMINO)
            return
        self._imprimir_ciclo(ciclo)

    def mas_importantes(self, cantidad):
        """
        Muestra las n canciones más centrales/importantes del mundo según el algoritmo de pagerank,
        ordenadas de mayor importancia a menor importancia.
        """
        if not self.rank_canciones:
            for v, rank in _page_rank(self.user_canciones).items():
                if v in self.canciones:
                    self.rank_canciones.append((-rank, v))
            heapify(self.rank_canciones)

        copia = self.rank_canciones.copy()
        for _ in range(min(cantidad, len(copia)) - 1):
            _, cancion = heappop(copia)
            print(f'{cancion}; ', end="")
        _, cancion = heappop(copia)
        print(cancion)

    def recomendacion(self, largo, modo, canciones):
        """
        Permite obtener una serie de recomendaciones de usuarios/canciones en base a una lista de canciones pasada.
        """
        rank = {}
        for cancion in canciones:
            rank[cancion] = RANK_INICIAL
            self._obtener_recomendacion(largo, cancion, rank)

        recomendaciones = []
        set_canciones = set(canciones)

        for clave, valor in rank.items():
            if modo == CANCIONES and clave not in self.canciones:
                continue
            if modo == USUARIOS and clave not in self.usuarios:
                continue
            if clave in set_canciones:
                continue
            recomendaciones.append((-valor, clave))

        heapify(recomendaciones)
        for _ in range(largo-1):
            _, v = heappop(recomendaciones)
            print(f'{v}; ', end="")
        _, v = heappop(recomendaciones)
        print(v)

    # Funciones auxiliares del procesamiento de entradas:
    def _procesar_usuario(self, id_usuario):
        """
        Si el usuario no se encuentra en el sistema, lo agrega y crea sus dependencias.
        """
        if id_usuario in self.usuarios:
            return
        self.usuarios.add(id_usuario)
        self.user_canciones.agregar_vertice(id_usuario)

    def _procesar_cancion(self, cancion, artista):
        """
        Formatea correctamente una canción y la agrega al sistema.
        """
        cancion_procesada = f'{cancion} - {artista}'
        if cancion_procesada in self.canciones:
            return cancion_procesada
        self.canciones.add(cancion_procesada)
        self.user_canciones.agregar_vertice(cancion_procesada)
        return cancion_procesada

    def _procesar_playlist(self, id_playlist, nombre_playlist):
        """
        Si la playlist no se encuentra en el sistema, la agrega y crea sus dependencias.
        """
        if id_playlist in self.playlists:
            return
        self.playlists[id_playlist] = nombre_playlist

    # Funciones auxiliares para la ejecución de utilidades:
    def _reconstruir_camino(self, cancion1, cancion2, padres):
        """
        Dadas dos canciones (origen y fin) y un diccionario de padres, reconstruye el camino en el orden correcto.
        """
        if cancion2 not in padres:
            return None

        camino = []
        actual = cancion2

        while actual != cancion1:
            camino.append(actual)
            id_playlist = ""
            if actual in self.usuarios:
                id_playlist = self.aristas_playlist[(actual, padres[actual])]
            else:
                id_playlist = self.aristas_playlist[(padres[actual], actual)]
            camino.append(id_playlist)
            actual = padres[actual]

        camino.append(cancion1)
        return camino[::-1]

    def _imprimir_camino(self, camino):
        """
        Imprime un camino entre dos canciones.
        """
        if not camino:
            print(SIN_CAMINO)
            return

        for i, actual in enumerate(camino):
            if actual in self.canciones:
                if i == len(camino) - 1:
                    print(actual)
                else:
                    print(f'{actual} --> aparece en playlist --> ', end="")
            elif actual in self.playlists:
                if camino[i - 1] in self.canciones:
                    print(f'{self.playlists[actual]} --> de --> ', end="")
                else:
                    print(f'{self.playlists[actual]} --> donde aparece --> ', end="")
            elif actual in self.usuarios:
                print(f'{actual} --> tiene una playlist --> ', end="")

    def _buscar_ciclo(self, v, origen, largo, visitados, padres):
        """
        Busca un ciclo de un largo predefinido, implementa un recorrido DFS modificado.
        """
        visitados.add(v)
        if largo == -1:
            return
        for a in self.proyeccion.adyacentes(v):
            if a == origen and largo == 0:
                return _reconstruir_ciclo(v, origen, padres)
            if a not in visitados:
                padres[a] = v
                ciclo = self._buscar_ciclo(a, origen, largo - 1, visitados, padres)
                if ciclo is not None:
                    return ciclo
        visitados.remove(v)

    def _obtener_recomendacion(self, largo, origen, rank):
        """
        Algoritmo para obtener page-rank personalizado en base a una canción inicial.
        """
        cola = deque([origen])
        dist = {origen: 0}

        while cola:
            v = cola.popleft()
            for a in self.user_canciones.adyacentes(v):
                if a not in rank:
                    dist[a] = dist[v] + 1
                    rank[a] = rank.get(a, 0) + rank[v] / self.user_canciones.cantidad_adyacentes(v)
                    if dist[a] < largo * 2:
                        cola.append(a)

    def _imprimir_ciclo(self, ciclo):
        """
        Imprime un ciclo de canciones en el formato correcto.
        """
        for cancion in ciclo:
            print(f'{cancion} --> ', end="")
        print(ciclo[0])

    def _chequear_existencia_cancion(self, cancion):
        """
        Válida la existencia de una canción en el sistema.
        """
        if cancion not in self.canciones:
            print(MSG_CANCION_NO_EXISTE)
            return False
        return True

    def _chequear_existencia_canciones(self, cancion1, cancion2):
        """
        Válida la existencia de dos canciones en el sistema.
        """
        if cancion1 not in self.canciones or cancion2 not in self.canciones:
            print(MSG_CANCIONES_NO_EXISTEN)
            return False
        return True

    def _existe_proyeccion(self):
        """
        Chequea que el grafo de proyección exista.
        """
        return self.proyeccion is not None

    def _hallar_proyeccion(self):
        """
        Halla la proyección del grafo usuarios-canciones.
        """
        self.proyeccion = Grafo(es_dirigido=False, vertices=list(self.canciones))

        for v in self.canciones:
            for w in self.user_canciones.adyacentes(v):
                for a in self.user_canciones.adyacentes(w):
                    if a != v and not self.proyeccion.estan_unidos(v, a):
                        self.proyeccion.agregar_arista(v, a)

# Funciones auxiliares genéricas (podrían funcionar independientemente de la biblioteca):
def _bfs(origen, grafo, visitados=None, padres=None, destino=None, saltos=None, contador = None):
    """
    Realiza un recorrido BFS en un grafo, permitiendo obtener disinta info. dependiendo de los parámetros pasados.
    """
    if visitados is None:
        visitados = {origen}
    if padres is None:
        padres = {origen: None}
    dist = {origen: 0}
    cola = deque([origen])

    while cola:
        v = cola.popleft()
        if destino is not None and v == destino:
            break
        if saltos is not None and dist[v] == saltos:
            contador[0] += 1
            continue
        for a in grafo.adyacentes(v):
            if a not in visitados:
                visitados.add(a)
                padres[a] = v
                dist[a] = dist[v] + 1
                cola.append(a)

def _reconstruir_ciclo(fin, origen, padres):
    """
    Dados dos vértices (origen y fin) y un diccionario de padres, reconstruye un ciclo.
    """
    ciclo = []
    actual = fin
    while actual != origen:
        ciclo.append(actual)
        actual = padres[actual]
    ciclo.append(origen)
    return ciclo[::-1]

def _page_rank(grafo):
    """
    Permite definir un ranking, o importancia, de los distintos vértices dentro de una red.
    """
    n = len(grafo.obtener_vertices())
    d = CO_AMORTIGUACION

    page_rank = {}
    for v in grafo:
        page_rank[v] = RANK_INICIAL / n

    for _ in range(ITERACIONES):
        suma_total = 0
        nuevo = {}
        for v in grafo:
            sumatoria = 0 
            for a in grafo.adyacentes(v):
                sumatoria += page_rank[a] / grafo.cantidad_adyacentes(a)
            nuevo[v] = (1 - d) / n + d * sumatoria
            suma_total += nuevo[v]

        for v in grafo:
            nuevo[v] /= suma_total
        page_rank = nuevo

    return page_rank