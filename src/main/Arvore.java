package main;

import java.util.ArrayList;

public class Arvore {

    private static final int TAMANHO = 6;
    private Node raiz;
    private int dificuldadeEscolhida;
    private int profundidadeCalculada;

    public static final char[][] MAPA_LETRAS = {
            { 0, 'A', 0, 'B', 0, 'C' },
            { 'D', 0, 'E', 0, 'F', 0 },
            { 0, 'G', 0, 'H', 0, 'I' },
            { 'J', 0, 'K', 0, 'L', 0 },
            { 0, 'M', 0, 'N', 0, 'O' },
            { 'P', 0, 'Q', 0, 'R', 0 }
    };

    private int corIA;
    private int corHumano;

    public Arvore(Tabuleiro tabuleiro, int corIA) {
        this(tabuleiro, corIA, 12);
    }

    public Arvore(Tabuleiro tabuleiro, int corIA, int dificuldadeEscolhida) {
        this.corIA = corIA;
        this.corHumano = (corIA == 1) ? 2 : 1;
        this.dificuldadeEscolhida = dificuldadeEscolhida;
        
        // Mapeamento linear exato: Dificuldade 1 = Profundidade 1, etc
        if (dificuldadeEscolhida == 10) {
            this.profundidadeCalculada = 12; // Modo Máximo (Minimax Puro)
        } else {
            this.profundidadeCalculada = dificuldadeEscolhida; // 1 ao 9
        }

        boolean isRaizBranca = (corIA == 1);
        this.raiz = new Node();
        this.raiz.setMatriz(copiarMatriz(tabuleiro.getMatriz()));
        this.raiz.setTurn(isRaizBranca);

        int turnoSendoProcessado = corIA;
        ArrayList<Jogada> jogadasDaRaiz = retornaJogadasPossiveis(tabuleiro, turnoSendoProcessado);


        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Instancia apenas os filhos diretos (Nível 1) no objeto 'Node'
        for (Jogada jogada : jogadasDaRaiz) {
            Tabuleiro tFilho = tabuleiro.clone();
            aplicarJogadaCompleta(tFilho, jogada);

            Node filhoNivel1 = new Node();
            filhoNivel1.setOrigem(jogada.getOrigem());
            filhoNivel1.setDest(jogada.getDestino());
            filhoNivel1.setMatriz(copiarMatriz(tFilho.getMatriz()));
            filhoNivel1.setTurn(!isRaizBranca); // Inverte o turno para o próximo humano
            
            // Avalia o galho profundamente repassando o Node atual
            int score = alphaBeta(tFilho, 1, alpha, beta, !isRaizBranca, filhoNivel1);
            filhoNivel1.setMiniMax(score);
            
            this.raiz.setChildren(filhoNivel1);

            bestValue = Math.max(bestValue, score);
            alpha = Math.max(alpha, bestValue);
        }
        
        this.raiz.setMiniMax(bestValue);
    }


    //func principal que constroi a arvore de possibilidades
    //usa a poda alfa-beta pra não gastar memoria calculando jogada que ja sabe que é ruim
    private int alphaBeta(Tabuleiro t, int nivel, int alpha, int beta, boolean isTurnoBrancas, Node noOrigem) {
        int turno = isTurnoBrancas ? 1 : 2;

        // Limite Absoluto
        if (nivel >= profundidadeCalculada) {
            if (dificuldadeEscolhida == 10) {
                return aplicarHeuristicaVitoria(t, isTurnoBrancas);
            } else {
                return simulacaoAleatoria(t, nivel, isTurnoBrancas);
            }
        }

        ArrayList<Jogada> jogadasPossiveis = retornaJogadasPossiveis(t, turno);

        if (jogadasPossiveis.isEmpty()) {
            return aplicarHeuristicaVitoria(t, isTurnoBrancas);
        }

        boolean isVezDaIA = (turno == corIA);

        if (isVezDaIA) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < jogadasPossiveis.size(); i++) {
                Jogada jogada = jogadasPossiveis.get(i);
                Tabuleiro clone = t.clone();
                aplicarJogadaCompleta(clone, jogada);

                Node filho = new Node();
                filho.setOrigem(jogada.getOrigem());
                filho.setDest(jogada.getDestino());
                filho.setMatriz(copiarMatriz(clone.getMatriz()));
                filho.setTurn(!isTurnoBrancas);
                if (noOrigem != null) noOrigem.setChildren(filho);

                int eval = alphaBeta(clone, nivel + 1, alpha, beta, !isTurnoBrancas, filho);
                filho.setMiniMax(eval);
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha)
                    break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < jogadasPossiveis.size(); i++) {
                Jogada jogada = jogadasPossiveis.get(i);
                Tabuleiro clone = t.clone();
                aplicarJogadaCompleta(clone, jogada);

                Node filho = new Node();
                filho.setOrigem(jogada.getOrigem());
                filho.setDest(jogada.getDestino());
                filho.setMatriz(copiarMatriz(clone.getMatriz()));
                filho.setTurn(!isTurnoBrancas);
                if (noOrigem != null) noOrigem.setChildren(filho);

                int eval = alphaBeta(clone, nivel + 1, alpha, beta, !isTurnoBrancas, filho);
                filho.setMiniMax(eval);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                if (beta <= alpha)
                    break;
            }
            return minEval;
        }
    }

    public static ArrayList<Jogada> retornaJogadasPossiveis(Tabuleiro tabuleiro, int vez) {
        ArrayList<Jogada> jogadas = new ArrayList<>();
        char[][] m = tabuleiro.getMatriz();
        boolean deveComer = RegrasDamas.alguemPodeComer(tabuleiro, vez);

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                int peca = m[i][j];
                if (peca == '0' || peca == 'b')
                    continue;
                if (peca % 2 != vez % 2)
                    continue;

                if (deveComer) {
                    if (RegrasDamas.temCapturaDisponivel(tabuleiro, i, j, false)) {
                        buscarCapturas(m, i, j, false, jogadas);
                    }
                } else {
                    buscarMovimentos(m, i, j, jogadas);
                }
            }
        }
        
        if (deveComer) {
            return filtrarMaioria(jogadas);
        }
        return jogadas;
    }

    public static int contarCapturas(Jogada j) {
        int count = 0;
        Jogada atual = j;
        while (atual != null) {
            if (atual.isCaptura()) count++;
            atual = atual.getProximaCaptura();
        }
        return count;
    }

    public static ArrayList<Jogada> filtrarMaioria(ArrayList<Jogada> jogadas) {
        if (jogadas.isEmpty()) return jogadas;
        int max = 0;
        for (Jogada j : jogadas) {
            int c = contarCapturas(j);
            if (c > max) max = c;
        }
        if (max == 0) return jogadas;

        ArrayList<Jogada> filtradas = new ArrayList<>();
        for (Jogada j : jogadas) {
            if (contarCapturas(j) == max) filtradas.add(j);
        }
        return filtradas;
    }

    /*
     * minimax
     * 0 empate
     * 1 vitória
     * -1 derrota
     * 
     * vitória + 100
     * derrota - 100
     * empate -1
     * dama + 2
     * normal + 1
     * se é vezIA e temCapturaDisponivel + 1
     * // // // // e combo possivel + (tamanho do combo)
     * //
     * 
     * turn - maximizar ou minimizar o valor do minimax dos filhos
     */
    private int aplicarHeuristicaVitoria(Tabuleiro t, boolean isTurnoBrancas) {
        char[][] m = t.getMatriz();

        int SCORE_VITORIA = 100000;
        int SCORE_EMPATE  = 0;
        int SCORE_PECA    = 100;
        int SCORE_DAMA    = 300;
        
        //pontos pra quem chega perto do meio, fica na borda e avança
        int SCORE_CENTRO  = 20;
        int SCORE_BORDA   = 10;
        int SCORE_AVANCO  = 5;

        int scoreIA = 0;
        int scoreHumano = 0;

        int pecasIA = 0;
        int pecasHumano = 0;

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char p = m[i][j];
                if (p == '0' || p == 'b')
                    continue;

                int timeDaPeca = (p == '1' || p == '3') ? 1 : 2;
                boolean isDama = (p == '3' || p == '4');

                int bonusPosicional = 0;

                // Bônus de Centro e Bordas
                if ((i == 2 || i == 3) && (j == 2 || j == 3)) {
                    bonusPosicional += SCORE_CENTRO; // Domínio Central
                } else if (j == 0 || j == TAMANHO - 1) {
                    bonusPosicional += SCORE_BORDA; // Casas Incomíveis
                }

                // Bônus de Avanço (Direcional)
                if (!isDama) {
                    if (timeDaPeca == 1) { // Brancas nascem embaixo e miram a linha 0
                        bonusPosicional += (TAMANHO - 1 - i) * SCORE_AVANCO;
                    } else { // Pretas nascem na linha 0 e miram a base (linha 5)
                        bonusPosicional += i * SCORE_AVANCO;
                    }
                }

                // Acumula na carteira da Equipe específica
                int valorDadoAPeca = (isDama ? SCORE_DAMA : SCORE_PECA) + bonusPosicional;

                if (timeDaPeca == corIA) {
                    scoreIA += valorDadoAPeca;
                    pecasIA++;
                } else {
                    scoreHumano += valorDadoAPeca;
                    pecasHumano++;
                }
            }
        }

        // Checagem de Eliminações Reais
        if (pecasHumano == 0) return SCORE_VITORIA;
        if (pecasIA == 0) return -SCORE_VITORIA;

        boolean humanoPodeMover = RegrasDamas.temMovimentoDisponivel(t, corHumano);
        boolean iaPodeMover = RegrasDamas.temMovimentoDisponivel(t, corIA);

        // Travamentos e Empate Absoluto
        if (!humanoPodeMover && !iaPodeMover) return SCORE_EMPATE; 
        if (!humanoPodeMover) return SCORE_VITORIA; // Adversário sufocado
        if (!iaPodeMover) return -SCORE_VITORIA; // IA sufocada
        
        // Empate por falta de capturas (20 lances = 10 rodadas completas)
        if (t.getJogadasSemCaptura() >= 20) {
            return SCORE_EMPATE;
        }

        // Duelo Infinito (1 Dama x 1 Dama sem capturas imediatas)
        if (pecasHumano == 1 && scoreHumano >= SCORE_DAMA && pecasIA == 1 && scoreIA >= SCORE_DAMA) {
            if (!RegrasDamas.alguemPodeComer(t, corHumano) && !RegrasDamas.alguemPodeComer(t, corIA)) {
                return SCORE_EMPATE;
            }
        }

        // Subtração de forças
        int score = scoreIA - scoreHumano;

        // Bônus de captura ou de ameaça
        int vezVigente = isTurnoBrancas ? 1 : 2;
        boolean isVezDaIA = (vezVigente == corIA);

        if (isVezDaIA && RegrasDamas.alguemPodeComer(t, corIA)) {
            score += 50; //incentiva a ia a comer se for o turno dela
        } else if (!isVezDaIA && RegrasDamas.alguemPodeComer(t, corHumano)) {
            score -= 50; //tira ponto se ela tiver com a propria peça ameaçada pelo humano
}
        return score;
    }

    //simulacao de monte carlo
    //ela joga na sorte ate o limite pra prever futuros incertos (nos niveis 1 ao 9)
    private int simulacaoAleatoria(Tabuleiro t, int nivelAtual, boolean isTurnoBrancas) {
        Tabuleiro clone = t.clone();
        int iteracoesAleatorias = 0;
        boolean turnoAtualBrancas = isTurnoBrancas;

        java.util.Random rand = new java.util.Random();

        while (nivelAtual + iteracoesAleatorias < 10) {
            int vezAtual = turnoAtualBrancas ? 1 : 2;
            ArrayList<Jogada> jogadasPossiveis = retornaJogadasPossiveis(clone, vezAtual);

            if (jogadasPossiveis.isEmpty()) {
                break; // Fim de jogo na simulação
            }

            int randomIndex = rand.nextInt(jogadasPossiveis.size());
            Jogada escolhida = jogadasPossiveis.get(randomIndex);

            aplicarJogadaCompleta(clone, escolhida);

            turnoAtualBrancas = !turnoAtualBrancas;
            iteracoesAleatorias++;
        }

        // Ao final das jogadas aleatórias, usa a heurística no tabuleiro simulado
        return aplicarHeuristicaVitoria(clone, turnoAtualBrancas);
    }

    public static void buscarMovimentos(char[][] m, int linha, int col, ArrayList<Jogada> jogadas) {
        int peca = m[linha][col];
        char origem = MAPA_LETRAS[linha][col];

        if (peca <= '2') {
            int dir = (peca == '1') ? -1 : 1;
            for (int dC : new int[] { -1, 1 }) {
                int nl = linha + dir, nc = col + dC;
                if (dentro(nl, nc) && m[nl][nc] == '0') {
                    jogadas.add(new Jogada(origem, MAPA_LETRAS[nl][nc]));
                }
            }
        } else {
            for (int dL : new int[] { -1, 1 }) {
                for (int dC : new int[] { -1, 1 }) {
                    int r = linha + dL, c = col + dC;
                    while (dentro(r, c) && m[r][c] == '0') {
                        jogadas.add(new Jogada(origem, MAPA_LETRAS[r][c]));
                        r += dL;
                        c += dC;
                    }
                }
            }
        }
    }

    public static void buscarCapturas(char[][] m, int linha, int col, boolean emSequencia,
            ArrayList<Jogada> resultado) {
        int peca = m[linha][col];
        char origem = MAPA_LETRAS[linha][col];

        ArrayList<int[]> capturas = encontrarCapturas(m, linha, col, peca, emSequencia);

        for (int[] cap : capturas) {
            int rd = cap[0], cd = cap[1];
            int rm = cap[2], cm = cap[3];

            char[][] copia = copiarMatriz(m);
            copia[rd][cd] = copia[linha][col];
            copia[linha][col] = '0';
            copia[rm][cm] = '0';

            if (copia[rd][cd] == '1' && rd == 0)
                copia[rd][cd] = '3';
            if (copia[rd][cd] == '2' && rd == TAMANHO - 1)
                copia[rd][cd] = '4';

            ArrayList<Jogada> subJogadas = new ArrayList<>();
            buscarCapturas(copia, rd, cd, true, subJogadas);

            if (subJogadas.isEmpty()) {
                resultado.add(new Jogada(origem, MAPA_LETRAS[rd][cd], true));
            } else {
                for (Jogada sub : subJogadas) {
                    Jogada jogada = new Jogada(origem, MAPA_LETRAS[rd][cd], true);
                    jogada.setProximaCaptura(sub);
                    resultado.add(jogada);
                }
            }
        }
    }

    public static ArrayList<int[]> encontrarCapturas(char[][] m, int linha, int col, int peca, boolean emSequencia) {
        ArrayList<int[]> capturas = new ArrayList<>();

        if (peca > '2') {
            if (emSequencia) {
                for (int dL : new int[] { -1, 1 }) {
                    for (int dC : new int[] { -1, 1 }) {
                        int ri = linha + dL, ci = col + dC;
                        int rd = linha + dL * 2, cd = col + dC * 2;
                        if (dentro(rd, cd) && ehInimiga(m, ri, ci, peca) && m[rd][cd] == '0') {
                            capturas.add(new int[] { rd, cd, ri, ci });
                        }
                    }
                }
            } else {
                for (int dL : new int[] { -1, 1 }) {
                    for (int dC : new int[] { -1, 1 }) {
                        for (int i = 1; i < TAMANHO; i++) {
                            int ri = linha + dL * i, ci = col + dC * i;
                            int rd = ri + dL, cd = ci + dC;
                            if (!dentro(rd, cd))
                                break;
                            if (m[ri][ci] != '0' && m[ri][ci] != 'b') {
                                if (m[ri][ci] % 2 == peca % 2)
                                    break;
                                if (m[rd][cd] == '0')
                                    capturas.add(new int[] { rd, cd, ri, ci });
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            int[] sentidos = emSequencia ? new int[] { -1, 1 }
                    : (peca == '1') ? new int[] { -1 } : new int[] { 1 };

            for (int dL : sentidos) {
                for (int dC : new int[] { -1, 1 }) {
                    int rm = linha + dL, cm = col + dC;
                    int rd = linha + dL * 2, cd = col + dC * 2;
                    if (dentro(rd, cd) && ehInimiga(m, rm, cm, peca) && m[rd][cd] == '0') {
                        capturas.add(new int[] { rd, cd, rm, cm });
                    }
                }
            }
        }
        return capturas;
    }

    public static void aplicarJogadaCompleta(Tabuleiro tabuleiro, Jogada jogada) {
        Jogada atual = jogada;
        boolean houveCaptura = false;
        
        while (atual != null) {
            Posicao orig = Jogada.getPosicaoDaLetra(atual.getOrigem());
            Posicao dest = Jogada.getPosicaoDaLetra(atual.getDestino());
            int r1 = orig.getLinha(), c1 = orig.getColuna();
            int r2 = dest.getLinha(), c2 = dest.getColuna();

            char[][] m = tabuleiro.getMatriz();

            m[r2][c2] = m[r1][c1];
            m[r1][c1] = '0';

            if (atual.isCaptura()) {
                houveCaptura = true;
                int dirL = Integer.signum(r2 - r1);
                int dirC = Integer.signum(c2 - c1);
                tabuleiro.removerPeca(r2 - dirL, c2 - dirC);
            }

            if (m[r2][c2] == '1' && r2 == 0) {
                tabuleiro.getDamas()[1]++;
                m[r2][c2] = '3';
            }
            if (m[r2][c2] == '2' && r2 == TAMANHO - 1) {
                tabuleiro.getDamas()[2]++;
                m[r2][c2] = '4';
            }

            atual = atual.getProximaCaptura();
        }
        
        if (houveCaptura) {
            tabuleiro.setJogadasSemCaptura(0);
        } else {
            tabuleiro.setJogadasSemCaptura(tabuleiro.getJogadasSemCaptura() + 1);
        }
    }

    public static boolean dentro(int linha, int col) {
        return linha >= 0 && linha < TAMANHO && col >= 0 && col < TAMANHO;
    }

    public static boolean ehInimiga(char[][] m, int linha, int col, int peca) {
        int p = m[linha][col];
        return p != '0' && p != 'b' && (p % 2 != peca % 2);
    }

    public static char[][] copiarMatriz(char[][] original) {
        char[][] copia = new char[TAMANHO][TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            copia[i] = original[i].clone();
        }
        return copia;
    }

    public Node getRaiz() {
        return raiz;
    }

    public int getDificuldadeEscolhida() {
        return dificuldadeEscolhida;
    }

    public int getProfundidadeCalculada() {
        return profundidadeCalculada;
    }
}