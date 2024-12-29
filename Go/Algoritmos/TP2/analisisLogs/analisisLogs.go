package analisisLogs

type AnalisisLogs interface {
	ProcesarArchivo(string) error
	VerVisitantes(string, string)
	VerMasVisitados(int)
}
