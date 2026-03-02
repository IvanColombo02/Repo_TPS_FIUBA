#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>

#define FIN_LINEA "\n"
#define FIN_STR '\0'
#define ERROR_ARGS "Argumentos inválidos\n"
#define ERROR_OPEN "Hubo error al abrir directorio\n"
#define ERROR_STAT "Hubo error en stat\n"
#define ERROR_READLINK "Error en readlink\n"
#define MAX_PATH 4096
#define MAX_LINK 4096
#define CANT_MIN_ARGUMENTOS 2
#define TIPO_REGULAR '-'
#define TIPO_DIRECTORIO 'd'
#define TIPO_LINK 'l'
#define TIPO_CARACTER 'c'
#define TIPO_BLOQUE 'b'
#define TIPO_FIFO 'p'
#define TIPO_SOCKET 's'
#define TIPO_DESCONOCIDO '?'

// Devuelve el tipo de archivo como un caracter
char
tipo_archivo(mode_t m)
{
	if (S_ISREG(m)) {
		return TIPO_REGULAR;
	}
	if (S_ISDIR(m)) {
		return TIPO_DIRECTORIO;
	}
	if (S_ISLNK(m)) {
		return TIPO_LINK;
	}
	if (S_ISCHR(m)) {
		return TIPO_CARACTER;
	}
	if (S_ISBLK(m)) {
		return TIPO_BLOQUE;
	}
	if (S_ISFIFO(m)) {
		return TIPO_FIFO;
	}
	if (S_ISSOCK(m)) {
		return TIPO_SOCKET;
	}
	return TIPO_DESCONOCIDO;
}

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

// Realiza la busqueda en el directorio dir y sus subdirectorios
void
realizar_busqueda(DIR *dir, const char *dirpath)
{
	struct dirent *entry;
	char path[MAX_PATH];
	while ((entry = readdir(dir)) != NULL) {
		snprintf(path, sizeof(path), "%s/%s", dirpath, entry->d_name);
		struct stat st;
		if (lstat(path, &st) < 0) {
			ssize_t _ = write(2, ERROR_STAT, strlen(ERROR_STAT));
			(void) _;
			continue;
		}
		char t = tipo_archivo(st.st_mode);
		printf("%s %c %o %d", entry->d_name, t, st.st_mode & 0777, st.st_uid);
		if (t == 'l') {
			char link_dest[MAX_LINK + 1];
			ssize_t len = readlink(path, link_dest, MAX_LINK);
			if (len < 0) {
				ssize_t _ = write(2,
				                  ERROR_READLINK,
				                  strlen(ERROR_READLINK));
				(void) _;
			} else {
				link_dest[len] = FIN_STR;
				printf(" -> %s", link_dest);
			}
		}
		printf(FIN_LINEA);
	}
}

// Procesa el listado del directorio
int
procesar_list(const char *dirpath)
{
	DIR *dir = opendir(dirpath);
	if (!dir) {
		ssize_t _ = write(2, ERROR_OPEN, strlen(ERROR_OPEN));
		(void) _;
		return EXIT_FAILURE;
	}
	realizar_busqueda(dir, dirpath);
	closedir(dir);
	return EXIT_SUCCESS;
}

int
main(int argc, char *argv[])
{
	if (argumentos_invalidos(argc)) {
		return EXIT_FAILURE;
	}
	return procesar_list(argv[1]);
}
