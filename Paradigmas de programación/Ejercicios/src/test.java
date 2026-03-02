public class test {
    public static void main(String[] args) {
        fraccion f = new fraccion(4, 10);
        fraccion g = new fraccion(1, 3);
        System.out.println(f.imprimir());
        System.out.println(g.imprimir());
        fraccion h = f.sumar(g);
        System.out.println(h.imprimir());
        fraccion j = f.dividir(g);
        System.out.println(j.imprimir());
    }
}
