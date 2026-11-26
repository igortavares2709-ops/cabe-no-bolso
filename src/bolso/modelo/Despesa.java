package bolso.modelo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

public abstract sealed class Despesa implements Serializable permits Fixa, PorPessoa {
    private static final long serialVersionUID = 1L;
    private final String descricao;
    private final BigInteger valor;
    private final ArrayList<Integer> codigos;
    private final int prioridade; // zero identifica uma despesa essencial

    protected Despesa(String descricao, BigInteger valor, Collection<Integer> codigos, int prioridade) {
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("Informe a descrição.");
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("A despesa deve ser positiva.");
        if (prioridade < 0) throw new IllegalArgumentException("A prioridade deve ser positiva para uma opcional.");
        if (codigos == null || codigos.isEmpty()) throw new IllegalArgumentException("Selecione ao menos uma pessoa.");
        var unicos = new TreeSet<Integer>();
        for (Integer codigo : codigos) {
            if (codigo == null || codigo <= 0 || !unicos.add(codigo))
                throw new IllegalArgumentException("Código inválido ou repetido.");
        }
        this.descricao = descricao.strip();
        this.valor = valor;
        this.codigos = new ArrayList<>(unicos);
        this.prioridade = prioridade;
    }

    public String descricao() { return descricao; }
    public BigInteger valor() { return valor; }
    public List<Integer> codigos() { return List.copyOf(codigos); }
    public int prioridade() { return prioridade; }
    public final BigInteger cota(int codigo) {
        int indice = Collections.binarySearch(codigos, codigo);
        return indice < 0 ? BigInteger.ZERO : calcular(indice, codigos.size());
    }
    protected abstract BigInteger calcular(int indice, int quantidade);
    public abstract String tipo();
}
