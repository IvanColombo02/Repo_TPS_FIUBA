package cola

const (
	_TAM_INICIAL        = 4
	_FACTOR_REDIMENSION = 2
	_FACTOR_REDUCCION   = 4
	_MENSAJE_ERR        = "La cola esta vacia"
)

type colaDinamica[T any] struct {
	datos    []T
	cantidad int
}

func CrearColaDinamica[T any]() ColaDinamica[T] {
	return &colaDinamica[T]{make([]T, _TAM_INICIAL), 0}
}

func (cola *colaDinamica[T]) Encolar(elem T) {
	if cola.cantidad == cap(cola.datos) {
		nuevoTam := cola.cantidad * _FACTOR_REDIMENSION
		cola.redimensionar(nuevoTam)
	}
	cola.datos[cola.cantidad] = elem
	cola.cantidad++
}

func (cola *colaDinamica[T]) Desencolar() T {
	if cola.cantidad == 0 {
		devolverError()
	}
	dato := cola.datos[0]
	NuevosDatos := make([]T, cola.cantidad-1)
	for i := 1; i < cola.cantidad; i++ {
		NuevosDatos[i-1] = cola.datos[i]
	}
	cola.datos = NuevosDatos
	cola.cantidad--
	if cola.cantidad*_FACTOR_REDUCCION <= cap(cola.datos) {
		nuevoTam := cola.cantidad / _FACTOR_REDIMENSION
		cola.redimensionar(nuevoTam)
	}
	return dato

}

func (cola colaDinamica[T]) EstaVacia() bool {
	return cola.cantidad == 0
}

func (cola colaDinamica[T]) VerPrimero() T {
	if cola.cantidad == 0 {
		devolverError()
	}
	return cola.datos[0]
}

func (cola *colaDinamica[T]) redimensionar(nuevoTam int) {
	nuevosDatos := make([]T, nuevoTam)
	copy(nuevosDatos, cola.datos)
	cola.datos = nuevosDatos
}
func devolverError() {
	panic(_MENSAJE_ERR)
}
