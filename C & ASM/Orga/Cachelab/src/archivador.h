#ifndef ARCHIVADOR_T
#define ARCHIVADOR_T
#include <stdio.h>
#include "cache.h"

// Lee un acceso del archivo y lo guarda en un acceso_t
acceso_t* leer_acceso(FILE *archivo);

// Verifica que los parámetros sean válidos
FILE* verificar_entrada(int argc , char *argv[]);

#endif // ARCHIVADOR_T
