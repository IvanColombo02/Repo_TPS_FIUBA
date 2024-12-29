
#include "archivador.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define _CANTIDAD_PARAMETROS_NO_VERBOSO 5
#define _CANTIDAD_PARAMETROS_VERBOSO 8

const char* _ERROR_ARCHIVO = "Error al abrir el archivo\n";
const char* _ARGUMENTOS_VERBOSO = "Argumentos n y m del modo verboso no cumplen 0 ≤ n ≤ m\n";
const char* _PARAMETROS_INVALIDOS = "Cantidad de parametros invalidos (4 o 7)\n";
const char* _PARAMETROS_NO_POTENCIA_DE_DOS = "Alguno de los parametros C, E o S no son potencia de dos\n";
const char* _MODO_VERBOSO_INVALIDO = "Modo verboso invalido\n";
const char* _MODO_VERBOSO = "-v";
const char* _MODO_LECTURA = "r";

// Devuelve 1 si n es potencia de 2, 0 en caso contrario
int es_potencia_de_dos(int n){
    return (n & (n - 1)) == 0;
}

acceso_t* leer_acceso(FILE *archivo) {
    acceso_t* acceso = malloc(sizeof(acceso_t));
    char linea[256];
    if (fgets(linea, sizeof(linea), archivo)) {
        sscanf(linea, "%x: %c %x %d %x",
               &acceso->instruction_pointer,
               &acceso->operacion,
               &acceso->direccion_de_memoria,
               &acceso->byte_contador,
               &acceso->data);
        return acceso;
    }
    else{
        free(acceso);
        return NULL;
    }
}

FILE* verificar_entrada(int argc , char *argv[]){
    if(argc != _CANTIDAD_PARAMETROS_NO_VERBOSO && argc != _CANTIDAD_PARAMETROS_VERBOSO){ 
        printf("%s",_PARAMETROS_INVALIDOS);
        return NULL;
    }

    char* archivo_traza = argv[1]; // Nombre del archivo de traza
    FILE *archivo = fopen(archivo_traza, _MODO_LECTURA); 
    if (!archivo) {
        perror(_ERROR_ARCHIVO);
        fclose(archivo);
        exit(EXIT_FAILURE);
    }

    // si alguno de los parámetros C, E o S no son potencia de dos
    int tamanio_cache = atoi(argv[2]);
    int asociatividad = atoi(argv[3]);
    int cant_sets = atoi(argv[4]);
    if(!es_potencia_de_dos(tamanio_cache) || !es_potencia_de_dos(asociatividad) || !es_potencia_de_dos(cant_sets)){
        printf("%s",_PARAMETROS_NO_POTENCIA_DE_DOS);
        fclose(archivo);
        return NULL;
    }

    // si el modo verboso no es -v
    char* entrada_verboso = argv[5];
    if(argc == _CANTIDAD_PARAMETROS_VERBOSO && strcmp(entrada_verboso, _MODO_VERBOSO) != 0){
        printf("%s", _MODO_VERBOSO_INVALIDO);
        fclose(archivo);
        return NULL;
    }

    // si los argumentos n y m del modo verboso no son números enteros que cumplan 0 ≤ n ≤ m
    int inicio = atoi(argv[6]);
    int fin = atoi(argv[7]);
    if(argc == _CANTIDAD_PARAMETROS_VERBOSO && ((inicio < 0 || fin < 0) || inicio > fin)){
        printf("%s", _ARGUMENTOS_VERBOSO);
        fclose(archivo);
        return NULL;
    }
    return archivo;
}