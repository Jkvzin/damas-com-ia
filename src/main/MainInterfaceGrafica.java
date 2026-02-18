package main;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Douglas
 */
public final class MainInterfaceGrafica extends JFrame {

    private final int TAMANHO = 6;
    private final CasaBotao[][] tabuleiroInterface = new CasaBotao[TAMANHO][TAMANHO];
    
    /*
        Vazio: 0
        Brancas: 1
        Pretas: 2
        Damas: 3 (branca) ou 4 (preta)

        - Definir quem utilizará as peças brancas
        - Obrigatório comer a peça
        - Não é permitido comer pra trás
        - Uma peça comum pode comer múltiplas peças em qualquer direção, desde que a primeira seja para frente
        - A dama pode andar infinitas casas
        - A dama pode comer pra trás
        - A dama pode comer multiplas peças
        - A última peça a comida pela dama indica a posição que a dama deverá parar (posição subsequente na direção da comida)


    */
    private final Tabuleiro tabuleiroLogico; 

    private int linhaOrigem = -1, colOrigem = -1;

    private int vez = 1; // 1 é vez Branca e 2 é vez Preta

    private boolean sequenciaCaptura = false;

    public MainInterfaceGrafica() {
        
        /*
            TABULEIRO DO JOGO
        */
        tabuleiroLogico = new Tabuleiro();

        setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
        setSize(800, 800);
        setLayout(new GridLayout(TAMANHO, TAMANHO));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
                    tabuleiroInterface[i][j].setBackground(new Color(119, 149, 86));  // Verde
                }

                final int linha = i;
                final int coluna = j;
                tabuleiroInterface[i][j].addActionListener(e -> tratarClique(linha, coluna));
                add(tabuleiroInterface[i][j]);
            }
        }
    }

    private void tratarClique(int linha, int col) {
        boolean sucesso = false;
        boolean alguemPodeComer = false;



        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if (tabuleiroLogico.getMatriz()[i][j] != 0 && (tabuleiroLogico.getMatriz()[i][j] % 2 == vez % 2)) {
                    if (temCapturaDisponivel(i, j, sequenciaCaptura)) {
                        alguemPodeComer = true;
                        break;
                    }
                }
            }
            if (alguemPodeComer) break;
        }

        // Caso 1: Nenhuma peça selecionada ainda
        if (linhaOrigem == -1) {
            
            // Verifica se a casa clicada contém QUALQUER peça (1, 2, 3 ou 4)
             // adicionei não poder escolher uma casa bege
             // adicionei uma checagem para vez de quem é a vez
            if ((tabuleiroLogico.getMatriz()[linha][col] != 0) && (tabuleiroLogico.getMatriz()[linha][col] != -2) && ((vez % 2) == (tabuleiroLogico.getMatriz()[linha][col] % 2))) {
            
                if (alguemPodeComer && !temCapturaDisponivel(linha, col, sequenciaCaptura)) {
                    return; // Sai do método sem selecionar a peça
                }

                linhaOrigem = linha;
                colOrigem = col;
                tabuleiroInterface[linha][col].setBackground(Color.YELLOW); 
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

            // Eu pego a distancia entre o clique e a peça selecionada
            int distLinha = Math.abs(linha - linhaOrigem);
            int distCol = Math.abs(col - colOrigem);
            int peca = tabuleiroLogico.getMatriz()[linhaOrigem][colOrigem];

            // Movimento simples de uma peça comum
            if (distLinha == 1 && distCol == 1 && !alguemPodeComer) {
                // Verifico se a peça comum está andando para o lado certo
                if ((peca == 1 && linha < linhaOrigem) || (peca == 2 && linha > linhaOrigem)) {
                    sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                } else if (peca > 2) { // DAMA: Pode andar várias casas se o caminho estiver vazio
                    if (caminhoVazio(linhaOrigem, colOrigem, linha, col)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                    }
                }

                //Captura de uma peça
            } else if (alguemPodeComer) {
                if (peca <= 2 && distLinha == 2 && distCol == 2) {
                    int linhaMeio = (linha + linhaOrigem) / 2; // Linha da peça que será comida
                    int colMeio = (col + colOrigem) / 2;     // Coluna da peça que será comida
                    int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                    boolean sentidoValido = true;
                    if (!sequenciaCaptura) { 
                        if (peca == 1 && linha > linhaOrigem) sentidoValido = false; 
                        if (peca == 2 && linha < linhaOrigem) sentidoValido = false; 
                    }
                    // Verifico se tem inimigo no meio (Branca 1 ou 3 vs Preta 2 ou 4)
                    if (sentidoValido && pecaMeio != 0 && (pecaMeio % 2 != peca % 2)) {
                        sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                        if (sucesso) {
                            tabuleiroLogico.getMatriz()[linhaMeio][colMeio] = 0;
                        }
                    }
                }  else if (peca > 2) {
                    sucesso = tentarCapturaDama(linhaOrigem, colOrigem, linha, col);
                }
            }

            if (sucesso) {
                cancelarSelecao();

                sincronizarInterface();

                verificarFimDeJogo();

                boolean foiCaptura = (distLinha > 1);

                /*
                    VERIFICAÇÃO DE QUEM É A VEZ DE JOGAR E IMPLEMENTAÇÃO DA JOGADA DA IA
                */

                if (foiCaptura && temCapturaDisponivel(linha, col, true)) { 
                    sequenciaCaptura = true;
                    linhaOrigem = linha; 
                    colOrigem = col;
                    tabuleiroInterface[linhaOrigem][colOrigem].setBackground(Color.YELLOW);
                } else {
                    vez = (vez == 1) ? 2 : 1; 
                    sequenciaCaptura = false; 
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
        int brancas = 0, pretas = 0;
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                int p = tabuleiroLogico.getMatriz()[i][j];
                if (p == 1 || p == 3) brancas++;
                if (p == 2 || p == 4) pretas++;
            }
        }
        if (brancas == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Pretas venceram!");
        } else if (pretas == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Brancas venceram!");
        }
    }

    //método que checa se alguma peça é obrigada a comer ou não
    private boolean temCapturaDisponivel(int linha, int col, boolean emSequencia) {
        int peca = tabuleiroLogico.getMatriz()[linha][col];
        if (peca == 0) return false;

        // Sentidos da linha: Branca (1) sobe (-1), Preta (2) desce (+1)
        int[] sentidosLinha;

        // Se for Dama (3 ou 4) sempre come para todos os lados
        if (peca > 2) {
            sentidosLinha = new int[]{-1, 1};

            for (int dL : new int[]{-1, 1}) {
                for (int dC : new int[]{-1, 1}) {
                    // Varre a diagonal
                    for (int i = 1; i < TAMANHO; i++) {
                        int rInimigo = linha + (dL * i);
                        int cInimigo = col + (dC * i);
                        int rDestino = rInimigo + dL;
                        int cDestino = cInimigo + dC;

                        // Se sair do tabuleiro, para
                        if (rDestino < 0 || rDestino >= TAMANHO || cDestino < 0 || cDestino >= TAMANHO) break;

                        int pecaNoCaminho = tabuleiroLogico.getMatriz()[rInimigo][cInimigo];
                        if (pecaNoCaminho != 0) {
                            // Se encontrar peça própria, essa diagonal está bloqueada
                            if (pecaNoCaminho % 2 == peca % 2) break;
                            // Se encontrar inimiga, vê se a próxima casa está vazia
                            if (tabuleiroLogico.getMatriz()[rDestino][cDestino] == 0) return true;
                            else break; // Bloqueado por outra peça atrás da inimiga
                        }
                    }
                }
            }
        } 
        // Agora usamos o parâmetro 'emSequencia' em vez da variável global direta
        else if (emSequencia) {
            sentidosLinha = new int[]{-1, 1};
        } 
        else {
            sentidosLinha = (peca == 1) ? new int[]{-1} : new int[]{1};
        }

        for (int dLinha : sentidosLinha) {
            for (int dCol : new int[]{-1, 1}) { // Esquerda (-1) e Direita (1)
                int linhaMeio = linha + dLinha;
                int colMeio = col + dCol;
                int linhaDestino = linha + (dLinha * 2);
                int colDestino = col + (dCol * 2);

                // Verifica se o destino está dentro dos limites do tabuleiro
                if (linhaDestino >= 0 && linhaDestino < TAMANHO && colDestino >= 0 && colDestino < TAMANHO) {
                    int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];
                    int pecaDestino = tabuleiroLogico.getMatriz()[linhaDestino][colDestino];

                    // Regra principal: 
                    // 1. Tem que ter uma peça no meio (pecaMeio != 0)
                    // 2. A peça do meio tem que ser do oponente (pecaMeio % 2 != peca % 2)
                    // 3. A casa de destino tem que estar vazia (pecaDestino == 0)
                    if (pecaMeio != 0 && (pecaMeio % 2 != peca % 2) && pecaDestino == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //verificação para saber se  o caminho de uma dama está vazio ou se tem uma peça no meio
    private boolean caminhoVazio(int r1, int c1, int r2, int c2) {
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2 && c != c2) {
            if (tabuleiroLogico.getMatriz()[r][c] != 0) {
                return false; // Encontrou um obstáculo
            }
            r += dirLinha;
            c += dirCol;
        }
        return true;
    }

                                        //1 = origem; 2 = novo
    private boolean tentarCapturaDama(int r1, int c1, int r2, int c2) {
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int pecaInimigaLinha = -1;
        int pecaInimigaCol = -1;
        int contadorInimigos = 0;

        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2) {
            int pecaNoCaminho = tabuleiroLogico.getMatriz()[r][c];
            if (pecaNoCaminho != 0) {
                // Verifica se é peça do próprio time
                if (pecaNoCaminho % 2 == tabuleiroLogico.getMatriz()[r1][c1] % 2) return false;
                
                contadorInimigos++;
                pecaInimigaLinha = r;
                pecaInimigaCol = c;
            }
            r += dirLinha;
            c += dirCol;
        }

        // tem que ter exatamente uma peça inimiga no caminho
        if (contadorInimigos == 1) {
            int rAposInimiga = pecaInimigaLinha + dirLinha;
            int cAposInimiga = pecaInimigaCol + dirCol;

            if (r2 == rAposInimiga && c2 == cAposInimiga) {
                if (moverPecaLogica(r1, c1, r2, c2)) {
                    tabuleiroLogico.getMatriz()[pecaInimigaLinha][pecaInimigaCol] = 0;
                    return true;
                }
            }
        }
        return false;
    }



    private void cancelarSelecao() {
        if (linhaOrigem != -1) {
            // Restaura a cor original
            tabuleiroInterface[linhaOrigem][colOrigem].setBackground(new Color(119, 149, 86));
        }
        linhaOrigem = -1;
        colOrigem = -1;
    }

    private boolean moverPecaLogica(int r1, int c1, int r2, int c2) {
        
        // A casa de destino deve estar vazia
        if (tabuleiroLogico.getMatriz()[r2][c2] == 0) {
            
            // Transfere o valor (seja 1, 2, 3 ou 4) para a nova posição
            tabuleiroLogico.getMatriz()[r2][c2] = tabuleiroLogico.getMatriz()[r1][c1];
            tabuleiroLogico.getMatriz()[r1][c1] = 0;

            // Promoção simples para Dama (opcional)
            if (tabuleiroLogico.getMatriz()[r2][c2] == 2 && r2 == 5) {
                tabuleiroLogico.getMatriz()[r2][c2] = 4;
            }
            if (tabuleiroLogico.getMatriz()[r2][c2] == 1 && r2 == 0) {
                tabuleiroLogico.getMatriz()[r2][c2] = 3;
            }

            return true;
        }
        return false;
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
                int peca = tabuleiroLogico.getMatriz()[i][j];
                tabuleiroInterface[i][j].setTipoPeca(peca);
            }
        }
    }

    private class CasaBotao extends JButton {

        private int tipoPeca = 0;

        public void setTipoPeca(int tipo) {
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
            if (tipoPeca == 1 || tipoPeca == 3) { 
                g2.setColor(Color.WHITE);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                g2.setColor(Color.BLACK);
                g2.drawOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            // Pretas
            } else if (tipoPeca == 2 || tipoPeca == 4) { 
                g2.setColor(Color.BLACK);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            }

            // Representação de Dama (uma borda dourada)
            if (tipoPeca > 2) { 
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(margem + 5, margem + 5, getWidth() - 2 * margem - 10, getHeight() - 2 * margem - 10);
            }
        }
    }
}
