package operaciones

import "errors"

func DivisionEntera(numerador, denominador int64) (int64, error) {
	// Recibe numerador, denominador y devuelve su division entera
	if denominador == 0 {
		return 0, errors.New("ERROR")
	}
	resultado := numerador / denominador
	return int64(resultado), nil
}
