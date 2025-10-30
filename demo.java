public class Demo {
    public static void main(String[] args) {
        CuentaBancaria.Banco banco = new CuentaBancaria.Banco();

        // Crear cuentas
        
        var cuentaA = banco.crearCuenta("Cuenta A (Corriente)", CuentaBancaria.TipoCuenta.CORRIENTE, 1000);
        var cuentaB = banco.crearCuenta("Cuenta B (Ahorros)", CuentaBancaria.TipoCuenta.AHORROS, 500);

        System.out.println("Cuentas creadas:");
        banco.listar().forEach(System.out::println);

        // Depositar en A

        cuentaA.depositar(500);
        System.out.println("\nDepósito en cuenta A: " + cuentaA.getSaldo());

        // Transferencia válida

        try {
            banco.transferir(cuentaA.getId(), cuentaB.getId(), 300);
            System.out.println("\nTransferencia realizada con éxito.");
        } catch (Exception e) {
            System.out.println("Error en transferencia: " + e.getMessage());
        }

        // Transferencia inválida (fondos insuficientes)

        try {
            banco.transferir(cuentaA.getId(), cuentaB.getId(), 5000);
        } catch (Exception e) {
            System.out.println("Error esperado: " + e.getMessage());
        }

        // Aplicar intereses/cargos globales

        banco.aplicarInteresesYCargos(0.05, 50);

        System.out.println("\nSaldos finales:");
        banco.listar().forEach(System.out::println);

        // Mostrar historiales

        System.out.println("\nHistorial Cuenta A:");
        banco.obtenerHistorial(cuentaA.getId()).forEach(System.out::println);

        System.out.println("\nHistorial Cuenta B:");
        banco.obtenerHistorial(cuentaB.getId()).forEach(System.out::println);
    }
}