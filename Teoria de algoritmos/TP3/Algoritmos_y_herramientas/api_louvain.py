from grafo import Grafo
import random

def louvain(grafo, k):
    """
    Obtiene k comunidades disjuntas de vértices pertenecientes a
    un grafo.
    """
    # Asigna cada vértice a una comunidad inicial:
    comunidad = _asignar_comunidades(grafo)

    # Crea un diccionario para guardar las referencias originales de las
    # comunidades de los vértices:
    comunidades_originales = _asignar_comunidades_originales_inicial(grafo)

    deberia_seguir = True
    # Corta cuando:
    # 'deberia seguir' es False, porque las comunidades ya convergieron.
    # Ya se tiene la cantidad de comunidades buscada.
    while len(comunidades_originales) > k and deberia_seguir:
        # FASE 1:
        # Agrupa los vértices en comunidades que maximicen la modularidad:
        deberia_seguir = _agrupar_nodos(grafo, comunidad)

        # FASE 2:
        # Reemplaza los vértices pertenecientes a mismas comunidades con
        # nuevos "supervértices" y sus respectivas conexiones:
        grafo, comunidades_originales = _crear_super_comunidades(grafo, comunidad, comunidades_originales)
        # Asigna comunidades a estos nuevos vértices:
        comunidad = _asignar_comunidades(grafo)
    
    # Si las comunidades convergieron antes de llegar a k, se mergean
    # las más cercanas hasta llega:
    if len(comunidades_originales) > k:
        _mergear_comunidades(grafo, k, comunidades_originales)

    # Formatea correctamente la solución y la retorna:
    return _transformar_comunidades_a_lista(comunidades_originales, k)

# -----------------------------------------------------
# Funciones principales de fases 1 y 2:

def _agrupar_nodos(grafo, comunidad):
    """
    Agrupa los vértices de manera que se maximice la modularidad.
    Corta cuando ya no puede ser mejorada.
    """

    # Sumade pesos de las aristas:
    dos_m = _calcular_dos_m(grafo)

    deberia_seguir = False
    hubo_cambios = True

    while hubo_cambios:
        hubo_cambios = False
        vertices = grafo.obtener_vertices()
        # Evita iterar los vértices siempre en el mismo orden:
        random.shuffle(vertices)
        
        for v in vertices:
            # Primero se calcula ΔQ(v -> D) como si el vértice no estuviese allí.

            # Calcula la suma de pesos internos de cada comunidad: 
            sum_in = _calcular_suma_pesos_internos_comunidades(grafo, comunidad, v)

            # Calcula la suma de pesos totales cada comunidad:
            sum_tot = _calcular_suma_pesos_comunidades(grafo, comunidad, v)

            # Suma de pesos entre el vértice y su comunidad (haciendo como si no
            # estuviese en ella):
            k_i_in = _calcular_suma_pesos_v_y_comu(grafo, v, comunidad[v], comunidad)

            # Suma de pesos del vértice:
            k_i = _calcular_suma_pesos_v(grafo, v)

            # Si era el único vértice en su comunidad no se agregó esta previamente
            # al diccionario, entonces:
            sum_in[comunidad[v]] = sum_in.get(comunidad[v], 0)
            sum_tot[comunidad[v]] = sum_tot.get(comunidad[v], 0)

            # Calculo de ΔQ(v -> D)
            delta_q_d = (((sum_in[comunidad[v]] + k_i_in) / dos_m) - (((sum_tot[comunidad[v]] + k_i) / dos_m)**2)) - ((sum_in[comunidad[v]] / dos_m) - ((sum_tot[comunidad[v]] / dos_m)**2) - ((k_i / dos_m)**2))

            visitadas = {comunidad[v]} # La modularidad en la comunidad donde ya está no me interesa.
            mejor = 0
            comu_mejor = None
            for a in grafo.adyacentes(v):
                comu_vecina = comunidad[a]
                if comu_vecina in visitadas:
                    continue
                else:
                    visitadas.add(comu_vecina)

                k_i_in = _calcular_suma_pesos_v_y_comu(grafo, v, comu_vecina, comunidad)
                # Calcula ΔQ(v -> C):
                delta_q_c = (((sum_in[comu_vecina] + k_i_in) / dos_m) - (((sum_tot[comu_vecina] + k_i) / dos_m)**2)) - ((sum_in[comu_vecina] / dos_m) - ((sum_tot[comu_vecina] / dos_m)**2) - ((k_i / dos_m)**2))
                # Calcula el incremento de modularidad, ΔQ(v -> C) - ΔQ(v -> D):
                incremento = delta_q_c - delta_q_d
              
                if incremento > mejor:
                    mejor = incremento
                    comu_mejor = comu_vecina
                    hubo_cambios = True
                    deberia_seguir = True
            
            if comu_mejor is not None:
                comunidad[v] = comu_mejor

    return deberia_seguir

def _crear_super_comunidades(grafo, comunidad, comunidades_originales):
    """
    Se crea un nuevo grafo donde cada comunidad resultando ahora es un
    nuevo supervértice. Los pesos de las aristas están dados: en el caso
    de los bucles, por la suma de pesos entre aristas de los vértices
    anteriores de una misma comunidad, y, en el caso de las aristas entre
    supervértices, por los pesos de las aristas que conectaban
    anteriomente vértices entre ellas.
    """
    nuevo_grafo = Grafo(False)
    visitados = set()
    for v in grafo:
        visitados.add(v)
        for a in grafo.adyacentes(v):
            x = comunidad[v]
            y = comunidad[a]

            if a in visitados and x != y: # Las aristas entre distintas comunidades solo se suman una vez.
                continue

            if x not in nuevo_grafo:
                nuevo_grafo.agregar_vertice(x)
            if not y in nuevo_grafo:
                nuevo_grafo.agregar_vertice(y)
            
            if not nuevo_grafo.estan_unidos(x, y):
                nuevo_grafo.agregar_arista(x, y, grafo.peso_arista(v, a))
            else:
                nuevo_grafo.actualizar_peso(x, y, nuevo_grafo.peso_arista(x, y) + grafo.peso_arista(v, a))
    
    return nuevo_grafo, _generar_nuevas_comunidades(comunidad, comunidades_originales)

def _generar_nuevas_comunidades(comunidad, comunidades_originales):
    """
    A los nuevos vértices se les asignan los vértices originales
    que engloban.
    """
    nuevas_comunidades = {}
    for v, comu in comunidad.items():
        if comu not in nuevas_comunidades:
            nuevas_comunidades[comu] = set()
        nuevas_comunidades[comu].update(comunidades_originales[v])
    return nuevas_comunidades

# -----------------------------------------------------
# Funciones encargadas de calculos necesarios para la modularidad:

def _calcular_dos_m(grafo):
    dos_m = 0
    for v in grafo:
        for a in grafo.adyacentes(v):
            dos_m += grafo.peso_arista(v, a)
    return dos_m

def _calcular_suma_pesos_v(grafo, v):
    sumatoria = 0
    for a in grafo.adyacentes(v):
        sumatoria += grafo.peso_arista(v, a)
    return sumatoria

def _calcular_suma_pesos_v_y_comu(grafo, v, comu, comunidades):
    sumatoria = 0
    for a in grafo.adyacentes(v):
        if comunidades[a] != comu:
            continue
        sumatoria += grafo.peso_arista(v, a)
    return sumatoria

def _calcular_suma_pesos_comunidades(grafo, comunidad, w):
    sum_tot = {}
    visitados = set()
    for v in grafo:
        # Hago como si 'w' no fuese parte de ninguna comunidad:
        if v == w:
            continue
        visitados.add(v)
        suma_pesos = 0
        for a in grafo.adyacentes(v):
            if a in visitados:
                continue
            suma_pesos += grafo.peso_arista(v, a)
        sum_tot[comunidad[v]] = sum_tot.get(comunidad[v], 0) + suma_pesos
    return sum_tot

def _calcular_suma_pesos_internos_comunidades(grafo, comunidad, w):
    sum_in = {}
    visitados = set()
    for v in grafo:
        if v == w:
            continue

        visitados.add(v)
        suma_pesos = 0
        for a in grafo.adyacentes(v):
            if comunidad[v] != comunidad[a] or a in visitados or a == w:
                continue
            suma_pesos += grafo.peso_arista(v, a)
        sum_in[comunidad[v]] = sum_in.get(comunidad[v], 0) + suma_pesos
    return sum_in

# -----------------------------------------------------
# Funciones de asignación auxiliares:

def _asignar_comunidades(grafo):
    return {v: i for i, v in enumerate(grafo)}

def _asignar_comunidades_originales_inicial(grafo):
    return {v: {v} for v in grafo}

# -----------------------------------------------------
# Funciones de procesamiento final (mergeo y formateo):

def _mergear_comunidades(grafo, k, comunidades_originales):
    while len(comunidades_originales) > k:
        v = grafo.vertice_aleatorio()
        if v not in comunidades_originales:
            continue

        max_ady, max_peso_ady = None, -1
        for a in grafo.adyacentes(v):
            if a not in comunidades_originales:
                continue
            if a != v and grafo.peso_arista(v, a) > max_peso_ady:
                max_ady, max_peso_ady = a, grafo.peso_arista(v, a)
        
        if not max_ady:
            continue
        
        comunidades_originales[v].update(comunidades_originales[max_ady])
        comunidades_originales.pop(max_ady)

def _transformar_comunidades_a_lista(comunidades, k):
    lista_comunidades = []
    for comunidad in comunidades.values():
        lista_comunidades.append(list(comunidad))
    while len(lista_comunidades) < k:
        lista_comunidades.append([])
    return lista_comunidades