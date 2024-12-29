package operaciones

import (
	"errors"
	"math"
)

const (
	MINIMO_VALOR_ARGUMENTO = 0
	MINIMO_VALOR_BASE      = 2
)

func Logaritmo(argumento, base int64) (int64, error) {
	/* Recibe base, argumento luego utiliza la propiedad de division de logaritmos y devuelve
	   el logaritmo en base "denominador" del argumento "numerador"*/
	if base < MINIMO_VALOR_BASE {
		return 0, errors.New("ERROR")
	}
	if argumento <= MINIMO_VALOR_ARGUMENTO {
		return 0, errors.New("ERROR")
	}
	numerador := math.Log(float64(argumento))
	denominador := math.Log(float64(base))
	return int64(numerador / denominador), nil
}
