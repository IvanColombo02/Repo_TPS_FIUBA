CC=gcc
CFLAGS=-Wall -Wextra -pedantic -std=c11 -O0 -ggdb -no-pie
LIBS=-lm  # Corregido el error de sintaxis

VALGRIND_FLAGS=valgrind --leak-check=full --track-origins=yes --show-reachable=yes --error-exitcode=2 --show-leak-kinds=all --trace-children=yes

PUNTOS_C:= cachesim.c src/archivador.c src/metricas.c src/cache.c 
PUNTOS_O:= cachesim.o src/archivador.o src/metricas.o src/cache.o 
PUNTOS_H:= src/lector.h  src/metricas.h src/archivador.h  src/cache.h

TEST_1:= trazas/adpcm.xex
TEST_2:= trazas/blowfish.xex

# Códigos de color
RED = \033[31m
GREEN = \033[32m
YELLOW = \033[33m
BLUE = \033[34m
RESET = \033[0m

all: cachesim

cachesim: $(PUNTOS_O)
	@echo "${YELLOW}Ejecutando PUNTOS_O${RESET}"
	$(CC) $(CFLAGS) $^ -o $@ $(LIBS)

cachesim.o: cachesim.c
	$(CC) $(CFLAGS) -c $^ -o $@

archivador.o: archivador.c archivador.h
	$(CC) $(CFLAGS) -c archivador.c -o archivador.o

cache.o: cache.c cache.h
	$(CC) $(CFLAGS) -c cache.c -o cache.o

metricas.o: metricas.c metricas.h
	$(CC) $(CFLAGS) -c metricas.c -o metricas.o

clean:
	@echo "${RED}Limpiando ejecutable...${RESET}"
	rm -f *.o
	rm -f cachesim


