package cl.duoc.pataverde.model;

public class Perro extends Mascota implements Adoptable {
    private static final double COSTO_BASE = 15000;
    private static final double RECARGO_NO_VACUNADO = 0.30;

    private String raza;
    private boolean vacunado;
    private boolean disponibleAdopcion;

    public Perro() {
    }

    public Perro(String nombre, int edad, double peso, String raza, boolean vacunado) {
        super(nombre, edad, peso);
        setRaza(raza);
        setVacunado(vacunado);
        this.disponibleAdopcion = false;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public boolean isVacunado() {
        return vacunado;
    }

    public void setVacunado(boolean vacunado) {
        this.vacunado = vacunado;
    }

    @Override
    public boolean estaDisponibleParaAdopcion() {
        return disponibleAdopcion;
    }

    @Override
    public void marcarDisponibleParaAdopcion() {
        this.disponibleAdopcion = true;
    }

    @Override
    public double calcularCostoConsulta() {
        if (vacunado) {
            return COSTO_BASE;
        }
        return COSTO_BASE + COSTO_BASE * RECARGO_NO_VACUNADO;
    }

    @Override
    public String getTipo() {
        return "Perro";
    }
}
