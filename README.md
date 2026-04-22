# Projeto de Inteligência Artificial - Mini Jogo de Damas (6x6)

## 📋 Visão Geral
Este projeto é uma implementação do clássico jogo de Damas em um tabuleiro reduzido (6x6), desenvolvido em Java com uma Interface Gráfica interativa usando Java Swing. O grande destaque do projeto é o motor de Inteligência Artificial (IA) implementado do zero, capaz de jogar contra o usuário humano utilizando o algoritmo **Minimax com Poda Alfa-Beta**.

O jogo conta com **9 níveis de dificuldade**, que vão desde jogadas puramente aleatórias (Nível 1) até cálculos profundos e extremamente otimizados (Nível 9).

## 🚀 Funcionalidades
- **Tabuleiro 6x6 Customizado:** Menor que o tradicional (8x8), resultando em partidas mais rápidas, apertadas e agressivas.
- **Dificuldade Escalonável (1 a 9):**
  - **Nível 1:** A IA joga de forma totalmente aleatória (sem pensar no futuro).
  - **Níveis 2 a 8:** A IA usa a construção formal de Árvore (com instâncias da classe `Node`) para avaliar o tabuleiro, ativando bônus de heurística de forma gradativa para se tornar mais inteligente aos poucos.
  - **Nível 9 (Modo Extremo):** A IA utiliza Minimax puro (o algoritmo original mais poderoso do projeto). Ela desliga a alocação de objetos em massa e foca 100% no cálculo bruto, descendo 12 andares de profundidade em poucos milissegundos.
- **Auxílio Visual (Ponto Cego):** Ao clicar em uma peça na sua vez de jogar, a interface pinta de **roxo** os destinos válidos onde aquela peça pode parar (incluindo pulos longos de damas e próximos saltos de combos).
- **Regras Oficiais:** Implementação fiel às regras de torneio, onde a captura é mandatória e a Dama pode comer à distância e para trás.
- **Regra de Empate (Inércia):** Se passarem 10 rodadas completas (20 lances) sem que nenhuma captura ocorra de nenhum lado, o sistema detecta jogo trancado e declara empate automaticamente.

## 🛠️ Tecnologias e Ferramentas
- **Linguagem:** Java (Orientação a Objetos pura).
- **Interface Gráfica (GUI):** Java Swing, AWT, e Events/Threads.
- **Algoritmo Base de IA:** Busca Heurística Minimax.
- **Otimização Algorítmica:** Poda Alfa-Beta (Alpha-Beta Pruning).

## 🧠 Como a IA Funciona? (A Arquitetura)
O motor matemático da IA está construído primariamente no arquivo `Arvore.java` e opera sob as seguintes etapas lógicas:

1. **Geração da Árvore de Estados:** A cada turno da IA, ela clona a matriz atual do tabuleiro (para não estragar o jogo visual) e começa a "imaginar" os movimentos futuros gerando todos os desdobramentos possíveis.
2. **Avaliação Minimax:** A IA simula os lances dela (tentando **Maximizar** a própria pontuação) e em seguida simula os lances que o Humano faria em resposta (assumindo que o humano faria a melhor jogada possível para **Minimizar** os pontos dela). 
3. **Poda Alfa-Beta:** Para evitar ter que processar bilhões de possibilidades no tabuleiro inteiro, o código usa a Poda Alfa-Beta (`if (beta <= alpha) break;`). Isso corta galhos inteiros de pensamento que a IA já sabe que são ruins, acelerando absurdamente a tomada de decisão.
4. **Heurística (Sistema de Visão do Tabuleiro):** Como a IA dá "nota" ao jogo? Através do método `aplicarHeuristicaVitoria`, que avalia a foto do tabuleiro baseada em:
   - Contagem de peças normais (100 pts) e Damas (300 pts)
   - Domínio da área central (20 pts)
   - Peças seguras nas bordas (10 pts)
   - Avanço de linha (direção à promoção) (5 pts)
   - Bônus dinâmico se tiver a chance de eliminar inimigos no próximo turno imediato (50 pts).

## 📂 Estrutura do Código (Classes Principais)

- **`MainInterfaceGrafica.java`**: A ponte principal do programa. Desenha a janela do Windows, cria os botões redondos em Swing, rastreia seus cliques e renderiza a movimentação das pedras. Conta com uma Thread paralela para rodar o motor da IA sem "congelar" a tela do computador.
- **`Arvore.java`**: O grande cérebro matemático. Contém a árvore de simulações lógicas, a recursão infinita do Minimax e o método avaliador de heurísticas (a intuição da IA).
- **`RegrasDamas.java`**: O juiz neutro da partida. Esta classe abriga dezenas de validações de matriz que dizem se uma diagonal é válida, se tem obstáculo no caminho e se existe alguma captura obrigatória oculta para ser feita.
- **`Tabuleiro.java`**: Modela os dados em estado puro (a planta baixa do jogo). Salva quem tem mais damas, a posição de todos os chares na matriz `char[][]` e o recém-implementado contador de empate por rodadas passivas.
- **`Jogada.java`**: Encapsula uma jogada simples. Ela tem inteligência própria na forma de Estrutura de Dados (Lista Encadeada) chamada `proximaCaptura`, que serve para guardar encadeamentos de múltiplos saltos em um único clique (combo).
- **`Node.java`**: Modela uma "foto" de um instante no tempo do tabuleiro. Usado para conectar a raiz principal nas pontas do Minimax, estruturando fisicamente a árvore em memória.

## 🕹️ Como Rodar e Jogar
1. Compile todos os arquivos Java dentro do seu editor/IDE.
2. Execute a classe principal `main.MainInterfaceGrafica`.
3. Responda à caixinha de diálogo se você quer ser as Brancas (jogam primeiro) ou Pretas, e escolha o quão genial a IA deve ser (Nível 1 a 9).
4. Clique em cima de uma peça sua, olhe a marcação em Roxo mostrando onde você pode pular, e divirta-se. Lembre-se, o nível 9 não tem misericórdia!
