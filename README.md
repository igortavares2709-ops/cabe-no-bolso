# Cabe no bolso

Aplicação desktop para planejar um passeio, ratear despesas entre pessoas selecionadas e comparar os custos com seus orçamentos. Inclui o simulador da seção 7.

**Integrante identificado:** Igor Barroso Tavares. A matrícula consta no relatório técnico.

## Executar

Instale o JDK 21 (compilador e máquina virtual). O aplicativo usa somente a biblioteca padrão. Extraia o ZIP e abra o terminal **na pasta que contém fontes.txt**. No Windows, abra o PowerShell nessa pasta. Em Linux/macOS, use o terminal.

~~~sh
mkdir build
javac -encoding UTF-8 -d build "@fontes.txt"
java -cp build bolso.Main
~~~

Se a pasta build já existir, siga para o comando javac. A janela requer uma sessão gráfica. Outra opção é executar o JAR incluído com Java 21:

~~~sh
java -jar CabeNoBolso.jar
~~~

## Usar o passeio de referência

1. Clique em **Abrir…**, selecione **exemplos/passeio.bolso** e confirme. A aplicação também permite começar com um planejamento vazio.
2. Em **Participantes**, confira Ana (1, R$ 70), Bruno (2, R$ 60) e Carla (3, R$ 70). Selecione uma linha e use **Editar** para alterar nome ou orçamento.
3. Em **Despesas**, confira transporte fixo de R$ 90 para todos, almoço de R$ 20 por pessoa para todos, atividade fixa de R$ 30 para Ana e Carla e sobremesa de R$ 10 por pessoa para todos. Na inclusão/edição, use Ctrl (⌘ no macOS) para selecionar várias pessoas.
4. Use prioridade zero para essenciais, 3 para a atividade e 1 para a sobremesa. Clique em **Simular alternativas**. O total sugerido é R$ 180. O original permanece em R$ 210.
5. Em **Salvar…**, escolha um nome (por exemplo, meu-passeio.bolso). Feche a aplicação, inicie-a novamente e abra o arquivo. A abertura substitui os dados em tela. Salve suas alterações antes de sair.

## Reproduzir os testes

Depois da compilação, rode na mesma pasta:

~~~sh
java -Djava.awt.headless=true -cp build bolso.Validacao
~~~

No PowerShell, coloque **'-Djava.awt.headless=true'** entre aspas simples. O teste encerra com erro na primeira divergência. Ele regenera o passeio de exemplo e duas imagens Swing em docs. Use outro nome para guardar seu planejamento pessoal. A execução de referência está em docs/resultados.txt. O teste de recuperação usa uma nova instância de controle e duas leituras consecutivas. As capturas mostram componentes reais renderizados fora de uma janela nativa. Para validação manual, repita os cenários do relatório pela interface.

## Organização

- src/bolso/Main.java inicia o Swing na thread de eventos.
- modelo representa participantes e despesas, calcula o rateio e simula alternativas.
- controle converte entradas, coordena a edição e troca o plano após carga bem-sucedida.
- visao apresenta formulários, tabelas, mensagens e a simulação.
- arquivo contém o contrato de persistência e a implementação por serialização.
- test/bolso/Validacao.java contém os testes, as entradas e os valores esperados.
- docs contém as UML em DOT e PNG, as evidências e o relatório.
- exemplos/passeio.bolso contém os dados serializados do enunciado.

A relação entre despesa e participante é representada pelo código estável. Uma despesa só pode incluir pessoas do próprio planejamento. Os custos são calculados pelas subclasses e somados sob demanda. A justificativa das coleções, operações e relações está no relatório.

## Relatório técnico

[Baixar o relatório em PDF](docs/Relatorio_Cabe_no_Bolso.pdf). Para a entrega no AVA, a equipe deve conferir os dados dos integrantes e registrar as contribuições realizadas.
