"""
NOTA IMPORTANTE:
        Solo desarrollamos las primitivas pertinentes para el funcionamiento
    de nuestro algoritmo.
"""

class Nodo:
    def __init__(self, valor):
        """
        Crea un nodo. Uno nodo representa el encapsulamiento
        de un elemento en una lista doblemente enlazada.
        """
        self.__valor = valor
        self.__anterior = None
        self.__siguiente = None

    def get_valor(self):
        """
        Retorna el valor contenido por el nodo.
        """
        return self.__valor
    
    def _get_anterior(self):
        """
        Retorna el nodo anterior.
        """
        return self.__anterior
    
    def _get_siguiente(self):
        """
        Retorna el nodo siguiente.
        """
        return self.__siguiente
    
    def _set_anterior(self, anterior):
        """
        Recibe un nodo y lo setea como anterior al actual.
        """
        self.__anterior = anterior
    
    def _set_siguiente(self, siguiente):
        """
        Recibe un nodo y lo setea como siguiente al actual.
        """
        self.__siguiente = siguiente

class ListaDoblementeEnlazada:
    def __init__(self, iniciales = []):
        """
        Crea una lista doblemente enlazada. 'iniciales' permite
        inicializarla con valores ya definidos.
        """
        self.__primero = None
        self.__ultimo = None

        for elem in iniciales:
            self.agregar_ultimo(elem)

    def agregar_ultimo(self, valor):
        """
        Agrega un elemento al final de la lista.
        """
        nodo = Nodo(valor)
        if not self.__primero:
            self.__primero = self.__ultimo = nodo
        else:
            self.__ultimo._set_siguiente(nodo)
            nodo._set_anterior(self.__ultimo)
            self.__ultimo = nodo

    def borrar_elemento(self, nodo):
        """
        Recibe un nodo (es decir, el encapsulamiento de cada
        elemento) y lo borra de la lista.
        """
        if nodo == self.__primero:
            self.__primero = nodo._get_siguiente()
            if self.__primero is not None:
                self.__primero._set_anterior(None)

        elif nodo == self.__ultimo:
            self.__ultimo = nodo._get_anterior()
            if self.__ultimo is not None:
                self.__ultimo._set_siguiente(None)
            
        else:
            anterior = nodo._get_anterior()
            siguiente = nodo._get_siguiente()
            anterior._set_siguiente(siguiente)
            siguiente._set_anterior(anterior)

    def __iter__(self):
        """
        Itera los nodos que conforman los elementos de la lista.
        """
        actual = self.__primero
        while actual:
            yield actual # Retorna el nodo actual sin finalizar la ejecución de la función.
            actual = actual._get_siguiente()