package cl.duoc.pataverde.model;

public class Gato extends Mascota {
    private static final double COSTO_BASE = 12000;
    private static final double RECARGO_EXTERIOR = 0.15;

    private boolean exterior;

    public Gato() {
    }

    public Gato(String nombre, int edad, double peso, boolean exterior) {
        super(nombre, edad, peso);
        setExterior(exterior);
    }

    public boolean isExterior() {
        return exterior;
    }

    public void setExterior(boolean exterior) {
        this.exterior = exterior;
    }

    @Override
    public double calcularCostoConsulta() {
        if (exterior) {
            return COSTO_BASE - COSTO_BASE * RECARGO_EXTERIOR;
        }
        return COSTO_BASE;
    }

    @Override
    public String getTipo() {
        return "Gato";
    }
}
