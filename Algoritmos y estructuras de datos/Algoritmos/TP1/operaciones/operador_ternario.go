package operaciones

func OperadorTernario(valor_condicional, valor_si_true, valor_si_false int64) (int64, error) {
	// Recibe a, b, c y devuelve b si a != 0 sino devuelve c
	if valor_condicional != 0 {
		return valor_si_true, nil
	}
	return valor_si_false, nil
}
