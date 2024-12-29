package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	def "tp0/ejercicios"
)

const (
	RUTA  = "archivo1.in"
	RUTA2 = "archivo2.in"
)

func main() {
	//Crea los arreglos y luego ordena e imprime el arreglo mas grande

	vector1, vector2 := CrearVector(RUTA), CrearVector(RUTA2)
	if def.Comparar(vector1, vector2) == -1 {
		OrdenareImprimirVector(vector2)
	} else {
		OrdenareImprimirVector(vector1)
	}
}

func CrearVector(ruta string) []int {
	//Recibe una ruta y devuelve un arreglo de enteros

	vector := make([]int, 0)
	archivo, _ := os.Open(ruta)
	defer archivo.Close()
	lineas := bufio.NewScanner(archivo)

	//Aqui transforma los numeros del tipo string a int y los almacena en la variable vector

	for lineas.Scan() {
		linea := lineas.Text()
		valor, _ := strconv.Atoi(linea)
		vector = append(vector, valor)
	}
	return vector
}

func OrdenareImprimirVector(vector []int) {
	//Ordena el arreglo pasado por parametro y lo imprime

	def.Seleccion(vector)
	for i := range len(vector) {
		fmt.Printf("%d\n", vector[i])
	}
}
