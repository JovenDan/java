package service;

import exceptions.LibroNoDisponibleException;
import exceptions.UsuarioSinCupoException;
import model.Libro;
import model.Prestamo;
import model.Usuario;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Biblioteca {
    // repositorios
    private final Map<String, Libro> libros = new ConcurrentHashMap<>(); // clave: ISBN
    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final List<Prestamo> prestamos = Collections.synchronizedList(new ArrayList<>());

    // sincronizar operaciones que cambian estado
    public synchronized void agregarLibro(Libro libro) {
        libros.put(libro.getIsbn(), libro);
    }

    public Optional<Libro> buscarPorIsbn(String isbn) {
        return Optional.ofNullable(libros.get(isbn));
    }

    public List<Libro> buscarPorTitulo(String fragmento) {
        String q = fragmento == null ? "" : fragmento.toLowerCase();
        return libros.values().stream()
                .filter(l -> l.getTitulo().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public synchronized Usuario registrarUsuario(Usuario u) {
        usuarios.put(u.getId(), u);
        return u;
    }

    public Optional<Usuario> obtenerUsuario(int id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public synchronized Prestamo realizarPrestamo(int usuarioId, String isbn) throws LibroNoDisponibleException, UsuarioSinCupoException {
        Usuario usuario = usuarios.get(usuarioId);
        if (usuario == null) throw new UsuarioSinCupoException("Usuario no registrado.");
        Libro libro = libros.get(isbn);
        if (libro == null) throw new LibroNoDisponibleException("Libro no encontrado.");

        // validar reglas de usuario
        synchronized (usuario) {
            if (!usuario.puedePedirPrestado()) throw new UsuarioSinCupoException("Usuario no puede pedir prestado (límite o multas).");
            // intentar reservar libro
            libro.prestar(); // puede lanzar LibroNoDisponibleException
            usuario.agregarPrestamo(isbn);
            Prestamo prestamo = new Prestamo(isbn, usuarioId);
            prestamos.add(prestamo);
            return prestamo;
        }
    }

    public synchronized void devolverLibro(int usuarioId, String isbn) {
        Usuario usuario = usuarios.get(usuarioId);
        Libro libro = libros.get(isbn);
        if (usuario == null || libro == null) return;

        // buscar préstamo activo
        Optional<Prestamo> opt = prestamos.stream()
                .filter(p -> p.getUsuarioId() == usuarioId && p.getIsbn().equals(isbn) && p.getEstado().name().equals("ACTIVO"))
                .findFirst();

        if (opt.isPresent()) {
            Prestamo p = opt.get();
            p.marcarDevuelto();
            BigDecimal multa = p.calcularMulta();
            if (multa.compareTo(BigDecimal.ZERO) > 0) {
                usuario.agregarMulta(multa);
            }
            usuario.quitarPrestamo(isbn);
            libro.devolver();
        }
    }

    public List<Usuario> obtenerUsuariosConMultas() {
        return usuarios.values().stream()
                .filter(u -> u.getMultas().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    public List<Libro> obtenerTopLibrosPrestados(int topN) {
        return libros.values().stream()
                .sorted(Comparator.comparingInt(Libro::getVecesPrestado).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    public List<Prestamo> obtenerPrestamosDeUsuario(int usuarioId) {
        return prestamos.stream()
                .filter(p -> p.getUsuarioId() == usuarioId)
                .collect(Collectors.toList());
    }

    public List<Libro> listarLibrosDisponibles() {
        return libros.values().stream()
                .filter(Libro::estaDisponible)
                .collect(Collectors.toList());
    }

    // rotina para evaluar estado de prestamos y actualizar multas pendientes en usuarios
    public synchronized void evaluarPrestamosYMultas() {
        for (Prestamo p : prestamos) {
            p.evaluarEstado();
            if (p.getEstado().name().equals("VENCIDO")) {
                BigDecimal multa = p.calcularMulta();
                Usuario u = usuarios.get(p.getUsuarioId());
                if (u != null) u.agregarMulta(multa);
            }
        }
    }
}