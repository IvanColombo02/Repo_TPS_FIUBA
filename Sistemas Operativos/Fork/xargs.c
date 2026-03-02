#ifndef NARGS
#define NARGS 4
#endif
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>
#include <sys/wait.h>

#define COMANDO_INVALIDO "Comando invalido\n"
#define ERROR_EN_EXECVP "Hubo error al ejecutar el programa con execvp"
#define CANT_MIN_ARGUMENTOS 2

// Libera la memoria de las lineas de args que se usaron
void
liberar_memoria(char *args[], int *contador)
{
	for (int i = 1; i <= *contador; i++) {
		free(args[i]);
	}
}

// Devuelve true si los argumentos son invalidos
bool
argumentos_invalidos(int argc)
{
	if (argc != CANT_MIN_ARGUMENTOS) {
		printf(COMANDO_INVALIDO);
		return true;
	}
	return false;
}

// Ejecuta el comando junto con sus argumentos
// Si hay error en execvp se lanza un error
void
ejecutar_comando(char *args[], int *contador)
{
	args[*contador + 1] = NULL;

	if (fork() == 0) {
		execvp(args[0], args);
		perror(ERROR_EN_EXECVP);
		exit(-1);
	} else {
		wait(NULL);
	}

	liberar_memoria(args, contador);
}

// Lee las lineas con getline y las almacena en args
// Si alcanza el maximo de NARGS ejecuta el comando
// Si quedan argumentos, se ejecuta de nuevo el comando con los restantes
void
procesar_comando(char *args[])
{
	int contador = 0;
	char *linea = NULL;
	size_t tam = 0;

	while (getline(&linea, &tam, stdin) != -1) {
		linea[strcspn(linea, "\n")] = '\0';
		args[contador + 1] = strdup(linea);
		contador++;

		if (contador == NARGS) {
			ejecutar_comando(args, &contador);
			contador = 0;
		}
	}

	if (contador > 0) {
		ejecutar_comando(args, &contador);
	}
	free(linea);
}

// Chequea los argumentos y si son validos almacena el
// comando a ejecutar y los procesa junto con sus argumentos
int
main(int argc, char *argv[])
{
	if (argumentos_invalidos(argc)) {
		return 1;
	}

	char *args[NARGS + 2];
	args[0] = argv[1];

	procesar_comando(args);

	return 0;
}
