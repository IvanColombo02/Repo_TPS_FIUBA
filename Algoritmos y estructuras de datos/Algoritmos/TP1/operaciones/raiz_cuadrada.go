package operaciones

import (
	"errors"
	"math"
)

const MINIMO_VALOR_RADICANDO = 0

func RaizCuadrada(radicando int64) (int64, error) {
	// Recibe un radicando y devuelve su raiz cuadrada
	if radicando < MINIMO_VALOR_RADICANDO {
		return 0, errors.New("ERROR")
	}
	return int64(math.Sqrt(float64(radicando))), nil
}
