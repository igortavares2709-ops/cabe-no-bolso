package bolso.modelo;
import java.math.BigInteger;
import java.util.Collection;

public final class Fixa extends Despesa {
    private static final long serialVersionUID = 1L;
    public Fixa(String descricao, BigInteger valor, Collection<Integer> codigos, int prioridade) {
        super(descricao, valor, codigos, prioridade);
    }
    protected BigInteger calcular(int indice, int quantidade) {
        var partes = valor().divideAndRemainder(BigInteger.valueOf(quantidade));
        return indice < partes[1].intValueExact() ? partes[0].add(BigInteger.ONE) : partes[0];
    }
    public String tipo() { return "Fixa"; }
}
