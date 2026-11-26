package bolso.controle;
import bolso.modelo.*;
import bolso.arquivo.Persistencia;
import java.io.IOException;
import java.nio.file.Path;
import java.math.*;
import java.util.List;

public final class Controle {
    private Planejamento plano = new Planejamento();
    private final Persistencia arquivo;
    public Controle(Persistencia arquivo) { this.arquivo = arquivo; }
    public Planejamento plano() { return plano; }
    public void novo() { plano = new Planejamento(); }
    public void salvar(Path p) throws IOException { arquivo.salvar(plano, p); }
    public void abrir(Path p) throws IOException { plano = arquivo.carregar(p); }
    public static BigInteger centavos(String texto) {
        String valor = texto.strip().replace(',', '.');
        if (!valor.matches("-?[0-9]+([.][0-9]{1,2})?"))
            throw new IllegalArgumentException("Use um valor como 25,90, sem separador de milhar.");
        return new BigDecimal(valor).movePointRight(2).toBigIntegerExact();
    }
    public static int inteiro(String valor) {
        try { return Integer.parseInt(valor.strip()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Informe um número inteiro válido."); }
    }
    public void pessoa(boolean editar, String codigo, String nome, String limite) {
        var p = new Participante(inteiro(codigo), nome, centavos(limite));
        if (editar) plano.atualizar(p); else plano.cadastrar(p);
    }
    public void despesa(int indice, String descricao, String valor, boolean fixa, List<Integer> codigos, String prioridade) {
        int pontos = inteiro(prioridade);
        var d = fixa ? new Fixa(descricao, centavos(valor), codigos, pontos)
                     : new PorPessoa(descricao, centavos(valor), codigos, pontos);
        if (indice < 0) plano.incluir(d); else plano.editar(indice, d);
    }
    public void excluir(int indice) { plano.excluir(indice); }
    public Simulador.Resultado simular() { return Simulador.buscar(plano); }
}
