package main;

import java.util.ArrayList;

public class Arvore {

    private static final int TAMANHO = 6;
    private Node raiz;
    private int profundidadeMaxima;

    private static final char[][] MAPA_LETRAS = {
            { 0, 'A', 0, 'B', 0, 'C' },
            { 'D', 0, 'E', 0, 'F', 0 },
            { 0, 'G', 0, 'H', 0, 'I' },
            { 'J', 0, 'K', 0, 'L', 0 },
            { 0, 'M', 0, 'N', 0, 'O' },
            { 'P', 0, 'Q', 0, 'R', 0 }
    };

    public Arvore(Tabuleiro tabuleiro, boolean turno) {
        this(tabuleiro, turno, 10);
    }

    public Arvore(Tabuleiro tabuleiro, boolean turno, int profundidadeMaxima) {
        this.profundidadeMaxima = profundidadeMaxima;
        this.raiz = new Node();
        this.raiz.setMatriz(copiarMatriz(tabuleiro.getMatriz()));
        this.raiz.setTurn(turno);
        construir(raiz, tabuleiro, 0);
    }

    private void construir(Node pai, Tabuleiro tabuleiro, int profundidade) {
        if (profundidade >= profundidadeMaxima)
            return;

        int vez = pai.getTurn() ? 1 : 2;
        ArrayList<Jogada> jogadasPossiveis = retornaJogadasPossiveis(tabuleiro, vez);

        for (Jogada jogada : jogadasPossiveis) {
            // Clona o tabuleiro e aplica a jogada completa (incluindo combos)
            Tabuleiro clone = tabuleiro.clone();
            aplicarJogadaCompleta(clone, jogada);

            // Cria o nó filho
            Node filho = new Node();
            filho.setOrigem(jogada.getOrigem());
            filho.setDest(jogada.getDestino());
            filho.setMatriz(clone.getMatriz()); // usa direto, clone não é mais modificado
            filho.setTurn(!pai.getTurn());

            // Adiciona na árvore
            pai.setChildren(filho);

            // Desce recursivamente
            construir(filho, clone, profundidade + 1);
        }
    }

    public ArrayList<Jogada> retornaJogadasPossiveis(Tabuleiro tabuleiro, int vez) {
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
        return jogadas;
    }

    private void buscarMovimentos(char[][] m, int linha, int col, ArrayList<Jogada> jogadas) {
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

    private void buscarCapturas(char[][] m, int linha, int col, boolean emSequencia,
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

    private ArrayList<int[]> encontrarCapturas(char[][] m, int linha, int col, int peca, boolean emSequencia) {
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

    private void aplicarJogadaCompleta(Tabuleiro tabuleiro, Jogada jogada) {
        Jogada atual = jogada;
        while (atual != null) {
            Posicao orig = Jogada.getPosicaoDaLetra(atual.getOrigem());
            Posicao dest = Jogada.getPosicaoDaLetra(atual.getDestino());
            int r1 = orig.getLinha(), c1 = orig.getColuna();
            int r2 = dest.getLinha(), c2 = dest.getColuna();

            char[][] m = tabuleiro.getMatriz();

            m[r2][c2] = m[r1][c1];
            m[r1][c1] = '0';

            if (atual.isCaptura()) {
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
    }

    private boolean dentro(int linha, int col) {
        return linha >= 0 && linha < TAMANHO && col >= 0 && col < TAMANHO;
    }

    private boolean ehInimiga(char[][] m, int linha, int col, int peca) {
        int p = m[linha][col];
        return p != '0' && p != 'b' && (p % 2 != peca % 2);
    }

    private char[][] copiarMatriz(char[][] original) {
        char[][] copia = new char[TAMANHO][TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            copia[i] = original[i].clone();
        }
        return copia;
    }

    public Node getRaiz() {
        return raiz;
    }

    public int getProfundidadeMaxima() {
        return profundidadeMaxima;
    }
}