package cl.duoc.pataverde.model;

public abstract class Mascota {
    private String nombre;
    private int edad;
    private double peso;

    protected Mascota() {
    }

    public Mascota(String nombre, int edad, double peso) {
        setNombre(nombre);
        setEdad(edad);
        setPeso(peso);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío.");
        }
        this.nombre = nombre.trim();
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        if (edad < 0 || edad > 30) {
            throw new IllegalArgumentException("La edad debe estar entre 0 y 30 años.");
        }
        this.edad = edad;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que cero.");
        }
        this.peso = peso;
    }

    public abstract double calcularCostoConsulta();

    public abstract String getTipo();

    @Override
    public String toString() {
        return "Nombre: " + nombre + " | Edad: " + edad + " años";
    }
}
