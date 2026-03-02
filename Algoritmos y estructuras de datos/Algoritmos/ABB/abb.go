package abb

import (
	pila "Algoritmos/PilaDinamica"
)

type funcCmp[K comparable] func(K, K) int

type nodoAbb[K comparable, V any] struct {
	izquierdo *nodoAbb[K, V]
	derecho   *nodoAbb[K, V]
	clave     K
	dato      V
}

type abb[K comparable, V any] struct {
	raiz     *nodoAbb[K, V]
	cantidad int
	cmp      funcCmp[K]
}

type iterador[K comparable, V any] struct {
	desde *K
	hasta *K
	pila  pila.Pila[*nodoAbb[K, V]]
	cmp   funcCmp[K]
}

func CrearABB[K comparable, V any](funcion_cmp func(K, K) int) DiccionarioOrdenado[K, V] {
	return &abb[K, V]{raiz: nil, cantidad: 0, cmp: funcion_cmp}
}

func (arbol *abb[K, V]) Guardar(clave K, dato V) {
	buscado, padre := arbol.buscarNodo(arbol.raiz, clave)

	if buscado != nil {
		buscado.dato = dato
		return
	}

	arbol.guardarNuevoNodo(clave, dato, padre)
	arbol.cantidad++
}

// guardarNuevoNodo guarda un nuevo nodo en el árbol, debe recibir la clave y el dato a guardar,
// así como también su nodo padre.
func (arbol *abb[K, V]) guardarNuevoNodo(clave K, dato V, padre *nodoAbb[K, V]) {
	nuevoNodo := crearNodo(clave, dato)
	if padre == nil {
		arbol.raiz = nuevoNodo
	} else {
		comparacion := arbol.cmp(clave, padre.clave)
		if comparacion < 0 {
			padre.izquierdo = nuevoNodo
		} else {
			padre.derecho = nuevoNodo
		}
	}
}

func (arbol abb[K, V]) Pertenece(clave K) bool {
	nodo, _ := arbol.buscarNodo(arbol.raiz, clave)
	return nodo != nil
}
func (arbol abb[K, V]) Cantidad() int {
	return arbol.cantidad
}

func (arbol abb[K, V]) Obtener(clave K) V {
	nodo, _ := arbol.buscarNodo(arbol.raiz, clave)
	chequearPertenencia(nodo)
	return nodo.dato
}

func (arbol *abb[K, V]) Borrar(clave K) V {
	buscado, padre := arbol.buscarNodo(arbol.raiz, clave)
	chequearPertenencia(buscado)
	arbol.cantidad--

	if buscado.izquierdo == nil && buscado.derecho == nil {
		arbol.borrarConSinHijos(buscado, padre, nil)
	} else if buscado.izquierdo != nil && buscado.derecho == nil {
		arbol.borrarConSinHijos(buscado, padre, buscado.izquierdo)
	} else if buscado.izquierdo == nil && buscado.derecho != nil {
		arbol.borrarConSinHijos(buscado, padre, buscado.derecho)
	} else {
		arbol.borrarConDosHijos(buscado)
	}

	return buscado.dato
}

// borrarConSinHijos borra un nodo sin hijos, debe recibir el nodo a borrar y su padre.
func (arbol *abb[K, V]) borrarConSinHijos(buscado, padre, hijo *nodoAbb[K, V]) {
	if padre == nil {
		arbol.raiz = hijo
	} else if padre.izquierdo != nil && padre.izquierdo.clave == buscado.clave {
		padre.izquierdo = hijo
	} else {
		padre.derecho = hijo
	}
}

// borrarConDosHijos borra un nodo con dos hijos, debe recibir el nodo a borrar.
func (arbol abb[K, V]) borrarConDosHijos(buscado *nodoAbb[K, V]) {
	reemplazante, reempadre := arbol.buscarReemplazante(buscado)

	if reemplazante.derecho != nil {
		arbol.borrarConSinHijos(reemplazante, reempadre, reemplazante.derecho)
	} else {
		arbol.borrarConSinHijos(reemplazante, reempadre, nil)
	}

	buscado.clave = reemplazante.clave
	buscado.dato = reemplazante.dato
}

// buscarReemplazante busca el nodo reemplazante de un nodo con dos hijos, devuelve el nodo reemplazante y su padre.
func (arbol *abb[K, V]) buscarReemplazante(nodo *nodoAbb[K, V]) (*nodoAbb[K, V], *nodoAbb[K, V]) {
	padre := nodo
	reemplazante := nodo.derecho
	for reemplazante != nil && reemplazante.izquierdo != nil {
		padre = reemplazante
		reemplazante = reemplazante.izquierdo
	}

	return reemplazante, padre
}

func (arbol abb[K, V]) Iterar(visitar func(K, V) bool) {
	arbol.iterarNodoRango(arbol.raiz, nil, nil, visitar)
}

func (arbol abb[K, V]) IterarRango(desde *K, hasta *K, visitar func(K, V) bool) {
	arbol.iterarNodoRango(arbol.raiz, desde, hasta, visitar)
}

func (arbol abb[K, V]) iterarNodoRango(nodo *nodoAbb[K, V], desde, hasta *K, visitar func(K, V) bool) bool {
	if nodo == nil {
		return true
	}

	if desde == nil || arbol.cmp(nodo.clave, *desde) > 0 {
		if !arbol.iterarNodoRango(nodo.izquierdo, desde, hasta, visitar) {
			return false
		}
	}

	if (desde == nil || arbol.cmp(nodo.clave, *desde) >= 0) && (hasta == nil || arbol.cmp(nodo.clave, *hasta) <= 0) {
		if !visitar(nodo.clave, nodo.dato) {
			return false
		}
	}

	if hasta == nil || arbol.cmp(nodo.clave, *hasta) < 0 {
		if !arbol.iterarNodoRango(nodo.derecho, desde, hasta, visitar) {
			return false
		}
	}

	return true
}

func (arbol abb[K, V]) Iterador() IterDiccionario[K, V] {
	return arbol.IteradorRango(nil, nil)
}

func (arbol abb[K, V]) IteradorRango(desde *K, hasta *K) IterDiccionario[K, V] {
	iter := &iterador[K, V]{desde, hasta, pila.CrearPilaDinamica[*nodoAbb[K, V]](), arbol.cmp}
	iter.ApilarRango(arbol.raiz)
	return iter
}

// buscarNodo busca un nodo en el árbol, devuelve un puntero al nodo buscado y a su nodo padre.
func (arbol abb[K, V]) buscarNodo(actual *nodoAbb[K, V], clave K) (*nodoAbb[K, V], *nodoAbb[K, V]) {
	if actual == nil {
		return nil, nil
	}

	comparacion := arbol.cmp(clave, actual.clave)
	if comparacion == 0 {
		return actual, nil
	}

	var buscado, padre *nodoAbb[K, V]
	if comparacion < 0 {
		buscado, padre = arbol.buscarNodo(actual.izquierdo, clave)
	} else {
		buscado, padre = arbol.buscarNodo(actual.derecho, clave)
	}

	if padre == nil {
		return buscado, actual
	}
	return buscado, padre
}

// crearNodo crea un nuevo nodo.
func crearNodo[K comparable, V any](clave K, dato V) *nodoAbb[K, V] {
	return &nodoAbb[K, V]{nil, nil, clave, dato}
}

// chequearPertenencia chequea entra en pánico con el mensaje 'La clave no pertenece al diccionario'
// en caso de que un nodo dado por parámetro sea nulo.
func chequearPertenencia[K comparable, V any](nodo *nodoAbb[K, V]) {
	if nodo == nil {
		panic("La clave no pertenece al diccionario")
	}
}

// PRIMITIVAS DEL ITERADOR EXTERNO:

func (iter iterador[K, V]) VerActual() (K, V) {
	iter.chequearFinIteracion()
	return iter.pila.VerTope().clave, iter.pila.VerTope().dato
}

func (iter iterador[K, V]) HaySiguiente() bool {
	return !iter.pila.EstaVacia()
}

func (iter *iterador[K, V]) Siguiente() {
	iter.chequearFinIteracion()
	actual := iter.pila.Desapilar()
	iter.ApilarRango(actual.derecho)
}

// ApilarRango apila todos los nodos sucesivos hijos izquierdos de un nodo cuya clave
// se encuentre entre un rango predefinido. Si el nodo es menor al rango desde, se pasa
// a apilar su subrama derecha.
func (iter iterador[K, V]) ApilarRango(nodo *nodoAbb[K, V]) {
	if nodo == nil {
		return
	}

	if iter.desde == nil || iter.cmp(nodo.clave, *iter.desde) >= 0 {
		if iter.hasta == nil || iter.cmp(nodo.clave, *iter.hasta) <= 0 {
			iter.pila.Apilar(nodo)
		}
		iter.ApilarRango(nodo.izquierdo)
		return
	}

	iter.ApilarRango(nodo.derecho)
}

// chequearFinIteracion levanta un panic en caso de que el diccionario se halla iterado en
// su totalidad.
func (iterador *iterador[K, V]) chequearFinIteracion() {
	if !iterador.HaySiguiente() {
		panic("El iterador termino de iterar")
	}
}
