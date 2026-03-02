package pila

const (
	_TAM_INICIAL        = 4
	_FACTOR_REDIMENSION = 2
	_FACTOR_REDUCCION   = 4
	_MENSAJE_ERROR      = "La pila esta vacia"
)

type pilaDinamica[T any] struct {
	datos    []T
	cantidad int
}

func CrearPilaDinamica[T any]() Pila[T] {
	datos := make([]T, _TAM_INICIAL)
	return &pilaDinamica[T]{datos, 0}
}

func (pila *pilaDinamica[T]) Apilar(elem T) {
	if pila.cantidad == cap(pila.datos) {
		nuevoTam := cap(pila.datos) * _FACTOR_REDIMENSION
		pila.redimensionar(nuevoTam)
	}
	pila.datos[pila.cantidad] = elem
	pila.cantidad++
}

func (pila *pilaDinamica[T]) Desapilar() T {
	if pila.cantidad == 0 {
		lanzarError()
	}
	elem := pila.datos[pila.cantidad-1]
	pila.cantidad--
	if pila.cantidad*_FACTOR_REDUCCION <= cap(pila.datos) {
		nuevoTam := cap(pila.datos) / _FACTOR_REDIMENSION
		pila.redimensionar(nuevoTam)
	}
	return elem
}

func (pila pilaDinamica[T]) EstaVacia() bool {
	return pila.cantidad == 0
}

func (pila pilaDinamica[T]) VerTope() T {
	if pila.cantidad == 0 {
		lanzarError()
	}
	return pila.datos[pila.cantidad-1]
}

func (pila *pilaDinamica[T]) redimensionar(nuevoTam int) {
	nuevosDatos := make([]T, nuevoTam)
	copy(nuevosDatos, pila.datos)
	pila.datos = nuevosDatos
}

func lanzarError() {
	panic(_MENSAJE_ERROR)
}
