package bolso.arquivo;
import bolso.modelo.*;
import java.io.*;
import java.nio.file.*;

public final class ArquivoLocal implements Persistencia {
    public void salvar(Planejamento plano, Path caminho) throws IOException {
        Path temp = Files.createTempFile(caminho.toAbsolutePath().getParent(), ".bolso", ".tmp");
        try {
            try (var out = new ObjectOutputStream(Files.newOutputStream(temp))) { out.writeObject(plano); }
            try {
                Files.move(temp, caminho, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, caminho, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temp); }
    }
    public Planejamento carregar(Path caminho) throws IOException {
        try (var in = new ObjectInputStream(Files.newInputStream(caminho))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                "maxdepth=32;maxrefs=1000000;maxarray=1000000;bolso.modelo.*;java.util.TreeMap;java.util.ArrayList;java.math.BigInteger;java.lang.Number;java.lang.Integer;java.lang.Object;java.lang.String;!*"));
            if (!(in.readObject() instanceof Planejamento salvo)) throw new IOException("Tipo de arquivo incompatível.");
            var validado = new Planejamento();
            for (var p : salvo.participantes()) validado.cadastrar(new Participante(p.codigo(), p.nome(), p.orcamento()));
            for (var d : salvo.despesas()) validado.incluir(d instanceof Fixa
                ? new Fixa(d.descricao(), d.valor(), d.codigos(), d.prioridade())
                : new PorPessoa(d.descricao(), d.valor(), d.codigos(), d.prioridade()));
            return validado;
        } catch (NoSuchFileException e) {
            throw new IOException("Arquivo não encontrado.", e);
        } catch (ClassNotFoundException | RuntimeException | ObjectStreamException | EOFException e) {
            throw new IOException("Arquivo inválido ou incompatível.", e);
        }
    }
}
