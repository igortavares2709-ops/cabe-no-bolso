package bolso.visao;
import bolso.modelo.*;
import bolso.controle.Controle;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.*;
import java.nio.file.*;

public final class Painel extends JPanel {
    private final Controle controle;
    private final JTable pessoas = tabela("Código","Participante","Orçamento","Custo","Situação");
    private final JTable gastos = tabela("Descrição","Tipo","Valor (R$)","Códigos","Prioridade");
    private final JLabel total = new JLabel();
    private final JTabbedPane abas = new JTabbedPane();
    private static JTable tabela(String... colunas) {
        JTable t = new JTable(new DefaultTableModel(colunas, 0) {
            public boolean isCellEditable(int linha, int coluna) { return false; }
        });
        t.setRowHeight(30);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }
    private interface Acao { void executar() throws Exception; }
    private JButton botao(String nome, Acao acao) {
        var b = new JButton(nome);
        b.addActionListener(e -> {
            try { acao.executar(); atualizar(); }
            catch (Exception erro) { JOptionPane.showMessageDialog(this, erro.getMessage(),"Verifique os dados",JOptionPane.WARNING_MESSAGE); }
        });
        return b;
    }
    public Painel(Controle controle) {
        super(new BorderLayout(8,12));
        this.controle = controle;
        setBorder(BorderFactory.createEmptyBorder(16,18,16,18));
        var topo = new JPanel(new BorderLayout());
        var titulo = new JLabel("Cabe no bolso");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,24f));
        topo.add(titulo);
        var arquivos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        arquivos.add(botao("Novo", () -> { if (confirmar("Iniciar um planejamento vazio? Alterações não salvas serão perdidas.")) controle.novo(); }));
        arquivos.add(botao("Abrir…", () -> arquivo(false)));
        arquivos.add(botao("Salvar…", () -> arquivo(true)));
        topo.add(arquivos, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);
        abas.addTab("Participantes", secao(pessoas, botao("Cadastrar", () -> pessoa(false)), botao("Editar", () -> pessoa(true))));
        abas.addTab("Despesas", secao(gastos, botao("Incluir", () -> despesa(false)), botao("Editar", () -> despesa(true)),
            botao("Excluir", () -> { int i = linha(gastos); if (confirmar("Excluir esta despesa?")) controle.excluir(i); })));
        add(abas);
        var rodape = new JPanel(new BorderLayout());
        total.setFont(total.getFont().deriveFont(Font.BOLD,16f));
        rodape.add(total);
        rodape.add(botao("Simular alternativas", this::simular), BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
        atualizar();
    }
    private JPanel secao(JTable tabela, JButton... botoes) {
        var painel = new JPanel(new BorderLayout());
        var barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (var b : botoes) barra.add(b);
        painel.add(barra, BorderLayout.NORTH);
        var scroll = new JScrollPane(tabela);
        scroll.setColumnHeaderView(tabela.getTableHeader());
        painel.add(scroll);
        return painel;
    }
    private boolean confirmar(String pergunta) {
        return JOptionPane.showConfirmDialog(this,pergunta,"Confirmar",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    private int linha(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) throw new IllegalArgumentException("Selecione uma linha da tabela.");
        return linha;
    }
    private static String moeda(BigInteger valor) { return new BigDecimal(valor,2).toPlainString().replace('.',','); }
    private void atualizar() {
        var plano = controle.plano();
        var despesas = plano.despesas();
        var pm = (DefaultTableModel)pessoas.getModel();
        var dm = (DefaultTableModel)gastos.getModel();
        pm.setRowCount(0);
        dm.setRowCount(0);
        for (var p : plano.participantes()) {
            var excesso = Planejamento.excesso(p, despesas);
            pm.addRow(new Object[]{p.codigo(),p.nome(),moeda(p.orcamento()),moeda(Planejamento.custo(p.codigo(),despesas)),
                excesso.signum() == 0 ? "Dentro do limite" : "Excede R$ " + moeda(excesso)});
        }
        for (var d : despesas) dm.addRow(new Object[]{d.descricao(),d.tipo(),moeda(d.valor()),d.codigos(),
            d.prioridade() == 0 ? "Essencial" : d.prioridade()});
        total.setText("Custo do grupo     R$ " + moeda(plano.total(despesas)));
    }
    private void formulario(String titulo, Object[] campos, Acao gravar) throws Exception {
        while (JOptionPane.showConfirmDialog(this,campos,titulo,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try { gravar.executar(); return; }
            catch (IllegalArgumentException erro) { JOptionPane.showMessageDialog(this,erro.getMessage()); }
        }
    }
    private void pessoa(boolean editar) throws Exception {
        var codigo = new JTextField();
        var nome = new JTextField();
        var limite = new JTextField();
        if (editar) {
            var p = controle.plano().participantes().get(linha(pessoas));
            codigo.setText(String.valueOf(p.codigo())); codigo.setEditable(false);
            nome.setText(p.nome()); limite.setText(moeda(p.orcamento()));
        }
        formulario("Participante",new Object[]{"Código",codigo,"Nome",nome,"Orçamento (R$)",limite},
            () -> controle.pessoa(editar,codigo.getText(),nome.getText(),limite.getText()));
    }
    private void despesa(boolean editar) throws Exception {
        var cadastrados = controle.plano().participantes();
        if (cadastrados.isEmpty()) throw new IllegalArgumentException("Cadastre um participante primeiro.");
        int indice = editar ? linha(gastos) : -1;
        var descricao = new JTextField();
        var valor = new JTextField();
        var tipo = new JComboBox<>(new String[]{"Fixa","Por pessoa"});
        var prioridade = new JTextField("0");
        var envolvidos = new JList<>(cadastrados.toArray(Participante[]::new));
        envolvidos.setVisibleRowCount(Math.min(6,cadastrados.size()));
        if (editar) {
            var d = controle.plano().despesas().get(indice);
            descricao.setText(d.descricao()); valor.setText(moeda(d.valor()));
            tipo.setSelectedItem(d.tipo()); prioridade.setText(String.valueOf(d.prioridade()));
            for (int i=0;i<cadastrados.size();i++) if (d.codigos().contains(cadastrados.get(i).codigo())) envolvidos.addSelectionInterval(i,i);
        }
        formulario("Despesa",new Object[]{"Descrição",descricao,"Tipo de cobrança",tipo,"Valor fixo ou unitário (R$)",valor,
            "Participantes (Ctrl ou ⌘ para selecionar vários)",new JScrollPane(envolvidos),"Prioridade (0 = essencial, 1 ou mais = opcional)",prioridade},
            () -> controle.despesa(indice,descricao.getText(),valor.getText(),tipo.getSelectedIndex()==0,
                envolvidos.getSelectedValuesList().stream().map(Participante::codigo).toList(),prioridade.getText()));
    }
    private void arquivo(boolean salvar) throws Exception {
        var seletor = new JFileChooser();
        int resposta = salvar ? seletor.showSaveDialog(this) : seletor.showOpenDialog(this);
        if (resposta != JFileChooser.APPROVE_OPTION) return;
        var caminho = seletor.getSelectedFile().toPath();
        if (salvar) {
            if (!Files.exists(caminho) || confirmar("Substituir o arquivo selecionado?")) controle.salvar(caminho);
        } else if (confirmar("Carregar o arquivo no lugar do planejamento atual?")) controle.abrir(caminho);
    }
    public String alternativa() {
        var plano = controle.plano();
        var r = controle.simular();
        var linhas = new StringBuilder(r.viavel() ? "Solução • prioridade total " + r.pontos() : "Sem solução • despesas essenciais excedem o orçamento");
        for (var d : plano.despesas()) linhas.append("\n").append(r.mantidas().contains(d) ? "Mantida" : "Retirada").append("   ").append(d.descricao());
        linhas.append("\n\nPessoa    |    Original → simulação");
        for (var p : plano.participantes()) {
            var excesso = Planejamento.excesso(p,r.mantidas());
            linhas.append("\n").append(p.nome()).append("    R$ ").append(moeda(Planejamento.custo(p.codigo(),plano.despesas())))
                .append(" → R$ ").append(moeda(Planejamento.custo(p.codigo(),r.mantidas())));
            if (excesso.signum()>0) linhas.append("    Excesso R$ ").append(moeda(excesso));
        }
        return linhas + "\nGrupo    R$ " + moeda(plano.total(plano.despesas())) + " → R$ " + moeda(plano.total(r.mantidas()));
    }
    private void simular() {
        var texto = new JTextArea(alternativa(),14,44);
        texto.setEditable(false);
        JOptionPane.showMessageDialog(this,new JScrollPane(texto),"Alternativas",JOptionPane.PLAIN_MESSAGE);
    }
}
