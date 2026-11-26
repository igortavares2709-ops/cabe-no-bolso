package bolso;
import bolso.arquivo.*;
import bolso.controle.*;
import bolso.modelo.*;
import bolso.visao.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class Validacao {
    private static final List<Integer> TODOS = List.of(3,1,2);
    private static Controle vazio() { return new Controle(new ArquivoLocal()); }
    private static Controle passeio() {
        var c = vazio();
        c.pessoa(false,"1","Ana","70");
        c.pessoa(false,"2","Bruno","60");
        c.pessoa(false,"3","Carla","70");
        c.despesa(-1,"Transporte","90",true,TODOS,"0");
        c.despesa(-1,"Almoço","20",false,TODOS,"0");
        c.despesa(-1,"Atividade recreativa","30",true,List.of(3,1),"3");
        c.despesa(-1,"Sobremesa","10",false,TODOS,"1");
        return c;
    }
    private static void igual(Object obtido, Object esperado) {
        if (!obtido.equals(esperado)) throw new AssertionError(obtido + " ≠ " + esperado);
    }
    private static String assinatura(Controle c) {
        var p = c.plano();
        var s = new StringBuilder();
        for (var pessoa : p.participantes()) s.append(List.of(pessoa.codigo(),pessoa.nome(),pessoa.orcamento()));
        for (var d : p.despesas()) s.append(List.of(d.tipo(),d.descricao(),d.valor(),d.codigos(),d.prioridade()));
        return s.append(p.total(p.despesas())).toString();
    }
    private static void custos(Controle c,List<Despesa> selecao,String... esperados) {
        int i=0;
        for (var p : c.plano().participantes()) igual(Planejamento.custo(p.codigo(),selecao).toString(),esperados[i++]);
        igual(c.plano().total(selecao).toString(),esperados[i]);
    }
    private static void rejeitar(Controle c, Runnable operacao) {
        String antes = assinatura(c);
        try { operacao.run(); throw new AssertionError("Entrada aceita"); }
        catch (IllegalArgumentException e) { igual(assinatura(c),antes); System.out.println("  Recusado: " + e.getMessage()); }
    }
    private static void principal(Controle c) {
        var p = c.plano();
        custos(c,p.despesas(),"7500","6000","7500","21000");
        String[][] esperado = {{"3000","2000","1500","1000"},{"3000","2000","0","1000"},{"3000","2000","1500","1000"}};
        for (var pessoa : p.participantes()) {
            int id=pessoa.codigo();
            for (int j=0;j<4;j++) igual(p.despesas().get(j).cota(id).toString(),esperado[id-1][j]);
            igual(Planejamento.excesso(pessoa,p.despesas()).intValueExact(),id==2 ? 0 : 500);
        }
    }
    private static void layout(Container c) {
        c.doLayout();
        for (Component filho : c.getComponents()) if (filho instanceof Container f) layout(f);
    }
    private static void captura(JComponent componente, String nome,int largura,int altura) throws IOException {
        componente.setSize(largura,altura);
        layout(componente);
        var img = new BufferedImage(largura,altura,BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        componente.printAll(g); g.dispose();
        ImageIO.write(img,"png",Path.of("docs",nome).toFile());
    }
    public static void main(String[] args) throws Exception {
        var c = passeio();
        principal(c);
        System.out.println("01 OK | Rateios e excessos por pessoa. Ana 75,00; Bruno 60,00; Carla 75,00; grupo 210,00.");
        c.despesa(0,"Van","100",true,TODOS,"0");
        var van = c.plano().despesas().get(0);
        igual(List.of(van.cota(1),van.cota(2),van.cota(3)).toString(),"[3334, 3333, 3333]");
        igual(c.plano().total(List.of(van)).intValueExact(),10000);
        System.out.println("02 OK | Fixa 100,00; entrada 3,1,2; cotas em ordem 1,2,3: 33,34 / 33,33 / 33,33.");
        rejeitar(c,() -> c.despesa(-1,"Van","1",true,List.of(),"0"));
        rejeitar(c,() -> c.despesa(0,"Van","-1",true,TODOS,"0"));
        rejeitar(c,() -> c.despesa(0," ","10",true,TODOS,"0"));
        rejeitar(c,() -> c.despesa(0,"Van","0",true,TODOS,"0"));
        rejeitar(c,() -> c.despesa(0,"Van","1",true,List.of(1,1),"0"));
        rejeitar(c,() -> c.despesa(0,"Van","1",true,List.of(99),"0"));
        rejeitar(c,() -> c.pessoa(false,"1","Outra","10"));
        rejeitar(c,() -> c.pessoa(true,"1","Ana","-1"));
        rejeitar(c,() -> c.pessoa(true,"1","Ana","1,234"));
        System.out.println("03 OK | Nove tentativas inválidas preservaram o estado anterior.");
        var edicao = passeio();
        edicao.pessoa(true,"1","Ana","80");
        igual(Planejamento.excesso(edicao.plano().participantes().get(0),edicao.plano().despesas()).intValueExact(),0);
        custos(edicao,edicao.plano().despesas(),"7500","6000","7500","21000");
        System.out.println("04 OK | Ana com limite 80,00: custo 75,00 e excesso zero.");
        var exclusao = passeio();
        exclusao.excluir(2); custos(exclusao,exclusao.plano().despesas(),"6000","6000","6000","18000");
        System.out.println("05 OK | Atividade excluída: 60,00 para cada pessoa e 180,00 no grupo.");
        var original = passeio();
        var caminho = Path.of("exemplos","passeio.bolso");
        original.salvar(caminho);
        var reaberto = vazio();
        reaberto.abrir(caminho); reaberto.abrir(caminho);
        igual(assinatura(reaberto),assinatura(original)); principal(reaberto);
        System.out.println("06 OK | Nova instância: duas aberturas recuperaram três pessoas e quatro despesas, sem duplicar.");
        Path pasta = Files.createTempDirectory("bolso-teste");
        Path invalido = pasta.resolve("danificado");
        try {
            Files.writeString(invalido,"arquivo inválido");
            for (var arq : List.of(invalido,pasta.resolve("ausente"))) {
                try { reaberto.abrir(arq); throw new AssertionError("Carga indevida"); }
                catch (IOException erro) { igual(assinatura(reaberto),assinatura(original)); System.out.println("  " + erro.getMessage()); }
            }
        } finally { Files.deleteIfExists(invalido); Files.deleteIfExists(pasta); }
        reaberto.novo(); igual(reaberto.plano().participantes().size(),0); igual(reaberto.plano().despesas().size(),0);
        System.out.println("07 OK | Carga inválida/ausente preservou o passeio. Novo abriu vazio.");
        String antes = assinatura(original);
        var r = original.simular();
        igual(r.viavel(),true); igual(r.pontos(),3L); igual(r.mantidas().size(),3);
        custos(original,r.mantidas(),"6500","5000","6500","18000"); igual(assinatura(original),antes);
        System.out.println("08 OK | Ótimo 3: mantida atividade, retirada sobremesa. 65,00 / 50,00 / 65,00. Total 180,00.");
        var inviavel = passeio();
        inviavel.pessoa(true,"1","Ana","40");
        var impossivel = inviavel.simular(); igual(impossivel.viavel(),false);
        igual(Planejamento.excesso(inviavel.plano().participantes().get(0),impossivel.mantidas()).intValueExact(),1000);
        System.out.println("09 OK | Essenciais de Ana 50,00 para limite 40,00; excesso 10,00. Sem solução.");
        var encontro = vazio();
        encontro.pessoa(false,"8","Lia","20"); encontro.pessoa(false,"2","Ivo","20");
        encontro.despesa(-1,"Mesa de café","35,01",true,List.of(8,2),"0");
        encontro.despesa(-1,"Suco","2,50",false,List.of(8),"2");
        custos(encontro,encontro.plano().despesas(),"1751","2000","3751");
        System.out.println("10 OK | Café da equipe: Ivo 17,51; Lia 20,00; total 37,51.");
        var limite = passeio();
        for (int i=0;i<8;i++) limite.despesa(-1,"Opção "+i,"0,01",true,TODOS,"1");
        igual(limite.simular().viavel(),true);
        limite.despesa(-1,"Décima primeira","0,01",true,TODOS,"1");
        rejeitar(limite,limite::simular);
        System.out.println("11 OK | Dez opcionais aceitas; onze sinalizadas.");
        SwingUtilities.invokeAndWait(() -> {
            try {
                var painel = new Painel(original);
                captura(painel,"participantes.png",820,340);
                for (Component filho : painel.getComponents()) if (filho instanceof JTabbedPane abas) abas.setSelectedIndex(1);
                captura(painel,"despesas.png",820,340);
            } catch (IOException erro) { throw new RuntimeException(erro); }
        });
        System.out.println("12 OK | Duas telas Swing renderizadas com o caso de referência.");
    }
}
