package bolso.modelo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

public final class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;
    private final TreeMap<Integer,Participante> pessoas = new TreeMap<>();
    private final ArrayList<Despesa> despesas = new ArrayList<>();

    public List<Participante> participantes() { return List.copyOf(pessoas.values()); }
    public List<Despesa> despesas() { return List.copyOf(despesas); }
    public void cadastrar(Participante p) {
        if (pessoas.putIfAbsent(p.codigo(), p) != null) throw new IllegalArgumentException("Código já cadastrado.");
    }
    public void atualizar(Participante p) {
        if (pessoas.replace(p.codigo(), p) == null) throw new IllegalArgumentException("Pessoa não cadastrada.");
    }
    private void validar(Despesa d) {
        if (!pessoas.keySet().containsAll(d.codigos())) throw new IllegalArgumentException("Participante não cadastrado.");
    }
    public void incluir(Despesa d) { validar(d); despesas.add(d); }
    public void editar(int indice, Despesa d) { validar(d); despesas.set(indice, d); }
    public void excluir(int indice) { despesas.remove(indice); }
    public static BigInteger custo(int codigo, List<Despesa> selecao) {
        return selecao.stream().map(d -> d.cota(codigo)).reduce(BigInteger.ZERO, BigInteger::add);
    }
    public BigInteger total(List<Despesa> selecao) {
        return pessoas.keySet().stream().map(c -> custo(c, selecao)).reduce(BigInteger.ZERO, BigInteger::add);
    }
    public static BigInteger excesso(Participante p, List<Despesa> selecao) {
        return custo(p.codigo(), selecao).subtract(p.orcamento()).max(BigInteger.ZERO);
    }
}
