# Trabajo Práctico 1 - La mafia de los algoritmos greedy

En este repositorio se encuentra el código implementado para resolver el problema, tests e informe que detalla el razonamiento, codificación, mediciones y conclusión. 

## Ejecución de los programas

### Algoritmo

El programa principal, el que resuelve el problema de forma greedy, recibe por parámetro la ruta del archivo que contiene los tiempos aproximados junto a su error y los timestamps del sospechoso. Imprime los resultados por salida estándar. Por ejemplo,

```bash
python3 tp1.py C:/ruta/al/archivo.txt
```

En caso de no proporcionarse una ruta, el programa se ejecutará con un ejemplo por defecto.

### Validación automática

El programa `tests_automatizados.py` ejecuta el algoritmo greedy haciendo uso de los ejemplos provistos por la cátedra y que creamos manualmente para probar algunos casos particulares. Todos estos ejemplos se encuentran en la carpeta `Archivos_para_tests_automatizados`. 

Dentro del programa que realiza las pruebas se corrobora que los resultados sean los deseados. En caso de no haber solución chequea que la salida sea la esperada (None) y en caso de que sí la haya verifica que los timestamps del sospechoso estén dentro del intervalo elegido. Al usar los archivos provistos por la cátedra no se comparan directamente los resultados con el archivo `Resultados Esperados.txt` ya que puede haber más de una solución óptima y en ese caso el test arrojaría un falso negativo.

Para correr las pruebas se debe utilizar el siguiente comando:

```bash
python3 tests_automatizados.py
```
### Generadores de ejemplos

Los generadores se ejecutan pasando como parámetro el generador elegido, el volumen deseado y el nombre del archivo, por ejemplo:

```bash
python3 ./Generadores_de_pruebas/generador_baja_variabilidad.py 1000 1000-es.txt
```

El formato del nombre del archivo "volumen-es" solamente es necesario si se desea utilizar el ejemplo en el notebook de mediciones.
