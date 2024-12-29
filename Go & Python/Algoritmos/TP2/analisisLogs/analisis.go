package analisisLogs

import (
	TDAABB "Algoritmos/ABB"
	TDAHash "Algoritmos/HashCerrado"
	TDAHeap "Algoritmos/Heap"
	TDALista "Algoritmos/ListaEnlazada"
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"
)

type sitio struct {
	url     string
	visitas int
}

type direccionIP struct {
	bloque1 int
	bloque2 int
	bloque3 int
	bloque4 int
}

type analisis struct {
	visitados           TDAHash.Diccionario[string, int]
	visitantesGlobal    TDAHash.Diccionario[direccionIP, bool]                      // Visitantes de todos los archivos
	visitantesArchivo   TDAHash.Diccionario[direccionIP, TDALista.Lista[time.Time]] // Visitantes de archivo en proceso
	visitantesOrdenados TDAABB.DiccionarioOrdenado[direccionIP, bool]
	ataquesDos          TDAHeap.ColaPrioridad[direccionIP]
	diccAtacantesDos    TDAHash.Diccionario[direccionIP, bool]
}

// CrearAnalisisLogs crea un analisis de logs vacío
func CrearAnalisisLogs() AnalisisLogs {
	return &analisis{
		visitados:           TDAHash.CrearHash[string, int](),
		visitantesGlobal:    TDAHash.CrearHash[direccionIP, bool](),
		visitantesOrdenados: TDAABB.CrearABB[direccionIP, bool](cmpIPs),
	}
}

// ProcesarArchivo procesa un archivo de logs
func (analisis *analisis) ProcesarArchivo(ruta string) error { // O(n + n log v)
	analisis.visitantesArchivo = TDAHash.CrearHash[direccionIP, TDALista.Lista[time.Time]]()
	analisis.ataquesDos = TDAHeap.CrearHeap[direccionIP](cmpIPsMin)
	analisis.diccAtacantesDos = TDAHash.CrearHash[direccionIP, bool]()

	archivo, err := os.Open(ruta)
	if err != nil {
		return err
	}
	defer archivo.Close()

	s := bufio.NewScanner(archivo)
	for s.Scan() {
		partes := strings.Fields(s.Text())
		analisis.actualizarVisitante(procesarIP(partes[0]), procesarInstante(partes[1])) // O(log v)
		analisis.actualizarVisitado(partes[3])                                           // O(1)
	}

	analisis.mostrarDoS()
	return nil
}

// actualizaVisitante actualiza los visitantes del análisis global y del archivo en proceso
func (analisis *analisis) actualizarVisitante(ip direccionIP, momento time.Time) { // O(log v)
	if !analisis.visitantesGlobal.Pertenece(ip) {
		analisis.visitantesGlobal.Guardar(ip, true)
		analisis.visitantesOrdenados.Guardar(ip, true) // O(log v)
	}

	if !analisis.visitantesArchivo.Pertenece(ip) {
		analisis.visitantesArchivo.Guardar(ip, TDALista.CrearListaEnlazada[time.Time]())
	}

	lista := analisis.visitantesArchivo.Obtener(ip)
	lista.InsertarUltimo(momento)
	if !analisis.diccAtacantesDos.Pertenece(ip) && chequearDoS(lista) {
		analisis.ataquesDos.Encolar(ip) // O(log d), pero como hay pocos DoS; d está acotdado por v; y solo se encola una vez por DoS de c/ v, d es despreciable => O(1)
		analisis.diccAtacantesDos.Guardar(ip, true)
	}
}

// actualizaVisitado actualiza los sitios visitados
func (analisis *analisis) actualizarVisitado(sitio string) { // O(1)
	if !analisis.visitados.Pertenece(sitio) {
		analisis.visitados.Guardar(sitio, 1)
		return
	}
	analisis.visitados.Guardar(sitio, analisis.visitados.Obtener(sitio)+1)
}

// VerMasVisitados muestra los k sitios más visitados
func (analisis analisis) VerMasVisitados(k int) { // O(s + k log s)
	sitios := make([]sitio, analisis.visitados.Cantidad())                                   // O(s)
	for i, iter := 0, analisis.visitados.Iterador(); iter.HaySiguiente(); iter.Siguiente() { // O(s)
		url, visitas := iter.VerActual()
		sitios[i] = sitio{url, visitas}
		i++
	}

	fmt.Println("Sitios más visitados:")
	heap := TDAHeap.CrearHeapArr[sitio](sitios, cmpSitiosMax) // O(s)
	for i := 0; i < k && !heap.EstaVacia(); i++ {             // O(k log s)
		visitado := heap.Desencolar() // O(log s)
		fmt.Printf("\t%s - %d\n", visitado.url, visitado.visitas)
	}
}

// VerVisitantes muestra los visitantes entre dos direcciones IP
func (analisis analisis) VerVisitantes(inicio, fin string) { // O(v), caso promedio O(log v)
	analisis.verVisitantesAux(procesarIP(inicio), procesarIP(fin))
}

func (analisis analisis) verVisitantesAux(inicio, fin direccionIP) { // O(v), caso promedio O(log v)
	fmt.Println("Visitantes:")
	for iter := analisis.visitantesOrdenados.IteradorRango(&inicio, &fin); iter.HaySiguiente(); iter.Siguiente() {
		ip, _ := iter.VerActual()
		fmt.Printf("\t%d.%d.%d.%d\n", ip.bloque1, ip.bloque2, ip.bloque3, ip.bloque4)
	}
}

// mostrarDoS muestra los ataques DoS
func (analisis analisis) mostrarDoS() { // O(d log d), d acotado superiormente por v y potencialmente despreciable => O(1)
	for !analisis.ataquesDos.EstaVacia() {
		ip := analisis.ataquesDos.Desencolar()
		fmt.Printf("DoS: %d.%d.%d.%d\n", ip.bloque1, ip.bloque2, ip.bloque3, ip.bloque4)
	}
}

// Funciones auxiliares:

// chequearDoS chequea si hay un ataque DoS
func chequearDoS(lista TDALista.Lista[time.Time]) bool { // O(1)
	if lista.Largo() < 5 {
		return false
	}

	if lista.VerUltimo().Sub(lista.VerPrimero()).Seconds() < 2 {
		return true
	}

	lista.BorrarPrimero()
	return false
}

// procesarIP procesa una dirección IP dada en formato string y la devuelve en formato direccionIP
func procesarIP(ip string) direccionIP { // O(1)
	bloquesInt := make([]int, 4)
	for i, bloque := range strings.Split(ip, ".") {
		bloquesInt[i], _ = strconv.Atoi(bloque)
	}
	return direccionIP{bloquesInt[0], bloquesInt[1], bloquesInt[2], bloquesInt[3]}
}

// procesarInstante procesa una fecha dada en formato string y la devuelve en formato time.Time
func procesarInstante(momento string) time.Time { // O(1)
	fecha, _ := time.Parse("2006-01-02T15:04:05-07:00", momento)
	return fecha
}

// cmpIPs compara si una dirección IP 'A' es mayor, menor o igual a otra dirección IP 'B'
func cmpIPs(dirA, dirB direccionIP) int { // O(1)
	if dirA.bloque1 != dirB.bloque1 {
		return dirA.bloque1 - dirB.bloque1
	}
	if dirA.bloque2 != dirB.bloque2 {
		return dirA.bloque2 - dirB.bloque2
	}
	if dirA.bloque3 != dirB.bloque3 {
		return dirA.bloque3 - dirB.bloque3
	}
	return dirA.bloque4 - dirB.bloque1
}

// cmpIPsMin realiza lo mismo que cmpIPs pero invirtiendo los argumentos
func cmpIPsMin(dirA, dirB direccionIP) int { // O(1)
	return cmpIPs(dirB, dirA)
}

// cmpSitiosMax compara si un sitio 'A' es mayor, menor o igual a otro sitio 'B'
func cmpSitiosMax(sitioA, sitioB sitio) int { // O(1)
	return sitioA.visitas - sitioB.visitas
}
