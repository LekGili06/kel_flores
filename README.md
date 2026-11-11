# Clínica Veterinaria PataVerde

Proyecto de la Evaluación Parcial 2 de **DSY1102 – Programación Orientada a Objetos** (DUOC UC).

Aplicación de escritorio en **Java 21** con **JavaFX** para registrar perros y gatos, calcular el costo de consulta de cada uno y guardar los datos en el archivo `mascotas.json` usando **Gson**.

## Funcionalidades

- Registrar perros (nombre, edad, peso, raza, vacunado, disponible para adopción) y gatos (nombre, edad, peso, sale al exterior).
- Ver todas las mascotas en una tabla con su costo de consulta.
- Eliminar la mascota seleccionada.
- Validación de todos los campos del formulario, con mensajes que explican el error.
- Los datos se guardan automáticamente en `mascotas.json` y se cargan al abrir la aplicación.

## Costo de consulta

| Tipo  | Condición          | Costo    |
|-------|--------------------|----------|
| Perro | Vacunado           | $15.000  |
| Perro | No vacunado (+30%) | $19.500  |
| Gato  | Interior           | $12.000  |
| Gato  | Sale al exterior (−15%) | $10.200 |

## Estructura

```
src/main/java/cl/duoc/pataverde/
├── Launcher.java            Punto de entrada
├── App.java                 Carga la vista principal
├── model/                   Mascota (abstracta), Perro, Gato, Adoptable
├── controller/              MainController (lógica de la vista)
└── dao/                     MascotaRepository, MascotaRepositoryJson, PersistenciaException
src/main/resources/cl/duoc/pataverde/view/
└── main-view.fxml           Vista creada con Scene Builder
```

## Persistencia

- Al leer el archivo, se validan los datos con las mismas reglas del modelo (nombre no vacío, edad entre 0 y 30, peso mayor que cero, raza obligatoria en perros).
- Si el archivo está dañado o tiene datos inválidos, la aplicación lo informa y **no guarda cambios** para no sobrescribirlo y perder información.
- Al guardar, se escribe primero un archivo temporal y luego se reemplaza el original, para que un error a mitad de la escritura no lo deje corrupto.

## Cómo ejecutar

1. Abrir el proyecto en IntelliJ IDEA como proyecto Maven (JDK 21).
2. Recargar Maven si lo pide (clic derecho en `pom.xml` > Maven > Reload Project).
3. Ejecutar la clase `Launcher`.

También se puede ejecutar con `mvn javafx:run`.

**Autor:** Kel Flores
