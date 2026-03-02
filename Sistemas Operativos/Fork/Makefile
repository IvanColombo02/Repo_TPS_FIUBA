CFLAGS := -ggdb3 -O2 -Wall -Wextra -std=c11
CFLAGS += -Wvla
CPPFLAGS := -D_DEFAULT_SOURCE

PROGS := primes xargs find copy list process_status

all: $(PROGS)

xargs: xargs.o
primes: primes.o
find: find.o
copy: copy.o
list: list.o
process_status: process_status.o

test:
	./tests/run
	pytest -v tests/test_basicos.py

format:
	clang-format -i primes.c xargs.c copy.c list.c find.c process_status.c

clean:
	rm -f $(PROGS) *.o core vgcore.*

.PHONY: all clean format test
