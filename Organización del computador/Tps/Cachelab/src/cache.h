#ifndef CACHE_H
#define CACHE_H

#include "metricas.h"
typedef struct acceso_memoria {
    unsigned int instruction_pointer;
    char operacion;
    unsigned int direccion_de_memoria; 
    int byte_contador; 
    unsigned int data;

} acceso_t;

typedef struct direccion {
    int tag;
    int set;
    int bloque;
} direccion_t;

typedef struct linea {
    int indice;
    int valido;
    int dirty;
    int tag;
    int cant_veces_no_usado; 
    char *bloque; 
    int ultimo_usado;
} linea_t;

typedef struct set {
    linea_t *lineas;
} set_t;

typedef struct cache {
    set_t *sets;
    int cant_sets;
    int cant_lineas_por_set;
    int tamanio_bloque;
} cache_t;

//Crea una cache
cache_t* crear_cache(int tamanio_bytes, int asociatividad, int num_sets);

//libera la cache 
void liberar_cache(cache_t* cache);

//Procesa la instruccion de acceso pasada por parametro en la cache pasada por parametro
void procesar_acceso(cache_t* cache, acceso_t* acceso, metricas_t* metricas);

//Procesa la instruccion de acceso pasada por parametro en la cache pasada por parametro, EN MODO VERBOSO
void procesar_acceso_verboso(cache_t* cache, acceso_t* acceso, metricas_verboso_t* metricas_verboso, int cont);

#endif //CACHE_H