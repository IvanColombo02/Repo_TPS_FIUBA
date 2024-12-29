package main

import (
	TDAPila "Algoritmos/PilaDinamica"
	"bufio"
	"errors"
	"fmt"
	"os"
	"strconv"
	"strings"
	Operador "tp1/operaciones"
)

const (
	SUMA              = "+"
	RESTA             = "-"
	MULTIPLICACION    = "*"
	DIVISION          = "/"
	RAIZ_CUADRADA     = "sqrt"
	LOGARITMO         = "log"
	EXPONENCIACION    = "^"
	OPERADOR_TERNARIO = "?"
	MENSAJE_ERROR     = "ERROR"
)

type operacion struct {
	simbolo string
	aridad  int
	operar  func(operadores []int64) (int64, error)
}

func main() {
	LecturaArchivo()
}

func LecturaArchivo() {
	// Lee la entrada estandar para luego procesar la linea
	lectura := bufio.NewScanner(os.Stdin)
	for lectura.Scan() {
		linea := lectura.Text()
		procesarLinea(linea)
	}
}

func procesarLinea(linea string) {
	// Recibe una linea y una lista de operaciones e imprime el resultado
	// si es una operacion se ejecuta la operacion, de lo contrario es un numero y se apila
	tokens := strings.Fields(linea)
	pila := TDAPila.CrearPilaDinamica[int64]()
	largoPila := 0
	for _, token := range tokens {
		operacion, esOperador := esOperador(token, obtenerListaOperaciones())
		if esOperador {
			err := ejecutarOperacion(pila, operacion, &largoPila)
			if err != nil {
				fmt.Println(devolverError())
				return
			}
		} else {
			err := apilarOperando(pila, token)
			largoPila++
			if err != nil {
				fmt.Println(devolverError())
				return

			}
		}
	}
	// La pila debe tener un solo elemento, de lo contrario hay un error
	if pila.EstaVacia() {
		return
	}
	resultado := pila.Desapilar()
	if !pila.EstaVacia() {
		fmt.Println(devolverError())
		return
	}
	fmt.Println(resultado)

}

func ejecutarOperacion(pila TDAPila.Pila[int64], operacion operacion, largoPila *int) error {
	//Recibe una pila, su largo, una operacion y la ejecuta devolviendo el resultado
	//Si hubo error devuelve el mensaje "ERROR"
	operandos := make([]int64, operacion.aridad)
	if *largoPila < operacion.aridad {
		return devolverError()
	}
	cant_operandos := operacion.aridad - 1
	for cant_operandos >= 0 {
		operandos[cant_operandos] = pila.Desapilar()
		cant_operandos--
	}

	resultado, err := operacion.operar(operandos)
	if err != nil {
		return devolverError()
	}
	*largoPila -= operacion.aridad - 1
	pila.Apilar(resultado)
	return nil
}

func apilarOperando(pila TDAPila.Pila[int64], operando string) error {
	// Esta funcion recibe una pila, operando string y lo convierte a int
	// luego lo apila como int64
	numero, err := strconv.Atoi(operando)
	if err != nil {
		return devolverError()
	}
	pila.Apilar(int64(numero))
	return nil
}

func esOperador(token string, listaOperaciones []operacion) (operacion, bool) {
	//Si el token es un operador entonces devuelve una instancia de la operacion con sus campos
	//de lo contrario devuelve la instancia vacia y false
	for _, operaciones := range listaOperaciones {
		if operaciones.simbolo == token {
			return operaciones, true
		}
	}
	return operacion{}, false
}

func devolverError() error {
	// Solamente devuelve error
	return errors.New(MENSAJE_ERROR)
}

func obtenerListaOperaciones() []operacion {
	//Devuelve una lista con cada operacion, su simbolo, aridad y funcion
	return []operacion{
		{
			simbolo: SUMA,
			aridad:  2,
			operar:  func(operadores []int64) (int64, error) { return operadores[0] + operadores[1], nil },
		},
		{
			simbolo: RESTA,
			aridad:  2,
			operar:  func(operadores []int64) (int64, error) { return operadores[0] - operadores[1], nil },
		},
		{
			simbolo: MULTIPLICACION,
			aridad:  2,
			operar:  func(operadores []int64) (int64, error) { return operadores[0] * operadores[1], nil },
		},
		{
			simbolo: DIVISION,
			aridad:  2,
			operar:  func(operadores []int64) (int64, error) { return Operador.DivisionEntera(operadores[0], operadores[1]) },
		},
		{
			simbolo: RAIZ_CUADRADA,
			aridad:  1,
			operar:  func(operadores []int64) (int64, error) { return Operador.RaizCuadrada(operadores[0]) },
		},
		{
			simbolo: EXPONENCIACION,
			aridad:  2,
			operar:  func(operadores []int64) (int64, error) { return Operador.Exponenciacion(operadores[0], operadores[1]) },
		},
		{
			simbolo: LOGARITMO,
			aridad:  2,
			operar:  func(operadores []int64) (int64, error) { return Operador.Logaritmo(operadores[0], operadores[1]) },
		},
		{
			simbolo: OPERADOR_TERNARIO,
			aridad:  3,
			operar: func(operadores []int64) (int64, error) {
				return Operador.OperadorTernario(operadores[0], operadores[1], operadores[2])
			},
		},
	}
}
