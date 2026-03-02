import unittest
from TP1.tp1 import *

RED = "\033[91m"
GREEN = "\033[92m"
RESET = "\033[0m"
RUTA = 'Archivos_para_tests_automatizados/{}'

def _verificar_compatibilidad(solucion):
    for timestamp, aproximacion, error in solucion:
        inicio = aproximacion - error
        fin = aproximacion + error

        if timestamp < inicio or timestamp > fin:
            return False
    return True

class TestTp1(unittest.TestCase):
    def test_todos_compatibles(self):
        """
            Testea qué sucede cuando todos los timestamps son compatibles
            con todos los intervalos.
        """
        s = [50, 120, 300, 301, 800]
        t = [(200, 600), (300, 550), (100, 1000), (300, 550), (2000, 1950)]
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 5)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_ninguno_compatible(self):
        """
            Testea qué sucede cuando ningún timestamp es compatible
            con ningún intervalo.
        """
        s = [321, 654, 876]
        t = [(1000, 120), (200, 100), (2000, 10)]
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_seleccion_optima(self):
        """
            Testea el caso en el que una selección intermedia determina
            el hallazgo de una solución optima global.
        """

        # En este caso, si al 125 se le asigna (200, 200), cuando se
        # llegue al 280 no habría solución.
        s = [125, 280]
        t = [(200,200), (100,30)]
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 2)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_intervalo_no_se_asigna_doble(self):
        """
            Testea que intervalo no pueda ser asignado dos veces para
            hallar una solución.
        """
        s = [15, 22]
        t = [(15, 10), (100,30)]
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_10000_es(self):
        """
            Test de volúmen 10000 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("10000-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 10000)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_30000_es(self):
        """
            Test de volúmen 30000 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("30000-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 30000)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_1000000_es(self):
        """
            Test de volúmen ¡1.000.000! que encuentra solución optima en O(n*log(n)).
        """
        t, s = procesar_archivo_entrada(RUTA.format("1000000-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 1000000)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_2000000_es(self):
        """
            Test de volúmen ¡2.000.000! que encuentra solución optima en O(n*log(n)).
        """
        t, s = procesar_archivo_entrada(RUTA.format("2000000-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 2000000)
        self.assertTrue(_verificar_compatibilidad(solucion))

    # Pruebas provistas por el curso:
    # NOTA: No se comparan los resltados esperados provistos ya que
    # se puede obtener más de una solución óptima.
    def test_5_es(self):
        """
            Test de volúmen 5 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("5-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 5)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_10_es(self):
        """
            Test de volúmen 10 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("10-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 10)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_10_es_bis(self):
        """
            Test de volúmen 10 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("10-es-bis.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 10)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_50_es(self):
        """
            Test de volúmen 50 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("50-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 50)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_100_es(self):
        """
            Test de volúmen 100 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("100-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 100)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_500_es(self):
        """
            Test de volúmen 500 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("500-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 500)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_1000_es(self):
        """
            Test de volúmen 1000 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("1000-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 1000)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_5000_es(self):
        """
            Test de volúmen 5000 que encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("5000-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 5000)
        self.assertTrue(_verificar_compatibilidad(solucion))

    def test_5_no_es(self):
        """
            Test de volúmen 5 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("5-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_10_no_es(self):
        """
            Test de volúmen 10 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("10-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_10_no_es_bis(self):
        """
            Test de volúmen 10 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("10-no-es-bis.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_50_no_es(self):
        """
            Test de volúmen 50 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("50-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_100_no_es(self):
        """
            Test de volúmen 100 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("100-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_500_no_es(self):
        """
            Test de volúmen 500 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("500-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_1000_no_es(self):
        """
            Test de volúmen 1000 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("1000-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

    def test_5000_no_es(self):
        """
            Test de volúmen 5000 que no encuentra solución optima.
        """
        t, s = procesar_archivo_entrada(RUTA.format("5000-no-es.txt"))
        solucion = verificar_coincidencia_timestamps(t, s)
        self.assertIsNone(solucion)

class ColoredTestResult(unittest.TextTestResult):
    def getDescription(self, test):
        doc = test.shortDescription()
        if doc:
            return doc
        return str(test)
    
    def addSuccess(self, test):
        super().addSuccess(test)
        self.stream.writeln(f"{self.getDescription(test)} - {GREEN}OK{RESET}")

    def addFailure(self, test, err):
        super().addFailure(test, err)
        self.stream.writeln(f"{self.getDescription(test)} - {RED}ERROR{RESET}")

class ColoredTestRunner(unittest.TextTestRunner):
    resultclass = ColoredTestResult

def main():
    # Usa nuestro runner coloreado
    runner = ColoredTestRunner(verbosity=1)
    unittest.main(testRunner=runner, exit=False)

if __name__ == "__main__":
    main()