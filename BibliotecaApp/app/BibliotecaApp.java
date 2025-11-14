package app;

import exceptions.LibroNoDisponibleException;
import exceptions.UsuarioSinCupoException;
import model.Libro;
import model.Prestamo;
import model.Usuario;
import service.Biblioteca;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BibliotecaApp {
    private static final Biblioteca biblioteca = new Biblioteca();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        seedDatos();
        boolean run = true;
        while (run) {
            mostrarMenu();
            int opt = leerEntero("Opción: ");
            try {
                switch (opt) {
                    case 1 -> opcionAgregarLibro();
                    case 2 -> opcionRegistrarUsuario();
                    case 3 -> opcionRealizarPrestamo();
                    case 4 -> opcionDevolverLibro();
                    case 5 -> opcionConsultarLibrosDisponibles();
                    case 6 -> opcionConsultarPrestamosUsuario();
                    case 7 -> opcionListarUsuariosConMultas();
                    case 8 -> opcionTopLibros();
                    case 9 -> { run = false; System.out.println("Saliendo..."); }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- Biblioteca ---");
        System.out.println("1. Agregar libro");
        System.out.println("2. Registrar usuario");
        System.out.println("3. Realizar préstamo");
        System.out.println("4. Devolver libro");
        System.out.println("5. Consultar libros disponibles");
        System.out.println("6. Consultar préstamos de usuario");
        System.out.println("7. Listar usuarios con multas");
        System.out.println("8. Top 5 libros más prestados");
        System.out.println("9. Salir");
    }

    private static void opcionAgregarLibro() {
        System.out.println("Agregar libro:");
        String isbn = leerTexto("ISBN (13 dígitos): ");
        String titulo = leerTexto("Título: ");
        String autor = leerTexto("Autor: ");
        int anio = leerEntero("Año: ");
        int total = leerEntero("Ejemplares totales: ");
        Libro l = new Libro(isbn, titulo, autor, anio, total);
        biblioteca.agregarLibro(l);
        System.out.println("Libro agregado: " + l);
    }

    private static void opcionRegistrarUsuario() {
        System.out.println("Registrar usuario:");
        String nombre = leerTexto("Nombre: ");
        String email = leerTexto("Email: ");
        Usuario u = new Usuario(nombre, email);
        biblioteca.registrarUsuario(u);
        System.out.println("Usuario registrado: " + u);
    }

    private static void opcionRealizarPrestamo() {
        int uid = leerEntero("ID usuario: ");
        String isbn = leerTexto("ISBN: ");
        try {
            Prestamo p = biblioteca.realizarPrestamo(uid, isbn);
            System.out.println("Préstamo realizado: " + p);
        } catch (LibroNoDisponibleException | UsuarioSinCupoException e) {
            System.out.println("No se pudo realizar el préstamo: " + e.getMessage());
        }
    }

    private static void opcionDevolverLibro() {
        int uid = leerEntero("ID usuario: ");
        String isbn = leerTexto("ISBN: ");
        biblioteca.devolverLibro(uid, isbn);
        System.out.println("Operación de devolución procesada.");
    }

    private static void opcionConsultarLibrosDisponibles() {
        List<Libro> disponibles = biblioteca.listarLibrosDisponibles();
        System.out.println("Libros disponibles:");
        disponibles.forEach(System.out::println);
    }

    private static void opcionConsultarPrestamosUsuario() {
        int uid = leerEntero("ID usuario: ");
        List<Prestamo> ps = biblioteca.obtenerPrestamosDeUsuario(uid);
        System.out.println("Préstamos del usuario " + uid + ":");
        ps.forEach(System.out::println);
    }

    private static void opcionListarUsuariosConMultas() {
        List<Usuario> lista = biblioteca.obtenerUsuariosConMultas();
        System.out.println("Usuarios con multas:");
        lista.forEach(System.out::println);
    }

    private static void opcionTopLibros() {
        List<Libro> top = biblioteca.obtenerTopLibrosPrestados(5);
        System.out.println("Top libros:");
        top.forEach(l -> System.out.printf("%s — Veces prestado: %d\n", l.getTitulo(), l.getVecesPrestado()));
    }

    // helpers de consola
    private static String leerTexto(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int leerEntero(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String line = sc.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Número inválido. Intenta otra vez.");
            }
        }
    }

    private static void seedDatos() {
        // datos de ejemplo
        biblioteca.agregarLibro(new Libro("9780306406157", "El Principito", "A. Saint-Exupéry", 1943, 5));
        biblioteca.agregarLibro(new Libro("9788497592208", "Cien Años de Soledad", "G. G. Márquez", 1967, 3));
        Usuario u1 = new Usuario("Juan Perez", "juan@example.com");
        Usuario u2 = new Usuario("María Lopez", "maria@example.com");
        biblioteca.registrarUsuario(u1);
        biblioteca.registrarUsuario(u2);
    }
}