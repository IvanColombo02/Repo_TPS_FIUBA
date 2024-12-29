#!/usr/bin/python3
import procesamientoSistemaMusical as procesamientoMusical
import sys

CANT_ARGS = 2
COMANDO_CAMINO = "camino"
COMANDO_CICLO = "ciclo"
COMANDO_IMPORTANTES = "mas_importantes"
COMANDO_RANGO = "rango"
COMANDO_RECOMENDACION = "recomendacion"
ENCODING = "UTF-8"
ERROR_COMANDO = "Comando incorrecto"
ERROR_PARAMETRO = "Error, debe ingresar un archivo como parámetro"
MODO = "r"
SEP_ESPACIO = " "
SEP_GT = " >>>> "
SEP_TAB = "\t"

def recomendify():
    if len(sys.argv) != CANT_ARGS:
        print(ERROR_PARAMETRO)
        return

    sistema = procesamientoMusical.SistemaMusical()
    with open(sys.argv[1], MODO, encoding=ENCODING) as archivo:
        archivo.readline()
        for linea in archivo:
            _procesarLinea(linea.strip(), sistema)

    for comando in sys.stdin:
        _procesar_comando(comando.strip(), sistema)

def _procesarLinea(linea, sistema):
    partes = linea.split(SEP_TAB)
    sistema.procesar_entrada(partes[1], partes[2], partes[3], int(partes[4]), partes[5])

def _procesar_comando(comando, sistema):
    partes = comando.split(SEP_ESPACIO, 1)

    if len(partes) < 2:
        _error_comando()
    elif partes[0] == COMANDO_CAMINO:
        _procesar_camino(sistema, partes[1])
    elif partes[0] == COMANDO_CICLO:
        _procesar_ciclo(sistema, partes[1])
    elif partes[0] == COMANDO_IMPORTANTES:
        _procesar_importantes(sistema, partes[1])
    elif partes[0] == COMANDO_RANGO:
        _procesar_rango(sistema, partes[1])
    elif partes[0] == COMANDO_RECOMENDACION:
        _procesar_recomendacion(sistema, partes[1])
    else:
        _error_comando()

def _procesar_camino(sistema, parametros):
    canciones = parametros.split(SEP_GT)
    if not _validar_len_partes(canciones, 2):
        return
    sistema.camino_minimo(canciones[0], canciones[1])

def _procesar_ciclo(sistema, parametros):
    partes = parametros.split(SEP_ESPACIO, 1)
    if not _validar_len_partes(partes, 2):
        return
    sistema.ciclo_canciones(partes[1], int(partes[0]))

def _procesar_importantes(sistema, parametros):
    sistema.mas_importantes(int(parametros))

def _procesar_rango(sistema, parametros):
    partes = parametros.split(SEP_ESPACIO, 1)
    if not _validar_len_partes(partes, 2):
        return
    sistema.rango(partes[1], int(partes[0]))

def _procesar_recomendacion(sistema, parametros):
    partes = parametros.split(SEP_ESPACIO, 2)
    if not _validar_len_partes(partes, 3):
        return
    sistema.recomendacion(int(partes[1]), partes[0], partes[2].split(SEP_GT))

def _error_comando():
    print(ERROR_COMANDO)

def _validar_len_partes(partes, cantidad):
    if len(partes) < cantidad:
        _error_comando()
        return False
    return True

if __name__ == "__main__":  
    recomendify()