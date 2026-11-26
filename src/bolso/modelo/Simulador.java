package bolso.modelo;
import java.util.*;

public final class Simulador {
    public record Resultado(boolean viavel, long pontos, List<Despesa> mantidas) {}
    public static Resultado buscar(Planejamento plano) {
        var todas = plano.despesas();
        var opcionais = todas.stream().filter(d -> d.prioridade() > 0).toList();
        var essenciais = todas.stream().filter(d -> d.prioridade() == 0).toList();
        if (opcionais.size() > 10) throw new IllegalArgumentException("O simulador aceita até dez opcionais.");
        var pessoas = plano.participantes();
        if (!cabe(pessoas, essenciais)) return new Resultado(false, 0, essenciais);
        var melhor = new Resultado(true, 0, essenciais);
        for (int mascara = 1; mascara < (1 << opcionais.size()); mascara++) {
            var selecao = new ArrayList<>(essenciais);
            long pontos = 0;
            for (int i = 0; i < opcionais.size(); i++) {
                if ((mascara & (1 << i)) != 0) {
                    Despesa d = opcionais.get(i);
                    selecao.add(d);
                    pontos += d.prioridade();
                }
            }
            if (pontos > melhor.pontos() && cabe(pessoas, selecao))
                melhor = new Resultado(true, pontos, List.copyOf(selecao));
        }
        return melhor;
    }
    private static boolean cabe(List<Participante> pessoas, List<Despesa> despesas) {
        return pessoas.stream().allMatch(p -> Planejamento.excesso(p, despesas).signum() == 0);
    }
}
