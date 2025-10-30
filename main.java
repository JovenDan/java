import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CuentaBancaria {
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    public enum TipoCuenta { CORRIENTE, AHORROS }

    private final int id;
    private final String cliente;
    private final TipoCuenta tipo;
    private double saldo;

    public CuentaBancaria(String cliente, TipoCuenta tipo, double saldoInicial) {
        this.id = SEQ.getAndIncrement();
        this.cliente = Objects.requireNonNull(cliente, "Cliente no puede ser null");
        this.tipo = Objects.requireNonNull(tipo, "Tipo de cuenta no puede ser null");
        this.saldo = Math.max(0.0, saldoInicial);
    }

    public int getId() { return id; }
    public String getCliente() { return cliente; }
    public TipoCuenta getTipo() { return tipo; }

    public synchronized double getSaldo() { return saldo; }

    public synchronized void depositar(double cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a depositar debe ser mayor que 0");
        saldo += cantidad;
    }

    public synchronized void retirar(double cantidad) throws InsufficientFundsException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a retirar debe ser mayor que 0");
        if (cantidad > saldo) throw new InsufficientFundsException("Saldo insuficiente");
        saldo -= cantidad;
    }

    @Override
    public String toString() {
        return String.format("ID:%d - %s (%s) - Saldo: %.2f", id, cliente, tipo, saldo);
    }

    // Excepción específica para manejo claro
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String msg) { super(msg); }
    }

    // Repositorio simple en memoria (separa persistencia del modelo)
    static class Banco {
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
    }

    // Interacción por consola (I/O separado de lógica)
    public static void main(String[] args) {
        Banco banco = new Banco();
        banco.crearCuenta("Tony Stark", TipoCuenta.CORRIENTE, 1599.99);

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("********************");
                System.out.println("1 - Crear cuenta");
                System.out.println("2 - Consultar saldo");
                System.out.println("3 - Retirar");
                System.out.println("4 - Depositar");
                System.out.println("5 - Listar cuentas");
                System.out.println("6 - Salir");
                System.out.print("Seleccione opción: ");

                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;

                int opcion;
                try { opcion = Integer.parseInt(linea); }
                catch (NumberFormatException e) { System.out.println("Opción inválida."); continue; }

                switch (opcion) {
                    case 1:
                        crearCuentaFlow(sc, banco);
                        break;
                    case 2:
                        consultarSaldoFlow(sc, banco);
                        break;
                    case 3:
                        retirarFlow(sc, banco);
                        break;
                    case 4:
                        depositarFlow(sc, banco);
                        break;
                    case 5:
                        listarFlow(banco);
                        break;
                    case 6:
                        System.out.println("Saliendo...");
                        return;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
        }
    }

    private static void crearCuentaFlow(Scanner sc, Banco banco) {
        System.out.print("Nombre del titular: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("Nombre no puede estar vacío."); return; }

        System.out.print("Tipo (1=Corriente, 2=Ahorros): ");
        String t = sc.nextLine().trim();
        TipoCuenta tipo;
        if ("2".equals(t)) tipo = TipoCuenta.AHORROS;
        else tipo = TipoCuenta.CORRIENTE;

        System.out.print("Saldo inicial: ");
        double saldoInicial;
        try { saldoInicial = Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Saldo inválido."); return; }

        CuentaBancaria c = banco.crearCuenta(nombre, tipo, saldoInicial);
        System.out.println("Cuenta creada: " + c);
    }

    private static void consultarSaldoFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isPresent()) {
            System.out.println("Saldo: " + o.get().getSaldo());
        } else {
            System.out.println("Cuenta no encontrada.");
        }
    }

    private static void retirarFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Cantidad a retirar: ");
        double monto;
        try { monto = Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Monto inválido."); return; }

        try {
            c.retirar(monto);
            System.out.println("Retiro exitoso. Nuevo saldo: " + c.getSaldo());
        } catch (InsufficientFundsException e) {
            System.out.println("Operación fallida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Entrada inválida: " + e.getMessage());
        }
    }

    private static void depositarFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Cantidad a depositar: ");
        double monto;
        try { monto = Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Monto inválido."); return; }

        try {
            c.depositar(monto);
            System.out.println("Depósito exitoso. Nuevo saldo: " + c.getSaldo());
        } catch (IllegalArgumentException e) {
            System.out.println("Entrada inválida: " + e.getMessage());
        }
    }

    private static void listarFlow(Banco banco) {
        Collection<CuentaBancaria> cuentas = banco.listar();
        if (cuentas.isEmpty()) { System.out.println("No hay cuentas."); return; }
        cuentas.forEach(System.out::println);
    }

    private static Optional<CuentaBancaria> obtenerCuentaPorId(Scanner sc, Banco banco) {
        System.out.print("Ingrese ID de cuenta: ");
        String line = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(line);
            return banco.obtenerCuenta(id);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return Optional.empty();
        }
    }
}
