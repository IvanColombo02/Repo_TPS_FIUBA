package lista

const (
	_MENSAJE_ERROR        = "La lista esta vacia"
	_MENSAJE_FIN_ITERADOR = "El iterador termino de iterar"
)

type nodoLista[T any] struct {
	dato      T
	siguiente *nodoLista[T]
}

type listaEnlazada[T any] struct {
	primero *nodoLista[T]
	ultimo  *nodoLista[T]
	largo   int
}

type iterListaEnlazada[T any] struct {
	actual   *nodoLista[T]
	anterior *nodoLista[T]
	lista    *listaEnlazada[T]
}

func CrearListaEnlazada[T any]() Lista[T] {
	// Crea una lista enlazada
	return &listaEnlazada[T]{nil, nil, 0}
}

func CrearNodoLista[T any](elem T, proximo *nodoLista[T]) *nodoLista[T] {
	// Crea un nodo con el dato pasado por parametro
	return &nodoLista[T]{elem, proximo}
}

func (lista *listaEnlazada[T]) InsertarPrimero(elem T) {
	nuevoNodo := CrearNodoLista(elem, lista.primero)
	if lista.largo == 0 {
		lista.ultimo = nuevoNodo
	}
	lista.primero = nuevoNodo
	lista.largo++
}

func (lista *listaEnlazada[T]) InsertarUltimo(elem T) {
	nuevoNodo := CrearNodoLista(elem, nil)
	if lista.largo == 0 {
		lista.primero = nuevoNodo
	} else {
		lista.ultimo.siguiente = nuevoNodo
	}
	lista.ultimo = nuevoNodo
	lista.largo++
}

func (lista listaEnlazada[T]) EstaVacia() bool {
	return lista.largo == 0
}

func (lista *listaEnlazada[T]) BorrarPrimero() T {
	if lista.largo == 0 {
		lanzarError()
	}
	datoActual := lista.primero.dato
	lista.primero = lista.primero.siguiente
	if lista.primero == nil {
		lista.ultimo = nil
	}
	lista.largo--
	return datoActual
}

func (lista listaEnlazada[T]) VerPrimero() T {
	if lista.largo == 0 {
		lanzarError()
	}
	return lista.primero.dato
}

func (lista listaEnlazada[T]) VerUltimo() T {
	if lista.largo == 0 {
		lanzarError()
	}
	return lista.ultimo.dato
}

func (lista listaEnlazada[T]) Largo() int {
	return lista.largo
}

func (lista *listaEnlazada[T]) Iterar(visitar func(T) bool) {
	actual := lista.primero
	for actual != nil {
		if !visitar(actual.dato) {
			break
		}
		actual = actual.siguiente
	}
}

func (lista *listaEnlazada[T]) Iterador() IteradorLista[T] {
	return &iterListaEnlazada[T]{lista.primero, nil, lista}
}

func (iter iterListaEnlazada[T]) VerActual() T {
	if iter.actual == nil {
		lanzarError()
	}
	return iter.actual.dato
}

func (iter iterListaEnlazada[T]) HaySiguiente() bool {
	return iter.actual != nil
}

func (iter *iterListaEnlazada[T]) Siguiente() {
	iter.chequearFinIteracion()
	actual := iter.actual
	iter.actual = actual.siguiente
	iter.anterior = actual
}

func (iter *iterListaEnlazada[T]) Insertar(elem T) {
	nuevoNodo := CrearNodoLista(elem, iter.actual)
	if iter.anterior == nil {
		iter.lista.primero = nuevoNodo
	} else {
		iter.anterior.siguiente = nuevoNodo
	}
	if iter.actual == nil {
		iter.lista.ultimo = nuevoNodo
	}
	iter.actual = nuevoNodo
	iter.lista.largo++
}

func (iter *iterListaEnlazada[T]) Borrar() T {
	iter.chequearFinIteracion()
	if iter.anterior == nil {
		iter.lista.primero = iter.actual.siguiente
	} else {
		iter.anterior.siguiente = iter.actual.siguiente
	}
	if iter.actual.siguiente == nil {
		iter.lista.ultimo = iter.anterior
	}
	elem := iter.actual.dato
	iter.actual = iter.actual.siguiente
	iter.lista.largo--
	return elem
}

func lanzarError() {
	// Lanza un panic
	panic(_MENSAJE_ERROR)
}

func (iter *iterListaEnlazada[T]) chequearFinIteracion() {
	// Chequea si el actual esta en el final de la iteracion
	if iter.actual == nil {
		panic(_MENSAJE_FIN_ITERADOR)
	}
}
