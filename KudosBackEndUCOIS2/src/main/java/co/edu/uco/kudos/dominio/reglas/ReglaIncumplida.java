package co.edu.uco.kudos.dominio.reglas;

/** Una regla del negocio no se cumple. Extiende {@link RuntimeException} para deshacer la transacción. */
public final class ReglaIncumplida extends RuntimeException {

    private final CodigoRegla codigo;

    public ReglaIncumplida(CodigoRegla codigo, String detalle) {
        super(detalle);
        this.codigo = codigo;
    }

    public CodigoRegla codigo() {
        return codigo;
    }

    /** Lanza la regla si la condición no se cumple. */
    public static void exigir(boolean condicion, CodigoRegla codigo, String detalle) {
        if (!condicion) {
            throw new ReglaIncumplida(codigo, detalle);
        }
    }
}
