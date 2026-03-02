# Trabajo Práctico 3 - Comunidades NP-Completas

En este repositorio se encuentra el código implementado para resolver el problema de forma óptima, las aproximaciones, sets de datos e informe que detalla el razonamiento, codificación, mediciones y conclusión. 

## Ejecución de los programas

Todos los programas que, o bien resuelven el problema o bien dan una aproximación del mismo, reciben por parámetro la ruta del archivo para formar el grafo. Éste tiene que tener una arista por línea. Por consola se leerá la resolución del problema: una línea por clúster y al final el diámetro máximo.

### Backtracking

```bash
python3 ./Algoritmos_y_heramientas/tp3.py C:/ruta/al/archivo_grafo.txt <cantidad de clústeres>
```
### Programación Lineal

```bash
python3 ./Algoritmos_y_heramientas/implementacion_pl.py C:/ruta/al/archivo_grafo.txt <cantidad de clústeres>
```
### Algoritmo de Louvain

```bash
python3 ./Algoritmos_y_heramientas/aproximacion_con_louvain.py C:/ruta/al/archivo_grafo.txt <cantidad de clústeres>
```

### Aproximación con Greedy

```bash
python3 ./Algoritmos_y_heramientas/aproximacion_greedy_inicial.py C:/ruta/al/archivo_grafo.txt <cantidad de clústeres>
```

### Aproximación con k-center

```bash
python3 ./Algoritmos_y_heramientas/aproximacion_con_kcenter.py C:/ruta/al/archivo_grafo.txt <cantidad de clústeres>
```

### Validador

Para demostrar que el problema de decisión se hallaba en NP se confeccionó el validador `validador.py`

### Otras herramientas
- Implementación de grafo utilizada, `grafo.py`
- Notebook usado para medir la performance y graficar las soluciones `Medidor.ipynb`
- Generadores de ejemplos `generador.py`

    Para el trabajo usamos un generador de redes complejas, el Barabási–Albert (BA). Para generar las pruebas, se deben indicar sus siglas, el tamaño deseado del grafo y un valor entero para el parámetro m (el número de aristas que unen a cada nuevo vértice con los anteriores). Con este generador también se pueden crear pruebas con el modelo Watts-Strogatz (WS) (que tiene tres parámetros) y Erdös–Rényi (ER).

    ```bash
    python3 ./Algoritmos_y_heramientas/generador.py <siglas del generador> <cantidad de vértices> <parámetro1> <parámetro2>
    ```

### Extras
- Apunte sobre K-Center
