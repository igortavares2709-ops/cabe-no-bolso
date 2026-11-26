package bolso;
import bolso.arquivo.ArquivoLocal;
import bolso.controle.Controle;
import bolso.visao.Painel;
import javax.swing.*;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Cabe no bolso | Planejamento compartilhado");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new Painel(new Controle(new ArquivoLocal())));
            frame.setSize(860,480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
