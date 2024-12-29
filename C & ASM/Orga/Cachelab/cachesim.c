#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include "src/archivador.h"
#include "src/cache.h"
#include "src/metricas.h"

const char* _MODO_ESCRITURA = "w";
const int _ENTRADA_INVALIDA = 1;

cache_t* cache;


// verifica si el modo verboso esta activado
int verboso(int argc){
    if(argc == 8){
        return 1;
    }
    return 0;
}

// limpia la memoria de la simulacion
void liberar_simulacion(cache_t *cache, metricas_t* metricas, FILE* archivo){
    fclose(archivo);
    liberar_cache(cache);
    liberar_metricas(metricas);
}

// limpia la memoria de la simulacion verbosa
void liberar_simulacion_verboso(cache_t *cache, metricas_verboso_t* metricas, FILE* archivo){
    fclose(archivo);
    liberar_cache(cache);
    liberar_metricas_verboso(metricas);
}

// procesa un acceso a la cache en modo verboso, imprime las metricas y libera la memoria 
void simulacion_verboso(FILE *archivo , int cache_tam, int asociatividad, int cant_sets, int inicio, int fin){
    cache_t* cache = crear_cache(cache_tam, asociatividad, cant_sets);
    metricas_verboso_t* metricas_verboso = crear_metricas_verboso();
    acceso_t* acceso = leer_acceso(archivo);
    int cont = inicio;
    while(acceso){
        procesar_acceso_verboso(cache, acceso, metricas_verboso, cont);
        imprimir_metricas_verboso(metricas_verboso, cache->cant_lineas_por_set);
        free(acceso);
        if (cont == fin)break;
        cont++;
        acceso = leer_acceso(archivo);
    }
    liberar_simulacion_verboso(cache, metricas_verboso, archivo);
}

// procesa un acceso a la cache, imprime las metricas y libera la memoria
void simulacion(FILE *archivo , int cache_tam, int asociatividad, int cant_sets){
    cache_t* cache = crear_cache(cache_tam, asociatividad, cant_sets);
    metricas_t* metricas = crear_metricas();
    acceso_t* acceso = leer_acceso(archivo);
    while(acceso){
        procesar_acceso(cache, acceso, metricas);
        free(acceso);
        acceso = leer_acceso(archivo);
    }
    imprimir_metricas(metricas, asociatividad, cant_sets, cache_tam/1024);
    liberar_simulacion(cache, metricas, archivo);
}

int main (int argc, char *argv[]){
    FILE *archivo = verificar_entrada(argc, argv);
    if(!archivo) return _ENTRADA_INVALIDA;
    int cache_tam = atoi(argv[2]);
    int asociatividad = atoi(argv[3]);
    int cant_sets = atoi(argv[4]);
    if (verboso(argc)){
        int inicio = atoi(argv[6]);   
        int fin = atoi(argv[7]);
        simulacion_verboso(archivo, cache_tam, asociatividad, cant_sets, inicio, fin);
    }
    else{
        simulacion(archivo, cache_tam, asociatividad, cant_sets);
    }
    return 0;
}


