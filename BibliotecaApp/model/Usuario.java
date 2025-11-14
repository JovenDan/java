package model;

import exceptions.InvalidDataException;
import exceptions.UsuarioSinCupoException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Usuario {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1000);

    private final int id; // primitivo: id autogenerado
    private String nombre;
    private String email;
    private final List<String> isbnPrestados = new ArrayList<>(); // guardamos ISBN de libros prestados
    private BigDecimal multas = BigDecimal.ZERO;

    private static final int MAX_LIBROS = 3;
    private static final BigDecimal MULTA_LIMITE = new BigDecimal("5000"); // regla: multa <= 5000

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}$");

    public Usuario(String nombre, String email) {
        if (nombre == null || nombre.isBlank()) throw new InvalidDataException("Nombre inválido.");
        if (!validarEmail(email)) throw new InvalidDataException("Email inválido.");
        this.id = ID_GENERATOR.getAndIncrement();
        this.nombre = nombre;
        this.email = email.toLowerCase(Locale.ROOT);
    }

    public static boolean validarEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public synchronized boolean puedePedirPrestado() {
        return isbnPrestados.size() < MAX_LIBROS && multas.compareTo(MULTA_LIMITE) <= 0;
    }

    public synchronized void agregarPrestamo(String isbn) throws UsuarioSinCupoException {
        if (!puedePedirPrestado()) throw new UsuarioSinCupoException("Usuario no puede pedir más libros o tiene multa alta.");
        isbnPrestados.add(isbn);
    }

    public synchronized void quitarPrestamo(String isbn) {
        isbnPrestados.remove(isbn);
    }

    public synchronized void agregarMulta(BigDecimal importe) {
        if (importe == null || importe.signum() <= 0) return;
        multas = multas.add(importe);
        if (multas.compareTo(MULTA_LIMITE) > 0) {
            // no lanzamos aquí, simplemente se registra que excedió; reglas de negocio pueden impedir más préstamos
        }
    }

    public synchronized void pagarMultas(BigDecimal pago) {
        if (pago == null || pago.signum() <= 0) return;
        multas = multas.subtract(pago);
        if (multas.signum() < 0) multas = BigDecimal.ZERO;
    }

    // getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public List<String> getIsbnPrestados() { return Collections.unmodifiableList(isbnPrestados); }
    public BigDecimal getMultas() { return multas; }

    @Override
    public String toString() {
        return String.format("Usuario %d: %s <%s> — Prestados: %d — Multas: $%s",
                id, nombre, email, isbnPrestados.size(), multas.toPlainString());
    }
}