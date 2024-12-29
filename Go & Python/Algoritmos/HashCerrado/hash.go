package hash

import "fmt"

type estadoTipo int

const (
	VACIO estadoTipo = iota
	BORRADO
	OCUPADO

	_TAM_INICIAL        = 11
	_MULTI_ACHICAR      = 4
	_FACTOR_AGRANDAR    = 0.7
	_FACTOR_REDIMENSION = 2
)

type celdaHash[K comparable, V any] struct {
	clave  K
	valor  V
	estado estadoTipo
}

type hashCerrado[K comparable, V any] struct {
	tabla    []celdaHash[K, V]
	cantidad int
	tam      int
	borrados int
}

type iterHashCerrado[K comparable, V any] struct {
	dicc     *hashCerrado[K, V]
	actual   celdaHash[K, V]
	posicion int
}

func CrearHash[K comparable, V any]() Diccionario[K, V] {
	return &hashCerrado[K, V]{
		tabla:    make([]celdaHash[K, V], _TAM_INICIAL),
		cantidad: 0,
		tam:      _TAM_INICIAL,
		borrados: 0,
	}
}

func (dicc *hashCerrado[K, V]) Guardar(clave K, valor V) {
	indice := dicc.obtenerIndice(clave)
	if dicc.tabla[indice].estado == VACIO {
		dicc.tabla[indice].clave = clave
		dicc.cantidad++
		dicc.tabla[indice].estado = OCUPADO
	}
	dicc.tabla[indice].valor = valor
	carga := float32((dicc.cantidad + dicc.borrados)) / float32(dicc.tam)
	if carga > _FACTOR_AGRANDAR {
		dicc.redimensionar(dicc.tam * _FACTOR_REDIMENSION)
	}
}

func (dicc *hashCerrado[K, V]) Pertenece(clave K) bool {
	indice := dicc.obtenerIndice(clave)
	return dicc.tabla[indice].estado == OCUPADO
}

func (dicc *hashCerrado[K, V]) Obtener(clave K) V {
	indice := dicc.obtenerIndice(clave)
	dicc.chequearPertenencia(indice)
	return dicc.tabla[indice].valor
}

func (dicc *hashCerrado[K, V]) Borrar(clave K) V {
	indice := dicc.obtenerIndice(clave)
	dicc.chequearPertenencia(indice)
	dicc.tabla[indice].estado = BORRADO
	valor := dicc.tabla[indice].valor
	dicc.borrados++
	dicc.cantidad--

	if dicc.cantidad*_MULTI_ACHICAR <= dicc.tam && dicc.tam > _TAM_INICIAL {
		dicc.redimensionar(dicc.tam / _FACTOR_REDIMENSION)
	}
	return valor
}

func (dicc *hashCerrado[K, V]) Cantidad() int {
	return dicc.cantidad
}

func (dicc *hashCerrado[K, V]) Iterar(visitar func(K, V) bool) {
	for _, celda := range dicc.tabla {
		if celda.estado == OCUPADO {
			if !visitar(celda.clave, celda.valor) {
				return
			}
		}
	}
}

func (dicc *hashCerrado[K, V]) Iterador() IterDiccionario[K, V] {
	pos := 0
	if dicc.cantidad != 0 {
		for i, celda := range dicc.tabla {
			if celda.estado == OCUPADO {
				pos = i
				break
			}
		}
	}

	return &iterHashCerrado[K, V]{dicc, dicc.tabla[pos], pos}
}

func (iter *iterHashCerrado[K, V]) HaySiguiente() bool {
	return iter.dicc.cantidad != 0 && iter.posicion < iter.dicc.tam
}

func (iter *iterHashCerrado[K, V]) VerActual() (K, V) {
	iter.chequearFinIteracion()
	return iter.actual.clave, iter.actual.valor
}

func (iter *iterHashCerrado[K, V]) Siguiente() {
	iter.chequearFinIteracion()

	pos := iter.posicion + 1
	for ; pos < iter.dicc.tam && iter.dicc.tabla[pos].estado != OCUPADO; pos++ {
	}

	if pos < iter.dicc.tam {
		iter.actual = iter.dicc.tabla[pos]
	}

	iter.posicion = pos
}

func (iter *iterHashCerrado[K, V]) chequearFinIteracion() {
	if !iter.HaySiguiente() {
		panic("El iterador termino de iterar")
	}
}

func (dicc *hashCerrado[K, V]) redimensionar(nuevo_tam int) {
	anterior := dicc.tabla
	dicc.tabla = make([]celdaHash[K, V], nuevo_tam)
	dicc.tam = nuevo_tam
	dicc.borrados = 0
	dicc.cantidad = 0

	for _, elemento := range anterior {
		if elemento.estado == OCUPADO {
			dicc.Guardar(elemento.clave, elemento.valor)
		}
	}
}

func (dicc *hashCerrado[K, V]) obtenerIndice(clave K) int {
	indice := obtenerHash(convertirABytes(clave)) % dicc.tam
	for dicc.tabla[indice].estado == BORRADO || dicc.tabla[indice].estado == OCUPADO && dicc.tabla[indice].clave != clave {
		indice = (indice + 1) % dicc.tam
	}
	return indice
}

func (dicc hashCerrado[K, V]) chequearPertenencia(indice int) {
	if dicc.tabla[indice].estado != OCUPADO {
		panic("La clave no pertenece al diccionario")
	}
}

func obtenerHash(bytes []byte) int {
	var hash uint32 = 2166136261
	for _, b := range bytes {
		hash ^= uint32(b)
		hash *= 16777619
	}

	return int(hash & 0x7FFFFFFF)
}

func convertirABytes[K comparable](clave K) []byte {
	return []byte(fmt.Sprintf("%v", clave))
}
