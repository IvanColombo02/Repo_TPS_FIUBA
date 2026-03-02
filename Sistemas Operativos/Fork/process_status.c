#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <ctype.h>
#include <stdbool.h>

#define FIN_LINEA "\n"
#define LECTURA "r"
#define FIN_STR '\0'
#define PROC_DIR "/proc"
#define COMM_FILE "comm"
#define ERROR_OPEN "Error al abrir el directorio /proc"
#define MAX_PATH 256
#define MAX_COMM 256

// Verifica si un string es numérico (PID válido)
bool
es_numero(const char *s)
{
	for (; *s; ++s) {
		if (!isdigit(*s)) {
			return false;
		}
	}
	return true;
}

// Lee el contenido del archivo comm y lo deja en comm_buffer
bool
leer_comm(const char *ruta, char *comm_buffer, size_t size)
{
	FILE *f = fopen(ruta, LECTURA);
	if (!f) {
		return false;
	}
	if (!fgets(comm_buffer, (int) size, f)) {
		fclose(f);
		return false;
	}
	comm_buffer[strcspn(comm_buffer, FIN_LINEA)] = FIN_STR;
	fclose(f);
	return true;
}

// Lista procesos mostrando PID y nombre de comando
void
listar_procesos(void)
{
	DIR *dir = opendir(PROC_DIR);
	if (!dir) {
		perror(ERROR_OPEN);
		exit(EXIT_FAILURE);
	}
	struct dirent *entry;
	char path[MAX_PATH];
	char comm[MAX_COMM];
	while ((entry = readdir(dir)) != NULL) {
		if (!es_numero(entry->d_name)) {
			continue;
		}
		snprintf(path, sizeof(path), PROC_DIR "/%s/" COMM_FILE, entry->d_name);
		if (leer_comm(path, comm, sizeof(comm))) {
			printf("%5s %s\n", entry->d_name, comm);
		}
	}
	closedir(dir);
}

int
main(void)
{
	listar_procesos();
	return EXIT_SUCCESS;
}