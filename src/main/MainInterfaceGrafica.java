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

    private int[] pecas = new int[]{0, TAMANHO, TAMANHO}; // a primeira posicao é ignoravel pra facilitar depois
    private int[] damas = new int[]{0, 0, 0};

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
        boolean realizouCaptura = false; // FLAG CORRIGIDA: Só muda para true se realmente comer uma peça

        // Verifico se tem alguma captura obrigatoria
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if ((tabuleiroLogico.getMatriz()[i][j] != '0') && (tabuleiroLogico.getMatriz()[i][j] != 'b') && (tabuleiroLogico.getMatriz()[i][j] % 2 == vez % 2)) {
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
            if ((tabuleiroLogico.getMatriz()[linha][col] != '0') && (tabuleiroLogico.getMatriz()[linha][col] != 'b') && ((vez % 2) == (tabuleiroLogico.getMatriz()[linha][col] % 2))) {
                if (alguemPodeComer && !temCapturaDisponivel(linha, col, sequenciaCaptura)) {
                    return; // Sai se for obrigado a comer mas clicou na peça errada
                }

                linhaOrigem = linha;
                colOrigem = col;
                tabuleiroInterface[linha][col].setBackground(Color.YELLOW); 
            }
        } 
        // Caso 2: Já existe uma peça selecionada, tentando mover
        else {
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
                    } 
                    else if (peca > '2') {
                        if (caminhoVazio(linhaOrigem, colOrigem, linha, col)) {
                            sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                        }
                    }
                } 
                else {
                    // MUDANÇA: Lógica de captura totalmente blindada
                    if (peca <= '2' && distLinha == 2) {
                        // Peça comum comendo
                        int linhaMeio = (linha + linhaOrigem) / 2;
                        int colMeio = (col + colOrigem) / 2;
                        int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                        boolean sentidoValido = true;
                        if (!sequenciaCaptura) { 
                            if (peca == '1' && linha > linhaOrigem) sentidoValido = false; 
                            if (peca == '2' && linha < linhaOrigem) sentidoValido = false; 
                        }
                        if (sentidoValido && pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2)) {
                            sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                            if (sucesso) {
                                if(tabuleiroLogico.getMatriz()[linhaMeio][colMeio] <= 2){
                                    pecas[tabuleiroLogico.getMatriz()[linhaMeio][colMeio]]--;
                                } else {
                                    damas[tabuleiroLogico.getMatriz()[linhaMeio][colMeio]]--;
                                    pecas[tabuleiroLogico.getMatriz()[linhaMeio][colMeio] - 2]--;
                                }
                                tabuleiroLogico.getMatriz()[linhaMeio][colMeio] = '0';
                                realizouCaptura = true; // Confirma que uma peça foi comida
                            }
                        }
                    } 
                    else if (peca > '2') {
                        // MUDANÇA: Tratamento diferenciado da Dama
                        if (sequenciaCaptura) {
                            // DAMA EM COMBO: Só pode comer peça adjacente (igual peça comum)
                            if (distLinha == 2) {
                                int linhaMeio = (linha + linhaOrigem) / 2;
                                int colMeio = (col + colOrigem) / 2;
                                int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];

                                if (pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2)) {
                                    sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);
                                    if (sucesso) {
                                        if(tabuleiroLogico.getMatriz()[linhaMeio][colMeio] <= 2){
                                            pecas[tabuleiroLogico.getMatriz()[linhaMeio][colMeio]]--;
                                        } else {
                                            damas[tabuleiroLogico.getMatriz()[linhaMeio][colMeio]]--;
                                            pecas[tabuleiroLogico.getMatriz()[linhaMeio][colMeio] - 2]--;
                                        }
                                        tabuleiroLogico.getMatriz()[linhaMeio][colMeio] = '0';
                                        realizouCaptura = true;
                                    }
                                }
                            }
                        } else {
                            // Primeira captura é distancia livre pra dama
                            if (distLinha > 1) {
                                sucesso = tentarCapturaDama(linhaOrigem, colOrigem, linha, col);
                                if (sucesso) {
                                    realizouCaptura = true;
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

                // MUDANÇA: Usa a nossa flag explícita, evitando que movimentos simples virem combos falsos
                if (realizouCaptura && temCapturaDisponivel(linha, col, true)) { 
                    sequenciaCaptura = true;
                    linhaOrigem = linha; 
                    colOrigem = col;
                    tabuleiroInterface[linhaOrigem][colOrigem].setBackground(Color.YELLOW);
                } else {
                    vez = (vez == 1) ? 2 : 1; 
                    sequenciaCaptura = false; 
                }
                
            } else {
                if (!sequenciaCaptura) {
                    cancelarSelecao();
                }
            }
        }
    }

    private void verificarFimDeJogo() {
        if (pecas[1] == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Pretas venceram!");
        } else if (pecas[2] == 0) {
            JOptionPane.showMessageDialog(this, "FIM DE JOGO! As Brancas venceram!");
        }
    }

    private boolean temCapturaDisponivel(int linha, int col, boolean emSequencia) {
        int peca = tabuleiroLogico.getMatriz()[linha][col];
        if (peca == '0' || peca == 'b') return false;

        // MUDANÇA: Verificação da Dama reescrita para aceitar regra do combo
        if (peca > '2') {
            if (emSequencia) {
                // REGRA DA SEQUÊNCIA: Dama só detecta captura se a inimiga estiver logo ali (Adjacente)
                for (int dL : new int[]{-1, 1}) {
                    for (int dC : new int[]{-1, 1}) {
                        int rInimigo = linha + dL;
                        int cInimigo = col + dC;
                        int rDestino = linha + (dL * 2);
                        int cDestino = col + (dC * 2);

                        if (rDestino >= 0 && rDestino < TAMANHO && cDestino >= 0 && cDestino < TAMANHO) {
                            int pecaInimiga = tabuleiroLogico.getMatriz()[rInimigo][cInimigo];
                            int pecaDest = tabuleiroLogico.getMatriz()[rDestino][cDestino];

                            if (pecaInimiga != '0' && pecaInimiga != 'b' && (pecaInimiga % 2 != peca % 2) && pecaDest == '0') {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else {
                // PRIMEIRA CAPTURA: Varre toda a diagonal como de costume
                for (int dL : new int[]{-1, 1}) {
                    for (int dC : new int[]{-1, 1}) {
                        for (int i = 1; i < TAMANHO; i++) {
                            int rInimigo = linha + (dL * i);
                            int cInimigo = col + (dC * i);
                            int rDestino = rInimigo + dL;
                            int cDestino = cInimigo + dC;

                            if (rDestino < 0 || rDestino >= TAMANHO || cDestino < 0 || cDestino >= TAMANHO) break;

                            int pecaNoCaminho = tabuleiroLogico.getMatriz()[rInimigo][cInimigo];
                            if (pecaNoCaminho != '0' && pecaNoCaminho != 'b') {
                                if (pecaNoCaminho % 2 == peca % 2) break; // Bloqueio aliado
                                if (tabuleiroLogico.getMatriz()[rDestino][cDestino] == '0') return true;
                                else break; // Bloqueado por outra peça atrás
                            }
                        }
                    }
                }
                return false; 
            }
        } 

        // Lógica para peças normais
        int[] sentidosLinha;
        if (emSequencia) {
            sentidosLinha = new int[]{-1, 1};
        } else {
            sentidosLinha = (peca == '1') ? new int[]{-1} : new int[]{1};
        }

        for (int dLinha : sentidosLinha) {
            for (int dCol : new int[]{-1, 1}) { 
                int linhaMeio = linha + dLinha;
                int colMeio = col + dCol;
                int linhaDestino = linha + (dLinha * 2);
                int colDestino = col + (dCol * 2);

                if (linhaDestino >= 0 && linhaDestino < TAMANHO && colDestino >= 0 && colDestino < TAMANHO) {
                    int pecaMeio = tabuleiroLogico.getMatriz()[linhaMeio][colMeio];
                    int pecaDestino = tabuleiroLogico.getMatriz()[linhaDestino][colDestino];

                    if (pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2) && pecaDestino == '0') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void print(){
        for(int i = 0; i < TAMANHO; i++){
            for(int j = 0; j < TAMANHO; j++){
                System.out.print((char)tabuleiroLogico.getMatriz()[i][j] + " ");
            }
            System.out.println("|");
        }
        System.out.println("---------------------------------");
    }

    private boolean caminhoVazio(int r1, int c1, int r2, int c2) {
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        do{
            if (tabuleiroLogico.getMatriz()[r][c] != '0') {
                return false; 
            }
            r += dirLinha;
            c += dirCol;
        } while (r != r2 && c != c2);
            
        return true;
    }

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
            if (pecaNoCaminho != '0' && pecaNoCaminho != 'b') {
                if (pecaNoCaminho % 2 == tabuleiroLogico.getMatriz()[r1][c1] % 2) return false;
                
                contadorInimigos++;
                pecaInimigaLinha = r;
                pecaInimigaCol = c;
            }
            r += dirLinha;
            c += dirCol;
        }

        if (contadorInimigos == 1) {
            int rAposInimiga = pecaInimigaLinha + dirLinha;
            int cAposInimiga = pecaInimigaCol + dirCol;

            // Mantido intacto porque ele obedece sua regra: A Dama TEM que parar exatamente após a peça
            if (r2 == rAposInimiga && c2 == cAposInimiga) {
                if (moverPecaLogica(r1, c1, r2, c2)) {
                    tabuleiroLogico.getMatriz()[pecaInimigaLinha][pecaInimigaCol] = '0';
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

            // Promoção simples para Dama 
            if (tabuleiroLogico.getMatriz()[r2][c2] == '2' && r2 == 5) {
                damas[2]++;
                tabuleiroLogico.getMatriz()[r2][c2] = '4';
            }
            if (tabuleiroLogico.getMatriz()[r2][c2] == '1' && r2 == 0) {
                damas[1]++;
                tabuleiroLogico.getMatriz()[r2][c2] = '3';
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

        private int tipoPeca = '0';

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
            if (tipoPeca > '2' && tipoPeca != 'b') { 
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(margem + 5, margem + 5, getWidth() - 2 * margem - 10, getHeight() - 2 * margem - 10);
            }
        }
    }
}
