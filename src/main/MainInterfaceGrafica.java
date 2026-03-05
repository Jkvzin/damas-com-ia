package main;

import javax.swing.*;
import java.awt.*;

public final class MainInterfaceGrafica extends JFrame {

    private final int TAMANHO = 6;
    private final CasaBotao[][] tabuleiroInterface = new CasaBotao[TAMANHO][TAMANHO];

    /*
     * Vazio: 0
     * Brancas: 1
     * Pretas: 2
     * Damas: 3 (branca) ou 4 (preta)
     * 
     * -> REGRAS DO JOGO
     * 
     * - DEFINIR QUEM UTILIZARÁ AS PEÇAS BRANCAS (COMEÇA O JOGO)
     * - OBRIGATÓRIO COMER A PEÇA
     * - NÃO É PERMITIDO COMER PRA TRÁS
     * - UMA PEÇA PODE COMER MÚLTIPLAS PEÇAS, EM QUALQUER
     * DIREÇÃO, DESDE QUE A PRIMEIRA SEJA PARA FRENTE
     * - A DAMA PODE ANDAR INFINITAS CASAS, RESPEITANDO O LIMITE DO TABULEIRO
     * - A DAMA PODE COMER PRA TRÁS
     * - A DAMA PODE COMER MÚLTIPLAS PEÇAS
     * - A PEÇA A SER COMIDA PELA DAMA INDICA A POSIÇÃO QUE A DAMA DEVERÁ PARAR
     * (POSIÇÃO SUBSEQUENTE NA DIREÇÃO DA COMIDA)
     * - NA IMPOSSIBILIDADE DE EFETUAR JOGADAS, O JOGADOR TRAVADO PERDE O JOGO
     * 
     * 
     * => SE EXISTIREM SOMENTE DUAS DAMAS E NÃO FOR POSSÍVEL COMER, ENTÃO EMPATE
     */
    private final Tabuleiro tabuleiroLogico;
    private int linhaOrigem = -1, colOrigem = -1;
    private int vez = 1; // 1 é vez Branca e 2 é vez Preta
    private boolean sequenciaCaptura = false;
    private boolean vezIA = true; // true = IA está ativada (joga como pretas)

    public MainInterfaceGrafica() {

        /*
         * TABULEIRO DO JOGO
         */
        tabuleiroLogico = new Tabuleiro();

        setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
        setSize(800, 800);
        setLayout(new GridLayout(TAMANHO, TAMANHO));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        inicializarComponentes();
        sincronizarInterface();

        setVisible(true);
    }

    private void inicializarComponentes() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                tabuleiroInterface[i][j] = new CasaBotao();

                // Cores do tabuleiro
                if ((i + j) % 2 == 0) {
                    tabuleiroInterface[i][j].setBackground(new Color(235, 235, 208)); // Bege
                } else {
                    tabuleiroInterface[i][j].setBackground(new Color(119, 149, 86)); // Verde
                }

                int linha = i;
                int coluna = j;
                tabuleiroInterface[i][j].addActionListener(e -> tratarClique(linha, coluna));
                add(tabuleiroInterface[i][j]);
            }
        }

    }

    /*
     * 1. Se não tem peça selecionada:
     * a. Verifica se alguem pode comer
     * b. Se sim, só pode selecionar peças que podem comer
     * c. Se não, pode selecionar qualquer peça
     * d. Seleciona a peça
     * 2. Se tem peça selecionada:
     * a. Verifica se é uma jogada valida
     * b. Se sim, executa a jogada
     * c. Se não, cancela a seleção
     */
    private void tratarClique(int linha, int col) {
        boolean sucesso = false;
        boolean realizouCaptura = false;

        // Verifico se tem alguma captura obrigatória
        boolean alguemPodeComer;
        if (sequenciaCaptura) {
            // Durante combo, verifico se a peça do combo pode capturar
            alguemPodeComer = RegrasDamas.temCapturaDisponivel(tabuleiroLogico, linhaOrigem, colOrigem, true);
        } else {
            alguemPodeComer = RegrasDamas.alguemPodeComer(tabuleiroLogico, vez);
        }

        // Caso 1: Nenhuma peça selecionada ainda
        if (linhaOrigem == -1) {

            // Verifica se a casa clicada contém peça do jogador da vez
            if ((tabuleiroLogico.getMatriz()[linha][col] != '0') && (tabuleiroLogico.getMatriz()[linha][col] != 'b')
                    && ((vez % 2) == (tabuleiroLogico.getMatriz()[linha][col] % 2))) {
                if (alguemPodeComer
                        && !RegrasDamas.temCapturaDisponivel(tabuleiroLogico, linha, col, sequenciaCaptura)) {
                    return; // Sai se for obrigado a comer mas clicou na peça errada
                }

                linhaOrigem = linha;
                colOrigem = col;
                tabuleiroInterface[linha][col].setBackground(Color.YELLOW); // Destaque do clique
            }
        }
        // Caso 2: Já existe uma peça selecionada, tentando mover
        else {

            // Se clicar na mesma peça, cancela a seleção
            if (linhaOrigem == linha && colOrigem == col) {
                if (!sequenciaCaptura) {
                    cancelarSelecao();
                }
                return;
            }

            int distLinha = Math.abs(linha - linhaOrigem);
            int distCol = Math.abs(col - colOrigem);
            int peca = tabuleiroLogico.getMatriz()[linhaOrigem][colOrigem];

            if (distLinha == distCol && distLinha > 0) {

                if (!alguemPodeComer) {
                    // Movimento simples
                    if (peca <= '2' && distLinha == 1) {
                        if ((peca == '1' && linha < linhaOrigem) || (peca == '2' && linha > linhaOrigem)) {
                            sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                        }
                    } else if (peca > '2') {
                        if (RegrasDamas.caminhoVazio(tabuleiroLogico, linhaOrigem, colOrigem, linha, col)) {
                            sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                        }
                    }
                } else {
                    if (peca <= '2' && distLinha == 2) {
                        // Peça comum comendo
                        int linhaMeio = (linha + linhaOrigem) / 2;
                        int colMeio = (col + colOrigem) / 2;
                        int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                        boolean sentidoValido = true;
                        if (!sequenciaCaptura) {
                            if (peca == '1' && linha > linhaOrigem)
                                sentidoValido = false;
                            if (peca == '2' && linha < linhaOrigem)
                                sentidoValido = false;
                        }
                        if (sentidoValido && pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2)) {
                            sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                            if (sucesso) {
                                tabuleiroLogico.removerPeca(linhaMeio, colMeio);
                                realizouCaptura = true;
                            }
                        }
                    } else if (peca > '2') {
                        // Tratamento diferenciado da Dama
                        if (sequenciaCaptura) {
                            // Em combo só pode comer peça adjacente
                            if (distLinha == 2) {
                                int linhaMeio = (linha + linhaOrigem) / 2;
                                int colMeio = (col + colOrigem) / 2;
                                int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                                if (pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2)) {
                                    sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                                    if (sucesso) {
                                        tabuleiroLogico.removerPeca(linhaMeio, colMeio);
                                        realizouCaptura = true;
                                    }
                                }
                            }
                        } else {
                            // Primeira captura é distância livre pra dama
                            if (distLinha > 1) {
                                int[] posInimiga = RegrasDamas.tentarCapturaDama(tabuleiroLogico, linhaOrigem,
                                        colOrigem, linha, col);
                                if (posInimiga != null) {
                                    sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                                    if (sucesso) {
                                        tabuleiroLogico.removerPeca(posInimiga[0], posInimiga[1]);
                                        realizouCaptura = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (sucesso) {
                cancelarSelecao();
                print();
                sincronizarInterface();
                verificarFimDeJogo();

                // Verifica se pode continuar comendo
                if (realizouCaptura && RegrasDamas.temCapturaDisponivel(tabuleiroLogico, linha, col, true)) {
                    sequenciaCaptura = true;
                    linhaOrigem = linha;
                    colOrigem = col;
                    tabuleiroInterface[linhaOrigem][colOrigem].setBackground(Color.YELLOW);
                } else {
                    vez = (vez == 1) ? 2 : 1;
                    sequenciaCaptura = false;

                    // Se for a vez da IA, constrói a árvore e calcula possibilidades
                    if (vezIA) {
                        System.out.println("\n=== VEZ DA IA ===");
                        long inicio = System.currentTimeMillis();
                        Arvore arvore = new Arvore(tabuleiroLogico.clone(), false); // false = pretas
                        long fim = System.currentTimeMillis();

                        int totalNos = contarNos(arvore.getRaiz());
                        System.out.println("Profundidade máxima: " + arvore.getProfundidadeMaxima());
                        System.out.println("Total de nós (possibilidades): " + totalNos);
                        System.out.println("Filhos diretos da raiz: " + arvore.getRaiz().getChildren().size());
                        System.out.println("Tempo de construção: " + (fim - inicio) + " ms");
                        System.out.println("==================\n");
                    }
                }

            } else {
                // Se o movimento for inválido (ex: clicar em cima de outra peça)
                if (!sequenciaCaptura) {
                    cancelarSelecao();
                }
            }
        }
    }

    private void verificarFimDeJogo() {
        int[] pecas = tabuleiroLogico.getPecas();
        if (pecas[1] == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Pretas venceram!");
        } else if (pecas[2] == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Brancas venceram!");
        }

        // Se o jogador estiver travado ele perde
        if (!RegrasDamas.temMovimentoDisponivel(tabuleiroLogico, vez)) {
            String vencedor = (vez == 1) ? "Pretas" : "Brancas";
            JOptionPane.showMessageDialog(this,
                    "FIM DE JOGO! As " + vencedor + " venceram! (adversário bloqueado)");
        }
    }

    private void print() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                System.out.print((char) tabuleiroLogico.getMatriz()[i][j] + " ");
            }
            System.out.println("|");
        }
        System.out.println("---------------------------------");
    }

    private boolean moverPecaLogica(int r1, int c1, int r2, int c2) {

        // A casa de destino deve estar vazia
        if (tabuleiroLogico.getMatriz()[r2][c2] == '0') {

            // Transfere o valor (seja 1, 2, 3 ou 4) para a nova posição
            tabuleiroLogico.getMatriz()[r2][c2] = tabuleiroLogico.getMatriz()[r1][c1];
            tabuleiroLogico.getMatriz()[r1][c1] = '0';

            // Promoção simples para Dama
            if (tabuleiroLogico.getMatriz()[r2][c2] == '2' && r2 == 5) {
                tabuleiroLogico.getDamas()[2]++;
                tabuleiroLogico.getMatriz()[r2][c2] = '4';
            }
            if (tabuleiroLogico.getMatriz()[r2][c2] == '1' && r2 == 0) {
                tabuleiroLogico.getDamas()[1]++;
                tabuleiroLogico.getMatriz()[r2][c2] = '3';
            }

            return true;
        }
        return false;
    }

    private static int contarNos(Node node) {
        int count = 1;
        for (Node filho : node.getChildren()) {
            count += contarNos(filho);
        }
        return count;
    }

    private void cancelarSelecao() {
        if (linhaOrigem != -1) {
            // Restaura a cor original
            tabuleiroInterface[linhaOrigem][colOrigem].setBackground(new Color(119, 149, 86));
        }
        linhaOrigem = -1;
        colOrigem = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainInterfaceGrafica::new);
    }

    /*
     * Atualiza a interface gráfica com base na matriz lógica do Tabuleiro. Este
     * método será chamado após cada jogada da IA.
     */
    public void sincronizarInterface() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = tabuleiroLogico.getMatriz()[i][j];
                tabuleiroInterface[i][j].setTipoPeca(peca);
            }
        }
    }

    private class CasaBotao extends JButton {

        private int tipoPeca = '0';

        public void setTipoPeca(char tipo) {
            this.tipoPeca = tipo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margem = 10;
            // Brancas
            if (tipoPeca == '1' || tipoPeca == '3') {
                g2.setColor(Color.WHITE);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                g2.setColor(Color.BLACK);
                g2.drawOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                // Pretas
            } else if (tipoPeca == '2' || tipoPeca == '4') {
                g2.setColor(Color.BLACK);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            }

            // Representação de Dama (uma borda dourada)
            if (tipoPeca == '3' || tipoPeca == '4') {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(margem + 5, margem + 5, getWidth() - 2 * margem - 10, getHeight() - 2 * margem - 10);
            }
        }
    }
}
