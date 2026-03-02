#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>

#define NO_DATA 0
#define SYS_ERROR -1
#define MIN_CANT_ARGS 3
#define ERROR_ARGS "Argumentos inválidos\n"
#define ERROR_ORIG "El archivo origen no existe o no es regular\n"
#define ERROR_DEST "El archivo de destino ya existe\n"
#define ERROR_OPEN "Hubo error al abrir archivo\n"
#define ERROR_STAT "Hubo error al obtener stat\n"
#define ERROR_MMAP "Error en mmap\n"
#define ERROR_TRUNC "Error en ftruncate\n"

// Devuelve true si la cantidad de argumentos es inválida
bool
argumentos_invalidos(int argc)
{
	if (argc != MIN_CANT_ARGS) {
		ssize_t _ = write(2, ERROR_ARGS, strlen(ERROR_ARGS));
		(void) _;
		return true;
	}
	return false;
}

// Abre el archivo origen y chequea que sea regular
int
abrir_origen(const char *origen, struct stat *st)
{
	int fd = open(origen, O_RDONLY);
	if (fd < 0) {
		ssize_t _ = write(2, ERROR_ORIG, strlen(ERROR_ORIG));
		(void) _;
		return SYS_ERROR;
	}
	if (fstat(fd, st) < 0 || !S_ISREG(st->st_mode)) {
		ssize_t _ = write(2, ERROR_ORIG, strlen(ERROR_ORIG));
		(void) _;
		close(fd);
		return SYS_ERROR;
	}
	return fd;
}

// Abre el archivo destino, debe no existir
int
abrir_destino(const char *destino, off_t size)
{
	int fd = open(destino, O_RDWR | O_CREAT | O_EXCL, 0644);
	if (fd < 0) {
		ssize_t _ = write(2, ERROR_DEST, strlen(ERROR_DEST));
		(void) _;
		return SYS_ERROR;
	}
	if (ftruncate(fd, size) < 0) {
		ssize_t _ = write(2, ERROR_TRUNC, strlen(ERROR_TRUNC));
		(void) _;
		close(fd);
		return SYS_ERROR;
	}
	return fd;
}

// Realiza la copia usando mmap
int
copiar_contenido(int fd_origen, int fd_destino, off_t size)
{
	if (size == 0) {
		return NO_DATA;
	}
	void *orig_map = mmap(NULL, size, PROT_READ, MAP_SHARED, fd_origen, 0);
	if (orig_map == MAP_FAILED) {
		ssize_t _ = write(2, ERROR_MMAP, strlen(ERROR_MMAP));
		(void) _;
		return EXIT_FAILURE;
	}
	void *dest_map = mmap(NULL, size, PROT_WRITE, MAP_SHARED, fd_destino, 0);
	if (dest_map == MAP_FAILED) {
		ssize_t _ = write(2, ERROR_MMAP, strlen(ERROR_MMAP));
		(void) _;
		munmap(orig_map, size);
		return EXIT_FAILURE;
	}
	memcpy(dest_map, orig_map, size);
	munmap(orig_map, size);
	munmap(dest_map, size);
	return EXIT_SUCCESS;
}

// procesa el comando, abre los archivos y copia el contenido
int
procesar_comando(const char *orig, const char *dest)
{
	struct stat st;
	int fd_origen = abrir_origen(orig, &st);
	if (fd_origen < 0) {
		return EXIT_FAILURE;
	}
	int fd_destino = abrir_destino(dest, st.st_size);
	if (fd_destino < 0) {
		close(fd_origen);
		return EXIT_FAILURE;
	}
	int res = copiar_contenido(fd_origen, fd_destino, st.st_size);
	close(fd_origen);
	close(fd_destino);
	return res;
}

int
main(int argc, char *argv[])
{
	if (argumentos_invalidos(argc)) {
		return EXIT_FAILURE;
	}
	return procesar_comando(argv[1], argv[2]);
}
