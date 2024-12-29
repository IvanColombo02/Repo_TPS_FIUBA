package procesamientoComandos

import (
	"errors"
	"fmt"
	"os"
	"strconv"
	"strings"

	analisisLogs "tp2/analisisLogs"
)

const (
	_AGREGAR_ARCHIVO   = "agregar_archivo"
	_VER_VISITANTES    = "ver_visitantes"
	_VER_MAS_VISITADOS = "ver_mas_visitados"
	_ERROR_COMANDO     = "el comando ingresado no es válido"
	_ERROR_PARAMETROS  = "los parámetros ingresados no son correctos"
)

// EjecutarComando dado un analisis y una linea, ejecuta el comando correspondiente
func EjecutarComando(analisis analisisLogs.AnalisisLogs, linea string) {
	comando, parametros, err := validarLinea(linea)
	if err != nil {
		mostrarError(comando)
		return
	}

	switch comando {
	case _AGREGAR_ARCHIVO:
		if analisis.ProcesarArchivo(parametros[0]) != nil {
			mostrarError(_AGREGAR_ARCHIVO)
			return
		}

	case _VER_VISITANTES:
		if !validarIP(parametros[0]) || !validarIP(parametros[1]) {
			mostrarError(_VER_VISITANTES)
			return
		}
		analisis.VerVisitantes(parametros[0], parametros[1])

	case _VER_MAS_VISITADOS:
		k, err := strconv.Atoi(parametros[0])
		if err != nil {
			mostrarError(_VER_MAS_VISITADOS)
			return
		}
		analisis.VerMasVisitados(k)
	}

	mostrarOk()
}

// validarLinea valida que la linea ingresada sea correcta
func validarLinea(linea string) (string, []string, error) {
	partes := strings.Fields(linea)
	if len(partes) == 0 {
		return "", nil, devolverError(_ERROR_COMANDO)
	}

	longitud := 0
	switch partes[0] {
	case _AGREGAR_ARCHIVO:
		longitud = 1
	case _VER_VISITANTES:
		longitud = 2
	case _VER_MAS_VISITADOS:
		longitud = 1
	default:
		return partes[0], nil, devolverError(_ERROR_COMANDO)
	}

	if !validarLongitud(partes[1:], longitud) {
		return partes[0], nil, devolverError(_ERROR_PARAMETROS)
	}

	return partes[0], partes[1:], nil
}

// validarIP valida que una ip tenga el formato correcto
func validarIP(ip string) bool {
	partes := strings.Split(ip, ".")
	if len(partes) != 4 {
		return false
	}

	for _, parte := range partes {
		bloque, err := strconv.Atoi(parte)
		if err != nil || bloque < 0 || bloque > 255 {
			return false
		}
	}
	return true
}

// devolverError devuelve un error con el tipo de error dado
func devolverError(tipoError string) error {
	return errors.New(tipoError)
}

// mostrarError imprime por stderr un mensaje de error con el formato "Error en comando <comando>"
func mostrarError(comando string) {
	fmt.Fprintln(os.Stderr, "Error en comando", comando)
}

// mostrarOk imprime por stdout "OK"
func mostrarOk() {
	fmt.Println("OK")
}

// validarLongitud valida que la longitud de un arreglo sea la esperada
func validarLongitud[T any](arreglo []T, longitud int) bool {
	return len(arreglo) == longitud
}
