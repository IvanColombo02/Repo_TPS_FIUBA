public class fraccion {

    public static final String NEGATIVO = "-%d/%d";
    public static final String POSITIVO = "%d/%d";
    public static final String ENTERO = "%d";
    public static final String ERROR = "El denominador no puede ser 0";


    int numerador;
    int denominador;

    public fraccion(int numerador, int denominador) {
        if (denominador == 0) {
            throw new IllegalArgumentException(ERROR);
        }
        this.numerador = numerador;
        this.denominador = denominador;
    }

    public fraccion sumar(fraccion otro) {
        int num = this.numerador * otro.denominador + otro.numerador * this.denominador;
        int den = this.denominador * otro.denominador;
        return new fraccion(num, den);
    }

    public fraccion resta(fraccion otro) {
        int num = this.numerador * otro.denominador - otro.numerador * this.denominador;
        int den = this.denominador * otro.denominador;
        return new fraccion(num, den);
    }

    public fraccion multiplicar(fraccion otro) {
        int num = this.numerador * otro.numerador;
        int den = this.denominador * otro.denominador;
        return new fraccion(num, den);
    }

    public fraccion dividir(fraccion otro) {
        int num = this.numerador * otro.denominador;
        int den = this.denominador * otro.numerador;
        return new fraccion(num, den);
    }

    public int parteEntera() {
        return this.numerador / this.denominador;
    }

    public String imprimir(){
        // si numerador y denominador son el mismo numero con mismo o distinto signo
        if (numYdenIguales()) {
            return casoIguales();
        }

        // caso numerador = 0
        if (this.numerador == 0) {
            return "0";
        }

        int maxComDiv = euclides(this.numerador, this.denominador);

        // si es que no hay manera de simplificar
        if (maxComDiv == 1){
            return formatoFraccion(this.numerador, this.denominador);
        }

        
        int nuevoNum = this.numerador / maxComDiv;
        int nuevoDen = this.denominador / maxComDiv;

        // caso donde el mcd hace que el divisor sea 1
        if (nuevoDen == 1 || nuevoDen == -1) {
            return imprimirEntero(nuevoNum);
        }
        return formatoFraccion(nuevoNum, nuevoDen);
    }

    private boolean numYdenIguales() {
        return this.numerador == this.denominador || this.numerador*-1 == this.denominador || this.numerador == this.denominador*-1;
    }

    private String casoIguales() {
        if (this.numerador < 0 && this.denominador > 0 || this.numerador > 0 && this.denominador < 0) {
            return "-1";
        }
        return "1";
    }

    private int euclides(int a, int b) {
        if (b == 0) {
            return a;
        }
        return euclides(b, a%b);
    }

    private String imprimirEntero(int num) {
        return String.format(ENTERO, num);
    }

    private String formatoFraccion(int num, int den) {
        if (den < 0 && num > 0) {
            return String.format(NEGATIVO, num, den*-1);
        }
        if (den > 0 && num < 0) {
            return String.format(NEGATIVO, num*-1, den);
        }
        return String.format(POSITIVO, num, den);
    }
}
