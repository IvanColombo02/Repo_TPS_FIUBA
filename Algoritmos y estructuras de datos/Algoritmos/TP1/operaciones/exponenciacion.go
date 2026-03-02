package operaciones

import (
	"errors"
	"math"
)

const (
	MINIMO_VALOR_EXPONENTE = 0
)

func Exponenciacion(base, exponente int64) (int64, error) {
	// Recibe base, exponente y devuelve la base elevado al exponente
	if exponente < MINIMO_VALOR_EXPONENTE {
		return 0, errors.New("Error")
	}
	return int64(math.Pow(float64(base), float64(exponente))), nil
}
