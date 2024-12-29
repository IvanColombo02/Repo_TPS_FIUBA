package main

import (
	"bufio"
	"os"
	analisisLogs "tp2/analisisLogs"
	comandos "tp2/procesamientoComandos"
)

func main() {
	analisis := analisisLogs.CrearAnalisisLogs()
	s := bufio.NewScanner(os.Stdin)
	for s.Scan() {
		comandos.EjecutarComando(analisis, s.Text())
	}
}
