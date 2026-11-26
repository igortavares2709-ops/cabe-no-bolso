package bolso.arquivo;
import bolso.modelo.Planejamento;
import java.io.IOException;
import java.nio.file.Path;

public interface Persistencia {
    void salvar(Planejamento plano, Path caminho) throws IOException;
    Planejamento carregar(Path caminho) throws IOException;
}
