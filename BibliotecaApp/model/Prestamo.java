package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Prestamo {
    private final String isbn;
    private final int usuarioId;
    private final LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion; // null hasta que se devuelva
    private EstadoPrestamo estado;
    public static final int DIAS_PRESTAMO = 14;
    private static final BigDecimal MULTA_DIARIA = new BigDecimal("500"); // $500 por día

    public Prestamo(String isbn, int usuarioId) {
        this.isbn = Objects.requireNonNull(isbn);
        this.usuarioId = usuarioId;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucion = fechaPrestamo.plusDays(DIAS_PRESTAMO);
        this.estado = EstadoPrestamo.ACTIVO;
    }

    public BigDecimal calcularMulta() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = fechaPrestamo.plusDays(DIAS_PRESTAMO);
        if (hoy.isAfter(limite)) {
            long diasRetraso = ChronoUnit.DAYS.between(limite, hoy);
            BigDecimal multa = MULTA_DIARIA.multiply(BigDecimal.valueOf(diasRetraso));
            return multa;
        }
        return BigDecimal.ZERO;
    }

    public void marcarDevuelto() {
        this.estado = EstadoPrestamo.DEVUELTO;
        // fechaDevolucion se puede actualizar a fecha real si se desea:
        this.fechaDevolucion = LocalDate.now();
    }

    public void evaluarEstado() {
        if (estado == EstadoPrestamo.ACTIVO) {
            LocalDate limite = fechaPrestamo.plusDays(DIAS_PRESTAMO);
            if (LocalDate.now().isAfter(limite)) estado = EstadoPrestamo.VENCIDO;
        }
    }

    // getters
    public String getIsbn() { return isbn; }
    public int getUsuarioId() { return usuarioId; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucionPrevista() { return fechaPrestamo.plusDays(DIAS_PRESTAMO); }
    public EstadoPrestamo getEstado() { return estado; }

    @Override
    public String toString() {
        return String.format("Prestamo{usuario=%d,isbn=%s,prestamo=%s,estado=%s}",
                usuarioId, isbn, fechaPrestamo, estado);
    }
}