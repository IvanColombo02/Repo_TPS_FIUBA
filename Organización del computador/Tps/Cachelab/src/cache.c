#include "cache.h"
#include "metricas.h"
#include <math.h>
#include <stdio.h>
#include <stdlib.h>

#define PENALTY 100
#define WRITE 'W'
#define READ 'R'
#define HIT 1
#define MISS 2
#define HIT_OP ' '
#define CLEAN_MISS 'a'
#define DIRTY_MISS 'b'
#define _ULTIMO_USADO_POR_DEFECTO -1
#define _LRU_INDEX_POR_DEFECTO 0
#define _TAM_DIR_MEMORIA 32

const char* _ERROR_CREACION_CACHE = "Error al crear la cache.\n";

/* PROCESAR CACHE */


void liberar_cache(cache_t* cache){
    for (int i = 0; i < cache->cant_sets; i++) {
        set_t* set = &cache->sets[i];
        
        if (set->lineas) {
            for (int j = 0; j < cache->cant_lineas_por_set; j++) {
                free(set->lineas[j].bloque);
            }
            free(set->lineas);
        }
    }
    free(cache->sets);
    free(cache);  
}

// Crea una linea de memoria cache
linea_t* crear_lineas(int cant_lineas_por_set, int tamanio_bloque){
    linea_t* lineas = calloc(cant_lineas_por_set, sizeof(linea_t));
    if(!lineas)return NULL;
    lineas->bloque = calloc(tamanio_bloque , sizeof(char));
    if(!lineas->bloque){
        free(lineas);
        return NULL;
    }
    lineas->ultimo_usado = _ULTIMO_USADO_POR_DEFECTO;
    return lineas;
}

cache_t* crear_cache(int tamanio_bytes, int cant_lineas_por_set, int num_sets){
    cache_t* cache = malloc(sizeof(cache_t));
        if(!cache){
        printf("%s", _ERROR_CREACION_CACHE);
        return NULL;
    }
    cache->sets = malloc(sizeof(set_t)*num_sets);
    if(!cache->sets) free(cache);
    
    cache->cant_sets = num_sets;
    cache->cant_lineas_por_set = cant_lineas_por_set;
    cache->tamanio_bloque= tamanio_bytes/(cant_lineas_por_set*num_sets);

    for(int i = 0; i < num_sets; i++){  
        cache->sets[i].lineas = crear_lineas(cant_lineas_por_set, cache->tamanio_bloque);
        if(!cache->sets[i].lineas) liberar_cache(cache);
    }
    return cache;
}


//Consigue la linea que se busca a traves de la direccion y cache
//en caso de no estar, cambiara las variables de ultimo_usado y lru_index
linea_t* conseguir_linea(cache_t* cache, set_t* set, direccion_t direccion, int* ultimo_usado, int* lru_index) {
    linea_t* linea = NULL;
    for (int i = 0; i < cache->cant_lineas_por_set; i++) {
        linea_t* actual = &set->lineas[i];

        if (actual->valido && actual->tag == direccion.tag) {
            linea = actual;
            *lru_index = i;
            break;
        }

        if (actual->cant_veces_no_usado > *ultimo_usado) {
            *ultimo_usado = actual->cant_veces_no_usado;
            *lru_index = i;
        }
    }
    return linea;
}


/* PROCESAR HIT Y MISS */


void procesar_hit(linea_t* linea, acceso_t* acceso, metricas_t* metricas){
    if(acceso->operacion == WRITE){
        metricas->write_time += 1;
        linea->dirty = 1;
    }
    else metricas->read_time += 1;
    linea->cant_veces_no_usado = 0; //Reinicia el contador de las veces que no se a usado
}

void procesar_hit_verboso(linea_t* linea, acceso_t* acceso, metricas_verboso_t* metricas_verboso){
    metricas_verboso->indice_caso = HIT;
    metricas_verboso->indentificador_caso = HIT_OP;
    // Chequeo los casos de line tag
    if (linea->valido){
        metricas_verboso->anterior_linea_tag = linea->tag;
        metricas_verboso->valid_bit = 1;
    }
    else{
        metricas_verboso->anterior_linea_tag = -1;
        metricas_verboso->valid_bit = 0;
    }
    if(linea->dirty == 1){
        metricas_verboso->dirty_bit = 1;
    } else {
        metricas_verboso->dirty_bit = 0;
    }
    if (acceso->operacion == WRITE){
        linea->dirty = 1;
    }        
    linea->cant_veces_no_usado = 0; // Reinicia el contador de las veces que no se ha usado 
}

void procesar_miss(cache_t* cache, acceso_t* acceso, metricas_t* metricas, direccion_t direccion, linea_t* linea){
    metricas->bytes_read += cache->tamanio_bloque;
    if (linea->dirty) {
        if (acceso->operacion == READ){
            metricas->read_time += 1 + 2*PENALTY;
            metricas->dirty_rmiss++;
        }
        else{
            metricas->write_time += 1 + 2*PENALTY;
            metricas->dirty_wmiss++;
        }
        metricas->bytes_written += cache->tamanio_bloque;
    }
    else{
        if(acceso->operacion == READ)
            metricas->read_time += 1 + PENALTY;
        else metricas->write_time += 1 + PENALTY;
    }
    if (acceso->operacion == READ){
        linea->dirty = 0;
        metricas->rmiss++;
    }
    else{
        linea->dirty = 1;
        metricas->wmiss++;
    }
    linea->valido = 1;
    linea->tag = direccion.tag;
    linea->cant_veces_no_usado = 0;
}

void procesar_miss_verboso(linea_t* linea, acceso_t* acceso, metricas_verboso_t* metricas_verboso, direccion_t direccion){
    metricas_verboso->valid_bit = linea->valido;
        if (linea->valido){
            metricas_verboso->anterior_linea_tag = linea->tag;
            metricas_verboso->valid_bit = 1;
        }else{
            metricas_verboso->anterior_linea_tag = -1;
            metricas_verboso->valid_bit = 0;
        }
        if (linea->dirty) {
            metricas_verboso->indice_caso = MISS;
            metricas_verboso->indentificador_caso = DIRTY_MISS;
            metricas_verboso->dirty_bit = 1;
        }
        else{
            metricas_verboso->indice_caso = MISS;
            metricas_verboso->indentificador_caso = CLEAN_MISS;
            metricas_verboso->dirty_bit = 0;
        }
        if (acceso->operacion == READ){
            linea->dirty = 0;
        }
        else{
            linea->dirty = 1;
        }
        linea->valido = 1;
        linea->tag = direccion.tag;
        linea->cant_veces_no_usado = 0;
}


/* PROCESAR ACCESOS */


// Si la operacion es un read aumenta el contador de loads, si es un write aumenta el contador de stores
void actualizar_metrica_simple(acceso_t* acceso , metricas_t* metricas){
    if(acceso->operacion == READ)
        metricas->loads++;
    else
        metricas->stores++;
}

// incrementa la cantidad de veces que una linea no fue utilizada 
void aumentar_contador_no_usados(cache_t* cache, set_t* set, linea_t* linea){
    for (int i = 0; i < cache->cant_lineas_por_set; i++) 
        if (&set->lineas[i] != linea) 
            set->lineas[i].cant_veces_no_usado++;
}

// Parsea una direccion de memoria en sus componentes tag, set y bloque
direccion_t decodificar_direccion(unsigned int direccion_de_memoria, int bits_bloque, int bits_set) {
    direccion_t dir;
    int bits_tag = _TAM_DIR_MEMORIA  - bits_bloque - bits_set;
    unsigned int mask_bloque = (1 << bits_bloque) - 1;
    unsigned int mask_set = (1 << bits_set) - 1;
    unsigned int mask_tag = (1 << bits_tag) - 1;

    dir.bloque = direccion_de_memoria & mask_bloque;
    dir.set = (direccion_de_memoria >> bits_bloque) & mask_set;
    dir.tag = (direccion_de_memoria >> (bits_bloque + bits_set)) & mask_tag;
    return dir;
}

void procesar_acceso(cache_t* cache, acceso_t* acceso, metricas_t* metricas){
    int bits_bloque = log2(cache->tamanio_bloque);
    int bits_set = log2(cache->cant_sets);
    direccion_t direccion = decodificar_direccion(acceso->direccion_de_memoria, bits_bloque, bits_set);

    set_t* set = &cache->sets[direccion.set];
    int lru_index = _LRU_INDEX_POR_DEFECTO; 
    int ultimo_usado = _ULTIMO_USADO_POR_DEFECTO;
    linea_t* linea = conseguir_linea(cache, set, direccion, &ultimo_usado, &lru_index);

    actualizar_metrica_simple(acceso, metricas);

    if (linea) { // Si encontro la linea => es un hit
        procesar_hit(linea, acceso, metricas);
    }
    else { //Sino, es un miss
        linea = &set->lineas[lru_index];
        procesar_miss(cache, acceso, metricas, direccion, linea);
    }
    aumentar_contador_no_usados(cache, set, linea);
    actualizar_metricas_totales(metricas);
}


void procesar_acceso_verboso(cache_t* cache, acceso_t* acceso, metricas_verboso_t* metricas_verboso, int cont){
    int bits_bloque = log2(cache->tamanio_bloque);
    int bits_set = log2(cache->cant_sets);
    direccion_t direccion = decodificar_direccion(acceso->direccion_de_memoria, bits_bloque, bits_set);

    set_t* set = &cache->sets[direccion.set];
    int lru_index = _LRU_INDEX_POR_DEFECTO;
    int ultimo_usado = _ULTIMO_USADO_POR_DEFECTO;
    
    linea_t* linea = conseguir_linea(cache, set, direccion, &ultimo_usado, &lru_index);
                                                                 
    metricas_verboso->indice_linea = cont;
    metricas_verboso->hexa_cache_index = direccion.set;                                 
    metricas_verboso->hexa_cache_tag = direccion.tag;
    metricas_verboso->cache_line = lru_index;

    if (linea){ // Si encontró la línea => es un hit
        procesar_hit_verboso(linea, acceso, metricas_verboso);
    }
    else { // Si no, es un miss
        linea = &set->lineas[lru_index]; // Obtengo la línea del menos usado
        procesar_miss_verboso(linea, acceso, metricas_verboso, direccion);
    }

    if (linea->ultimo_usado != -1){
        metricas_verboso->ultimo_usado = linea->ultimo_usado;
    } else {
        metricas_verboso->ultimo_usado = 0;
    }

    linea->ultimo_usado = cont;
    aumentar_contador_no_usados(cache, set, linea);
}