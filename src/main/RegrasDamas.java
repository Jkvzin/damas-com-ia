package main;

public class RegrasDamas {

    private static final int TAMANHO = 6;

    /**
     * Verifica se alguma peça do jogador da vez pode capturar.
     */
    public static boolean alguemPodeComer(Tabuleiro tabuleiro, int vez) {
        char[][] m = tabuleiro.getMatriz();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if (m[i][j] != '0' && m[i][j] != 'b' && (m[i][j] % 2 == vez % 2)) {
                    if (temCapturaDisponivel(tabuleiro, i, j, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica se a peça na posição (linha, col) tem alguma captura disponível.
     * 
     * @param emSequencia se true, indica que estamos numa sequência de capturas
     *                    (combo)
     */
    public static boolean temCapturaDisponivel(Tabuleiro tabuleiro, int linha, int col, boolean emSequencia) {
        char[][] m = tabuleiro.getMatriz();
        int peca = m[linha][col];
        if (peca == '0' || peca == 'b')
            return false;

        // MUDANÇA: Verificação da Dama reescrita para aceitar regra do combo
        if (peca > '2') {
            if (emSequencia) {
                // REGRA DA SEQUÊNCIA: Dama só detecta captura se a inimiga estiver logo ali
                // (Adjacente)
                for (int dL : new int[] { -1, 1 }) {
                    for (int dC : new int[] { -1, 1 }) {
                        int rInimigo = linha + dL;
                        int cInimigo = col + dC;
                        int rDestino = linha + (dL * 2);
                        int cDestino = col + (dC * 2);

                        if (rDestino >= 0 && rDestino < TAMANHO && cDestino >= 0 && cDestino < TAMANHO) {
                            int pecaInimiga = m[rInimigo][cInimigo];
                            int pecaDest = m[rDestino][cDestino];

                            if (pecaInimiga != '0' && pecaInimiga != 'b' && (pecaInimiga % 2 != peca % 2)
                                    && pecaDest == '0') {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else {
                // PRIMEIRA CAPTURA: Varre toda a diagonal como de costume
                for (int dL : new int[] { -1, 1 }) {
                    for (int dC : new int[] { -1, 1 }) {
                        for (int i = 1; i < TAMANHO; i++) {
                            int rInimigo = linha + (dL * i);
                            int cInimigo = col + (dC * i);
                            int rDestino = rInimigo + dL;
                            int cDestino = cInimigo + dC;

                            if (rDestino < 0 || rDestino >= TAMANHO || cDestino < 0 || cDestino >= TAMANHO)
                                break;

                            int pecaNoCaminho = m[rInimigo][cInimigo];
                            if (pecaNoCaminho != '0' && pecaNoCaminho != 'b') {
                                if (pecaNoCaminho % 2 == peca % 2)
                                    break; // Bloqueio aliado
                                if (m[rDestino][cDestino] == '0')
                                    return true;
                                else
                                    break; // Bloqueado por outra peça atrás
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
            sentidosLinha = new int[] { -1, 1 };
        } else {
            sentidosLinha = (peca == '1') ? new int[] { -1 } : new int[] { 1 };
        }

        for (int dLinha : sentidosLinha) {
            for (int dCol : new int[] { -1, 1 }) {
                int linhaMeio = linha + dLinha;
                int colMeio = col + dCol;
                int linhaDestino = linha + (dLinha * 2);
                int colDestino = col + (dCol * 2);

                if (linhaDestino >= 0 && linhaDestino < TAMANHO && colDestino >= 0 && colDestino < TAMANHO) {
                    int pecaMeio = m[linhaMeio][colMeio];
                    int pecaDestino = m[linhaDestino][colDestino];

                    if (pecaMeio != '0' && pecaMeio != 'b' && (pecaMeio % 2 != peca % 2) && pecaDestino == '0') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica se o caminho diagonal entre (r1,c1) e (r2,c2) está vazio.
     */
    public static boolean caminhoVazio(Tabuleiro tabuleiro, int r1, int c1, int r2, int c2) {
        char[][] m = tabuleiro.getMatriz();
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2 && c != c2) {
            if (m[r][c] != '0') {
                return false;
            }
            r += dirLinha;
            c += dirCol;
        }

        return true;
    }

    /**
     * Tenta realizar uma captura com a dama (distância livre na primeira captura).
     * Retorna um array {pecaInimigaLinha, pecaInimigaCol} se a captura é válida, ou
     * null se não.
     */
    public static int[] tentarCapturaDama(Tabuleiro tabuleiro, int r1, int c1, int r2, int c2) {
        char[][] m = tabuleiro.getMatriz();
        int dirLinha = (r2 > r1) ? 1 : -1;
        int dirCol = (c2 > c1) ? 1 : -1;
        int pecaInimigaLinha = -1;
        int pecaInimigaCol = -1;
        int contadorInimigos = 0;

        int r = r1 + dirLinha;
        int c = c1 + dirCol;

        while (r != r2) {
            int pecaNoCaminho = m[r][c];
            if (pecaNoCaminho != '0' && pecaNoCaminho != 'b') {
                if (pecaNoCaminho % 2 == m[r1][c1] % 2)
                    return null;

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

            // Mantido intacto porque ele obedece sua regra: A Dama TEM que parar exatamente
            // após a peça
            if (r2 == rAposInimiga && c2 == cAposInimiga) {
                return new int[] { pecaInimigaLinha, pecaInimigaCol };
            }
        }
        return null;
    }

    /**
     * Verifica se o jogador da vez tem algum movimento ou captura disponível.
     * Retorna false se o jogador está completamente bloqueado (perde).
     */
    public static boolean temMovimentoDisponivel(Tabuleiro tabuleiro, int vez) {
        char[][] m = tabuleiro.getMatriz();
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                int peca = m[i][j];
                if (peca == '0' || peca == 'b')
                    continue;
                if (peca % 2 != vez % 2)
                    continue;

                // Verifica se tem captura
                if (temCapturaDisponivel(tabuleiro, i, j, false))
                    return true;

                // Verifica se tem movimento simples
                if (peca <= '2') {
                    // Peça comum: só anda 1 casa pra frente na diagonal
                    int direcao = (peca == '1') ? -1 : 1;
                    for (int dCol : new int[] { -1, 1 }) {
                        int novaLinha = i + direcao;
                        int novaCol = j + dCol;
                        if (novaLinha >= 0 && novaLinha < TAMANHO && novaCol >= 0 && novaCol < TAMANHO) {
                            if (m[novaLinha][novaCol] == '0')
                                return true;
                        }
                    }
                } else {
                    // Dama: pode andar infinitas casas em qualquer diagonal
                    for (int dL : new int[] { -1, 1 }) {
                        for (int dC : new int[] { -1, 1 }) {
                            int r = i + dL;
                            int c = j + dC;
                            while (r >= 0 && r < TAMANHO && c >= 0 && c < TAMANHO) {
                                if (m[r][c] == '0')
                                    return true;
                                if (m[r][c] != '0')
                                    break; // Bloqueio
                                r += dL;
                                c += dC;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
