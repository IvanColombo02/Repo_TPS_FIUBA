package lista

type Lista[T any] interface {

	// EstaVacia devuelve verdadero si la lista no tiene elementos, false en caso contrario.
	EstaVacia() bool

	// InsertarPrimero agrega un nuevo elemento al principio de la lista.
	InsertarPrimero(T)

	// InsertarUltimo agrega un nuevo elemento al final de la lista.
	InsertarUltimo(T)

	// BorrarPrimero saca el primer elemento de la lista. Si la lista tiene elementos, se quita el primero de la misma,
	// y se devuelve ese valor. Si está vacía, entra en pánico con un mensaje "La lista esta vacia".
	BorrarPrimero() T

	// VerPrimero obtiene el valor del primer elemento de la lista. Si está vacía, entra en pánico con un mensaje
	// "La lista esta vacia".
	VerPrimero() T

	// VerUltimo obtiene el valor del último elemento de la lista. Si está vacía, entra en pánico con un mensaje
	// "La lista esta vacia".
	VerUltimo() T

	// Largo obtiene la cantidad de elementos presentes en la lista.
	Largo() int

	// Iterar recibe una función 'visitar' y recorre los elementos de la lista aplicandoles la función hasta que
	// la misma retorne false o se terminen de iterar los elementos.
	Iterar(visitar func(T) bool)

	// Iterador crea un iterador externo, que permite recorrer y modificar la lista.
	Iterador() IteradorLista[T]
}

type IteradorLista[T any] interface {

	// VerActual obtiene el valor del elemento donde se encuentra iterando el iterador. Si ya se iteraron todos los
	// elementos entra en pánico con un mensaje "El iterador termino de iterar"
	VerActual() T

	// HaySiguiente devuelve verdadero si el elemento actual tiene un siguiente, false en caso contrario.
	HaySiguiente() bool

	// Siguiente itera hacia el siguiente elemento de la lista. Si ya se iteraron todos los elementos entra en
	// pánico con un mensaje "El iterador termino de iterar"
	Siguiente()

	// Insertar agrega un elemento a la lista cumpliendo las siguientes condiciones:
	// • El elemento insertado va a tomar la posicion del elemento al que se apunta.
	// • Luego de una insercion, el iterador va a apuntar al nuevo elemento.
	Insertar(T)

	// Borrar elimina el elemento al que está apuntando el iterador y este apunta al elemento siguiente. Si ya se
	// iteraron todos los elementos entra en pánico con un mensaje "El iterador termino de iterar"
	Borrar() T
}
