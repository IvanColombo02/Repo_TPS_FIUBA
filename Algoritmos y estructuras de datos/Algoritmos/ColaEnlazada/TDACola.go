package cola

import "fmt"

const (
	_MENSAJE_ERROR = "La cola esta vacia"
)

type nodoCola[T any] struct {
	dato T
	prox *nodoCola[T]
}

type colaEnlazada[T any] struct {
	primero *nodoCola[T]
	ultimo  *nodoCola[T]
}

func CrearColaEnlazada[T any]() Cola[T] {
	// Crea una cola enlazada
	return &colaEnlazada[T]{nil, nil}
}

func nodoCrear[T any](dato T) *nodoCola[T] {
	// Crea un nodo con el dato pasado por parametro
	return &nodoCola[T]{dato, nil}
}

func (cola *colaEnlazada[T]) Encolar(elem T) {
	nodo := nodoCrear(elem)
	if cola.primero == nil {
		cola.primero = nodo
	} else {
		cola.ultimo.prox = nodo
	}
	cola.ultimo = nodo
}

func (cola *colaEnlazada[T]) Desencolar() T {
	if cola.primero == nil {
		lanzarError()
	}
	datoActual := cola.primero.dato
	cola.primero = cola.primero.prox
	if cola.primero == nil {
		cola.ultimo = nil
	}
	return datoActual
}

func (cola colaEnlazada[T]) VerPrimero() T {
	if cola.primero == nil {
		lanzarError()
	}
	return cola.primero.dato
}

func (cola colaEnlazada[T]) EstaVacia() bool {
	return cola.primero == nil
}

func (cola colaEnlazada[T]) Representar() {
	actual := cola.primero
	for actual.prox != nil {
		fmt.Printf("[%v]-", actual.dato)
		actual = actual.prox
	}
	fmt.Printf("[%v]\n", actual.dato)
}

func lanzarError() {
	panic(_MENSAJE_ERROR)
}
