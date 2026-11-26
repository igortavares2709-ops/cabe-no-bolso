package bolso.modelo;

import java.io.Serializable;
import java.math.BigInteger;

public record Participante(int codigo, String nome, BigInteger orcamento) implements Serializable {
    private static final long serialVersionUID = 1L;

    public Participante {
        if (codigo <= 0) throw new IllegalArgumentException("Use um código inteiro positivo.");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Informe o nome.");
        nome = nome.strip();
        if (orcamento == null || orcamento.signum() < 0)
            throw new IllegalArgumentException("O orçamento deve ser igual ou superior a zero.");
    }
    public String toString() { return codigo + " · " + nome; }
}
