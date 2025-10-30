import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CuentaBancaria {
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    public enum TipoCuenta { CORRIENTE, AHORROS }
    public enum TipoTransaccion { DEPOSITO, RETIRO, TRANSFERENCIA_ENTRANTE, TRANSFERENCIA_SALIENTE, INTERES, CARGO }

    private final int id;
    private final String cliente;
    private final TipoCuenta tipo;
    private double saldo;
    private final List<Transaccion> historial = new ArrayList<>();

    public CuentaBancaria(String cliente, TipoCuenta tipo, double saldoInicial) {
        this.id = SEQ.getAndIncrement();
        this.cliente = Objects.requireNonNull(cliente, "Cliente no puede ser null");
        this.tipo = Objects.requireNonNull(tipo, "Tipo de cuenta no puede ser null");
        this.saldo = Math.max(0.0, saldoInicial);
        registrarTransaccion(TipoTransaccion.DEPOSITO, saldoInicial, "Apertura de cuenta");
    }

    public int getId() { return id; }
    public String getCliente() { return cliente; }
    public TipoCuenta getTipo() { return tipo; }

    public synchronized double getSaldo() { return saldo; }

    public synchronized void depositar(double cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a depositar debe ser mayor que 0");
        saldo += cantidad;
        registrarTransaccion(TipoTransaccion.DEPOSITO, cantidad, "Depósito realizado");
    }

    public synchronized void retirar(double cantidad) throws InsufficientFundsException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a retirar debe ser mayor que 0");
        if (cantidad > saldo) throw new InsufficientFundsException("Saldo insuficiente");
        saldo -= cantidad;
        registrarTransaccion(TipoTransaccion.RETIRO, cantidad, "Retiro realizado");
    }

    public synchronized void aplicarInteres(double tasa) {
        if (tipo != TipoCuenta.AHORROS) return;
        if (tasa <= 0) throw new IllegalArgumentException("Tasa de interés inválida");
        double interes = saldo * tasa;
        saldo += interes;
        registrarTransaccion(TipoTransaccion.INTERES, interes, "Interés aplicado");
    }

    public synchronized void aplicarCargoMensual(double monto) throws InsufficientFundsException {
        if (tipo != TipoCuenta.CORRIENTE) return;
        if (monto <= 0) throw new IllegalArgumentException("Cargo mensual inválido");
        if (saldo < monto) throw new InsufficientFundsException("Saldo insuficiente para aplicar cargo");
        saldo -= monto;
        registrarTransaccion(TipoTransaccion.CARGO, monto, "Cargo mensual aplicado");
    }

    private void registrarTransaccion(TipoTransaccion tipo, double monto, String descripcion) {
        historial.add(new Transaccion(tipo, monto, saldo, descripcion));
    }

    public List<Transaccion> getHistorial() {
        return Collections.unmodifiableList(new ArrayList<>(historial));
    }

    @Override
    public String toString() {
        return String.format("ID:%d - %s (%s) - Saldo: %.2f", id, cliente, tipo, saldo);
    }

    // Excepciones y registros

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String msg) { super(msg); }
    }

    public static class Transaccion {
        private final TipoTransaccion tipo;
        private final double monto;
        private final double saldoPosterior;
        private final String descripcion;
        private final Date fecha;

        public Transaccion(TipoTransaccion tipo, double monto, double saldoPosterior, String descripcion) {
            this.tipo = tipo;
            this.monto = monto;
            this.saldoPosterior = saldoPosterior;
            this.descripcion = descripcion;
            this.fecha = new Date();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %.2f | Saldo: %.2f | %s",
                    fecha, tipo, monto, saldoPosterior, descripcion);
        }
    }

    // Clase Banco

    public static class Banco {
        private final Map<Integer, CuentaBancaria> cuentas = new LinkedHashMap<>();

        public CuentaBancaria crearCuenta(String cliente, TipoCuenta tipo, double saldoInicial) {
            CuentaBancaria c = new CuentaBancaria(cliente, tipo, saldoInicial);
            cuentas.put(c.getId(), c);
            return c;
        }

        public Optional<CuentaBancaria> obtenerCuenta(int id) {
            return Optional.ofNullable(cuentas.get(id));
        }

        public Collection<CuentaBancaria> listar() {
            return Collections.unmodifiableCollection(cuentas.values());
        }

        public void transferir(int fromId, int toId, double monto) throws Exception {
            if (monto <= 0) throw new IllegalArgumentException("Monto inválido para transferencia");
            CuentaBancaria origen = cuentas.get(fromId);
            CuentaBancaria destino = cuentas.get(toId);
            if (origen == null || destino == null)
                throw new NoSuchElementException("Una o ambas cuentas no existen");

            synchronized (this) {
                if (origen.getSaldo() < monto)
                    throw new InsufficientFundsException("Fondos insuficientes en cuenta origen");

                try {
                    origen.retirar(monto);
                    destino.depositar(monto);
                    origen.registrarTransaccion(TipoTransaccion.TRANSFERENCIA_SALIENTE, monto, "Transferencia a cuenta " + destino.getId());
                    destino.registrarTransaccion(TipoTransaccion.TRANSFERENCIA_ENTRANTE, monto, "Transferencia desde cuenta " + origen.getId());
                } catch (Exception e) {
                    throw new RuntimeException("Error en transferencia: " + e.getMessage());
                }
            }
        }

        public List<Transaccion> obtenerHistorial(int id) {
            CuentaBancaria cuenta = cuentas.get(id);
            if (cuenta == null) throw new NoSuchElementException("Cuenta no encontrada");
            return cuenta.getHistorial();
        }

        public void aplicarInteresesYCargos(double tasaAhorro, double cargoCorriente) {
            for (CuentaBancaria c : cuentas.values()) {
                try {
                    if (c.getTipo() == TipoCuenta.AHORROS)
                        c.aplicarInteres(tasaAhorro);
                    else if (c.getTipo() == TipoCuenta.CORRIENTE)
                        c.aplicarCargoMensual(cargoCorriente);
                } catch (Exception e) {
                    System.err.println("Error al aplicar a cuenta " + c.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}