#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <sys/wait.h>
#define COMANDO_INVALIDO "Comando invalido\n"
#define ERROR_PIPE "Hubo un error en el pipe"
#define ERROR_LECTURA "Hubo un error al leer"
#define FORMATO_PRIMO "primo %d\n"
#define CANT_MIN_ARGUMENTOS 2


// Devuelve 0 si no es multiplo, sino devuelve 1
int
es_multiplo(int a, int b)
{
	return a % b == 0;
}

// Mientras pueda leer, escribe los numeros en el pipe derecho
void
escribir_numeros(int pipe_izq_out, int pipe_der_in, int n_actual)
{
	int n_leido;
	while (read(pipe_izq_out, &n_leido, sizeof(int)) > 0) {
		if (!es_multiplo(n_leido, n_actual)) {
			ssize_t _ = write(pipe_der_in, &n_leido, sizeof(int));
			(void) _;
		}
	}
}

// Genera numeros desde 2 hasta n y los escribe en el pipe
void
generador(int pipe_in, int n)
{
	for (int i = 2; i <= n; i++) {
		ssize_t _ = write(pipe_in, &i, sizeof(int));
		(void) _;
	}

	close(pipe_in);
}

// Devuelve true si la cantidad de argumentos es invalida
bool
argumentos_invalidos(int argc)
{
	if (argc != CANT_MIN_ARGUMENTOS) {
		printf(COMANDO_INVALIDO);
		return true;
	}

	return false;
}

// Si no hay mas para leer cierra el pipe pasado por parametro
// Si hay error al leer, lanza un mensaje
bool
hay_error_en_read(ssize_t bytes_leidos, int pipe_izq)
{
	if (bytes_leidos == 0) {
		close(pipe_izq);
		return true;

	} else if (bytes_leidos < 0) {
		perror(ERROR_LECTURA);
		return true;
	}

	return false;
}

// Devuelve true si hay error en pipe
bool
hay_error_en_pipe(int pipe)
{
	if (pipe < 0) {
		perror(ERROR_PIPE);
		return true;
	}
	return false;
}

// Imprime el valor leido y envia por el pipe derecho los numeros que no son
// multiplos del n actual.
bool
filtro(int pipe_izq)
{
	int n_actual;

	ssize_t bytes_leidos = read(pipe_izq, &n_actual, sizeof(int));
	if (hay_error_en_read(bytes_leidos, pipe_izq)) {
		return false;
	}

	printf(FORMATO_PRIMO, n_actual);

	int fds_der[2];
	if (hay_error_en_pipe(pipe(fds_der))) {
		return false;
	}

	if (fork() == 0) {
		close(pipe_izq);
		close(fds_der[1]);

		if (!filtro(fds_der[0])) {
			return 1;
		}

		close(fds_der[0]);
		exit(0);

	} else {
		close(fds_der[0]);

		escribir_numeros(pipe_izq, fds_der[1], n_actual);

		close(pipe_izq);
		close(fds_der[1]);

		wait(NULL);
	}

	return true;
}
// Crea el primer pipe para pasar todos los numeros generados
// por el padre para luego generar los filtros por el hijo
bool
procesar_comando(int n)
{
	int fds[2];
	if (hay_error_en_pipe(pipe(fds))) {
		return false;
	}

	if (fork() == 0) {
		close(fds[1]);

		if (!filtro(fds[0])) {
			return false;
		}

		close(fds[0]);
		exit(0);

	} else {
		close(fds[0]);

		generador(fds[1], n);

		wait(NULL);
	}

	return true;
}

// lee los argumentos pasados por terminal y procesa el comando
int
main(int argc, char *argv[])
{
	if (argumentos_invalidos(argc)) {
		return 1;
	}

	int n_actual = atoi(argv[1]);
	if (!procesar_comando(n_actual)) {
		return 1;
	}

	return 0;
}
