# Trabajo Práctico 2 - Que parezca programación dinámica

En este repositorio se encuentra el código implementado para resolver el problema, tests e informe que detalla el razonamiento, codificación, mediciones y conclusión. 

## Ejecución de los programas

### Algoritmo

El programa principal, el que resuelve el problema por progamación dinámica, recibe por parámetro la ruta del archivo diccionario. Éste tiene que tener una palabra válida por línea. Una vez ejecutado el comando, la consola queda a la espera de una cadena por stdin. Por consola se leerá la resolución del problema: un posible mensaje o la leyenda "No es un mensaje" en caso de que no se pueda segmentar la cadena.

```bash
python3 tp2.py C:/ruta/al/archivo_diccionario.txt
```

También se puede pasar un archivo con múltiples cadenas, una por línea. Por consola se leerá la posible decodificación, una por línea.

```bash
python3 tp2.py C:/ruta/al/archivo_diccionario.txt < C:/ruta/al/archivo_cadenas.txt
```


### Validación automática

El programa `tests_automatizados.py` ejecuta el algoritmo de programación dinámica haciendo uso de los ejemplos provistos por la cátedra y que creamos manualmente para probar algunos casos particulares. Todos estos ejemplos se encuentran en la carpeta `Archivos`. 

Dentro del programa que realiza las pruebas se corrobora que los resultados sean los deseados. En caso de no haber solución chequea que la salida sea la esperada ("No es un mensaje") y en caso de que sí la haya verifica que la longitud de la misma sea mayor a 1 ya que puede haber más de una solución posible y no se la puede contrastar contra un string en particular.

Para correr las pruebas se debe utilizar el siguiente comando:

```bash
python3 tests_automatizados.py
```
### Generadores de ejemplos

Para el tabajo usamos dos generadores, uno que usa a supergigante.txt como diccionario y a partir de él confecciona cadenas de los largos especificados por defecto en el main. Las guarda en una ruta que tiene seteada como constante. En caso de querer usar otro diccionario para generar cadenas, se puede cambiar la ruta de la constante que le corresponde.

```bash
python3 ./Generadores/generador_bt.py 
```

El otro generador recibe el tamaño de las cadenas a generar, la cantidad de cadenas, dónde se deben guardar las cadenas, dónde el diccionario generado, la dirección de un diccionario si se desea utilizar uno por defecto y un tipo de generador. Las variantes para este último parámetro son "RANDOM", "RANDOM_DICT", "CORTAS", "LARGAS". Para el trabajo solamente lo usamos en su variante RANDOM para generar un diccionario con palabras completamente aleatorias. 


```bash
python3 ./Generadores/generador_random.py <tamaño de cadena> <cantidad de cadenas> <ruta de salida del archivo de cadenas> <ruta de salida del archivo diccionario> <ruta de entrada del archivo diccionario> <tipo_de_generador>
```

