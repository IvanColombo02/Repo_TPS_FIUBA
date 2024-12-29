#include "metricas.h"

#define PENALTY 100

const char* _ERROR_CREACION_METRICAS = "Error al crear las metricas.\n";
const char* _ERROR_CREACION_METRICAS_VERBOSO = "Error al crear las metricas en modo verboso.\n";

void actualizar_metricas_totales(metricas_t* metricas){
    metricas->total_accesses = metricas->loads + metricas->stores;
    metricas->total_misses = metricas->rmiss + metricas->wmiss;
    if (metricas->total_accesses > 0) 
        metricas->miss_rate = (double)metricas->total_misses / metricas->total_accesses;
    else metricas->miss_rate = 0.0;
}

void imprimir_metricas(metricas_t *metricas, int asociatividad, int cant_sets, int cache_tam){
    printf("%d-way, %d sets, size = %dKB\n", asociatividad, cant_sets, cache_tam);
    printf("loads %d stores %d total %d\n", metricas->loads, metricas->stores, metricas->total_accesses);
    printf("rmiss %d wmiss %d total %d\n", metricas->rmiss, metricas->wmiss, metricas->total_misses);
    printf("dirty rmiss %d dirty wmiss %d\n", metricas->dirty_rmiss, metricas->dirty_wmiss);
    printf("bytes read %d bytes written %d\n", metricas->bytes_read, metricas->bytes_written);
    printf("read time %d write time %d\n", metricas->read_time, metricas->write_time);
    printf("miss rate %f\n", metricas->miss_rate);
}

void imprimir_metricas_verboso(metricas_verboso_t *metricas_verboso, int asociatividad){
    printf("%d %d", metricas_verboso->indice_linea, metricas_verboso->indice_caso);
    if(metricas_verboso->indice_caso !=1) printf("%c", metricas_verboso->indentificador_caso);
    printf(" %x %x %d ",
        metricas_verboso->hexa_cache_index,
        metricas_verboso->hexa_cache_tag,
        metricas_verboso->cache_line
    );
    if (metricas_verboso->anterior_linea_tag == -1) printf("-1 ");
    else printf("%x ", metricas_verboso->anterior_linea_tag);
    printf("%d %d", metricas_verboso->valid_bit, metricas_verboso->dirty_bit); 
    if(asociatividad > 1) printf(" %d\n" ,metricas_verboso->ultimo_usado);
    else printf("\n");
}

metricas_t* crear_metricas(){
    metricas_t* metricas = calloc(1, sizeof(metricas_t));
    if(!metricas){
        printf("%s", _ERROR_CREACION_METRICAS);
        return NULL;
    }
    return metricas;
}

metricas_verboso_t* crear_metricas_verboso(){
    metricas_verboso_t* metricas_verboso = calloc(1, sizeof(metricas_verboso_t));
    if(!metricas_verboso){
        printf("%s", _ERROR_CREACION_METRICAS_VERBOSO);
        return NULL;
    }
    return metricas_verboso;
}

void liberar_metricas( metricas_t* metricas){
    free(metricas);
}

void liberar_metricas_verboso( metricas_verboso_t* metricas_verb){
    free(metricas_verb);
}