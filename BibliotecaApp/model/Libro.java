package model;

import exceptions.InvalidDataException;
import exceptions.LibroNoDisponibleException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Libro {
    private final String isbn; // usar String para preservar ceros y validar longitud
    private String titulo;
    private String autor;
    private int anio; // primitivo: año no debe ser null
    private final AtomicInteger ejemplaresTotales = new AtomicInteger(0);
    private final AtomicInteger ejemplaresDisponibles = new AtomicInteger(0);
    private final AtomicInteger vecesPrestado = new AtomicInteger(0);

    public Libro(String isbn, String titulo, String autor, int anio, int total) {
        if (!validarIsbn(isbn)) throw new InvalidDataException("ISBN inválido (debe ser 13 dígitos).");
        if (anio < 1450 || anio > java.time.LocalDate.now().getYear()) throw new InvalidDataException("Año inválido.");
        if (total < 0) throw new InvalidDataException("Ejemplares totales no puede ser negativo.");
        this.isbn = isbn;
        this.titulo = Objects.requireNonNull(titulo);
        this.autor = Objects.requireNonNull(autor);
        this.anio = anio;
        this.ejemplaresTotales.set(total);
        this.ejemplaresDisponibles.set(total);
    }

    public static boolean validarIsbn(String isbn) {
        return isbn != null && isbn.matches("\\d{13}");
    }

    public synchronized void prestar() throws LibroNoDisponibleException {
        if (ejemplaresDisponibles.get() <= 0) throw new LibroNoDisponibleException("No hay ejemplares disponibles de: " + titulo);
        ejemplaresDisponibles.decrementAndGet();
        vecesPrestado.incrementAndGet();
    }

    public synchronized void devolver() {
        if (ejemplaresDisponibles.get() < ejemplaresTotales.get()) {
            ejemplaresDisponibles.incrementAndGet();
        }
    }

    public boolean estaDisponible() {
        return ejemplaresDisponibles.get() > 0;
    }

    // getters
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getAnio() { return anio; }
    public int getEjemplaresTotales() { return ejemplaresTotales.get(); }
    public int getEjemplaresDisponibles() { return ejemplaresDisponibles.get(); }
    public int getVecesPrestado() { return vecesPrestado.get(); }

    // setters si se necesitan
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setAnio(int anio) {
        if (anio < 1450 || anio > java.time.LocalDate.now().getYear()) throw new InvalidDataException("Año inválido.");
        this.anio = anio;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s (%d) — Disponibles: %d",
                isbn, titulo, autor, anio, ejemplaresDisponibles.get());
    }
}