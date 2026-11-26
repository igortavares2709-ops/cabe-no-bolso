package bolso.modelo;
import java.math.BigInteger;
import java.util.Collection;

public final class PorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;
    public PorPessoa(String descricao, BigInteger valor, Collection<Integer> codigos, int prioridade) {
        super(descricao, valor, codigos, prioridade);
    }
    protected BigInteger calcular(int indice, int quantidade) { return valor(); }
    public String tipo() { return "Por pessoa"; }
}
