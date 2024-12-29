package heap

const (
	_TAM_INICIAL        = 10
	_FACTOR_REDIMENSION = 2
	_FACTOR_ACHICAR     = 4
)

type ColaConPrioridad[T any] struct {
	datos []T
	cant  int
	cmp   func(T, T) int
}

func CrearHeap[T any](funcCmp func(T, T) int) ColaPrioridad[T] {
	return &ColaConPrioridad[T]{make([]T, _TAM_INICIAL), 0, funcCmp}
}

func CrearHeapArr[T any](arreglo []T, funcCmp func(T, T) int) ColaPrioridad[T] {
	heapify(arreglo, funcCmp)
	return &ColaConPrioridad[T]{arreglo, len(arreglo), funcCmp}
}

func (heap *ColaConPrioridad[T]) Encolar(elem T) {
	if heap.cant == cap(heap.datos) {
		nuevoTam := cap(heap.datos) * _FACTOR_REDIMENSION
		heap.redimensionar(nuevoTam)
	}
	heap.datos[heap.cant] = elem
	heap.cant++
	upheap(heap.datos, heap.cmp, heap.cant-1)
}

func (heap *ColaConPrioridad[T]) Desencolar() T {
	heap.chequearHeapVacio()
	elem := heap.datos[0]
	heap.datos[0] = heap.datos[heap.cant-1]
	heap.cant--
	downheap(heap.datos, heap.cmp, heap.cant, 0)
	if heap.cant*_FACTOR_ACHICAR <= cap(heap.datos) {
		nuevoTam := cap(heap.datos) / _FACTOR_REDIMENSION
		heap.redimensionar(nuevoTam)
	}
	return elem
}

func (heap ColaConPrioridad[T]) Cantidad() int {
	return heap.cant
}

func (heap ColaConPrioridad[T]) EstaVacia() bool {
	return heap.cant == 0
}

func (heap ColaConPrioridad[T]) VerMax() T {
	heap.chequearHeapVacio()
	return heap.datos[0]
}

func HeapSort[T any](elementos []T, funcCmp func(T, T) int) {
	heapify(elementos, funcCmp)
	indiceFin := 1
	cantidad := len(elementos)
	for i := cantidad - 1; i >= 0; i-- {
		swap(&elementos[0], &elementos[i])
		downheap(elementos, funcCmp, cantidad-indiceFin, 0)
		indiceFin++
	}
}

func upheap[T any](elementos []T, funcCmp func(T, T) int, indice int) {
	padre := (indice - 1) / 2
	if funcCmp(elementos[padre], elementos[indice]) < 0 {
		swap(&elementos[padre], &elementos[indice])
		upheap(elementos, funcCmp, padre)
	}
}

func downheap[T any](elementos []T, funcCmp func(T, T) int, cant, indice int) {
	hijoIzq := 2*indice + 1
	hijoDer := 2*indice + 2
	hijoMayor := chequearHijoMayor(elementos, funcCmp, cant, hijoIzq, hijoDer)
	if hijoMayor == -1 {
		return
	}
	if funcCmp(elementos[indice], elementos[hijoMayor]) < 0 {
		swap(&elementos[indice], &elementos[hijoMayor])
		downheap(elementos, funcCmp, cant, hijoMayor)
	}

}

func heapify[T any](elementos []T, funcCmp func(T, T) int) {
	for i := (len(elementos) - 1) / 2; i >= 0; i-- {
		downheap(elementos, funcCmp, len(elementos), i)
	}
}

func (heap *ColaConPrioridad[T]) redimensionar(nuevoTam int) {
	nuevosDatos := make([]T, nuevoTam)
	copy(nuevosDatos, heap.datos)
	heap.datos = nuevosDatos
}

func (heap *ColaConPrioridad[T]) chequearHeapVacio() {
	if heap.cant == 0 {
		panic("La cola esta vacia")
	}
}

func swap[T any](x *T, y *T) {
	*x, *y = *y, *x
}

func chequearHijoMayor[T any](elementos []T, funcCmp func(T, T) int, cant, indiceIzq, indiceDer int) int {
	if indiceIzq >= cant {
		return -1
	}
	if indiceDer >= cant || funcCmp(elementos[indiceIzq], elementos[indiceDer]) >= 0 {
		return indiceIzq
	}
	return indiceDer
}
