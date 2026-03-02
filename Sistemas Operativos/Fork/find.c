#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdbool.h>

#define ROOT_DIR "."
#define PREV_DIR ".."
#define ERROR_ARGS "Argumentos inválidos\n"
#define ERROR_OPEN "Error al abrir directorio\n"
#define MAX_PATH 4096
#define CANT_MIN_ARGUMENTOS 2

// Devuelve true si la cantidad de argumentos es invalida
bool
argumentos_invalidos(int argc)
{
	if (argc != CANT_MIN_ARGUMENTOS) {
		ssize_t _ = write(2, ERROR_ARGS, strlen(ERROR_ARGS));
		(void) _;
		return true;
	}
	return false;
}

// Busca recursivamente en el directorio dirpath y sus subdirectorios
void
buscar(const char *dirpath, const char *patron)
{
	DIR *dir = opendir(dirpath);
	if (!dir) {
		ssize_t _ = write(2, ERROR_OPEN, strlen(ERROR_OPEN));
		(void) _;
		return;
	}
	struct dirent *entry;
	char path[MAX_PATH];
	while ((entry = readdir(dir)) != NULL) {
		if (strcmp(entry->d_name, ROOT_DIR) == 0 ||
		    strcmp(entry->d_name, PREV_DIR) == 0) {
			continue;
		}
		snprintf(path, sizeof(path), "%s/%s", dirpath, entry->d_name);
		struct stat st;
		if (stat(path, &st) == 0) {
			if (strstr(entry->d_name, patron) != NULL) {
				printf("%s\n", path);
			}
			if (S_ISDIR(st.st_mode)) {
				buscar(path, patron);
			}
		}
	}
	closedir(dir);
}

int
main(int argc, char *argv[])
{
	if (argumentos_invalidos(argc)) {
		return EXIT_FAILURE;
	}
	buscar(ROOT_DIR, argv[1]);
	return EXIT_SUCCESS;
}
