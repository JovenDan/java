import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

// Clase CuentaBancaria
class CuentaBancaria {
    private String titular;
    private double saldo;

    public CuentaBancaria(String titular, double saldoInicial) {
        if (titular == null || titular.trim().isEmpty()) {
            throw new IllegalArgumentException("El titular no puede estar vacío.");
        }
        if (saldoInicial < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }
        this.titular = titular;
        this.saldo = saldoInicial;
    }

    public String getTitular() {
        return titular;
    }

    public double getSaldo() {
        return saldo;
    }

    public void depositar(double cantidad) {
        if (cantidad <= 0) {
            System.out.println("⚠️ Error: La cantidad a depositar debe ser mayor que cero.");
            return;
        }
        saldo += cantidad;
        System.out.println("✅ Depósito exitoso: $" + cantidad);
    }

    public void retirar(double cantidad) {
        if (cantidad <= 0) {
            System.out.println("⚠️ Error: La cantidad a retirar debe ser mayor que cero.");
            return;
        }
        if (cantidad > saldo) {
            System.out.println("❌ Fondos insuficientes. Saldo actual: $" + saldo);
            return;
        }
        saldo -= cantidad;
        System.out.println("✅ Retiro exitoso: $" + cantidad);
    }

    public void mostrarInfo() {
        System.out.printf("👤 Titular: %s | 💰 Saldo: $%.2f%n", titular, saldo);
    }
}

// Clase Banco con manejo de excepciones y menú
public class BancoApp {
    private List<CuentaBancaria> cuentas;

    public BancoApp() {
        cuentas = new ArrayList<>();
    }

    public void agregarCuenta(CuentaBancaria cuenta) {
        cuentas.add(cuenta);
        System.out.println("✅ Cuenta creada para: " + cuenta.getTitular());
    }

    public CuentaBancaria buscarCuenta(String titular) {
        for (CuentaBancaria c : cuentas) {
            if (c.getTitular().equalsIgnoreCase(titular)) {
                return c;
            }
        }
        System.out.println("⚠️ No se encontró ninguna cuenta con ese titular.");
        return null;
    }

    public void mostrarTodasLasCuentas() {
        if (cuentas.isEmpty()) {
            System.out.println("📭 No hay cuentas registradas aún.");
            return;
        }
        System.out.println("\n--- 🏦 Listado de Cuentas ---");
        for (CuentaBancaria c : cuentas) {
            c.mostrarInfo();
        }
    }

    // Menú principal
    public void iniciar() {
        Scanner sc = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            try {
                System.out.println("\n=== MENÚ DEL BANCO ===");
                System.out.println("1. Crear cuenta");
                System.out.println("2. Depositar");
                System.out.println("3. Retirar");
                System.out.println("4. Mostrar cuentas");
                System.out.println("5. Buscar cuenta");
                System.out.println("6. Salir");
                System.out.print("Elige una opción: ");
                int opcion = sc.nextInt();
                sc.nextLine(); // limpiar buffer

                switch (opcion) {
                    case 1 -> {
                        System.out.print("Nombre del titular: ");
                        String titular = sc.nextLine();
                        System.out.print("Saldo inicial: ");
                        double saldo = sc.nextDouble();
                        sc.nextLine();
                        try {
                            agregarCuenta(new CuentaBancaria(titular, saldo));
                        } catch (IllegalArgumentException e) {
                            System.out.println("⚠️ " + e.getMessage());
                        }
                    }
                    case 2 -> {
                        System.out.print("Titular: ");
                        String titular = sc.nextLine();
                        CuentaBancaria cuenta = buscarCuenta(titular);
                        if (cuenta != null) {
                            System.out.print("Cantidad a depositar: ");
                            double monto = sc.nextDouble();
                            sc.nextLine();
                            cuenta.depositar(monto);
                        }
                    }
                    case 3 -> {
                        System.out.print("Titular: ");
                        String titular = sc.nextLine();
                        CuentaBancaria cuenta = buscarCuenta(titular);
                        if (cuenta != null) {
                            System.out.print("Cantidad a retirar: ");
                            double monto = sc.nextDouble();
                            sc.nextLine();
                            cuenta.retirar(monto);
                        }
                    }
                    case 4 -> mostrarTodasLasCuentas();
                    case 5 -> {
                        System.out.print("Titular a buscar: ");
                        String titular = sc.nextLine();
                        CuentaBancaria encontrada = buscarCuenta(titular);
                        if (encontrada != null) encontrada.mostrarInfo();
                    }
                    case 6 -> {
                        continuar = false;
                        System.out.println("👋 Gracias por usar el sistema bancario. ¡Hasta pronto!");
                    }
                    default -> System.out.println("⚠️ Opción inválida. Intenta nuevamente.");
                }

            } catch (InputMismatchException e) {
                System.out.println("⚠️ Error: Debes ingresar un número válido.");
                sc.nextLine(); // limpiar entrada incorrecta
            } catch (Exception e) {
                System.out.println("❌ Error inesperado: " + e.getMessage());
            }
        }

        sc.close();
    }

    // Método main
    public static void main(String[] args) {
        BancoApp banco = new BancoApp();
        banco.iniciar();
    }
}