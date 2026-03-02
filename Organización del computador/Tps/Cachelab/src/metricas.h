#ifndef METRICAS_H
#define METRICAS_H
#include <stdlib.h>
#include <stdio.h>

typedef struct metricas{
    int loads; 
    int stores;
    int total_accesses;
    int rmiss;
    int wmiss;
    int total_misses;
    int dirty_rmiss;
    int dirty_wmiss;
    int bytes_read;
    int bytes_written;
    int read_time;
    int write_time;
    double miss_rate;
}metricas_t;

typedef struct metricas_verboso{
    int indice_linea;
    int indice_caso;
    char indentificador_caso;
    int hexa_cache_index;
    int hexa_cache_tag;
    int cache_line;
    int anterior_linea_tag;
    int valid_bit;
    int dirty_bit;
    int ultimo_usado;
} metricas_verboso_t;

//Actualiza los valores totales de los datos de metricas
void actualizar_metricas_totales(metricas_t* metricas);

//Imprime las metricas del modo verboso en la consola
void imprimir_metricas_verboso(metricas_verboso_t *metricas_verboso, int asociatividad);

//Imprime las metricas en la consola
void imprimir_metricas(metricas_t *metricas, int asociatividad, int cant_sets, int cache_tam);

// crea una nueva tabla de metricas
metricas_t* crear_metricas();

//Crea una nueva tabla de metricas en modo verboso
metricas_verboso_t* crear_metricas_verboso();

//Libera la memoria asignada para las metricas
void liberar_metricas(metricas_t* metricas);

// libera la memoria asignada para las metricas del modo verboso
void liberar_metricas_verboso(metricas_verboso_t* metricas_verboso);

#endif // METRICAS_H