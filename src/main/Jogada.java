package main;

import java.util.HashMap;
import java.util.Map;

/**
 *   0 1 2 3 4 5
 * 0 . A . B . C
 * 1 D . E . F .
 * 2 . G . H . I
 * 3 J . K . L .
 * 4 . M . N . O
 * 5 P . Q . R .
 */


class Posicao {
    private final int linha;
    private final int coluna;

    public Posicao (int linha, int coluna){
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() { return linha; }
    public int getColuna() { return coluna; }
}

public class Jogada {

    private static final Map<Character, Posicao> LETRA_PARA_POSICAO = new HashMap<>();
    private static final Map<Posicao, Character> POSICAO_PARA_LETRA = new HashMap<>();

    static {
        registrar('A', 0, 1);
        registrar('B', 0, 3);
        registrar('C', 0, 5);
        registrar('D', 1, 0);
        registrar('E', 1, 2);
        registrar('F', 1, 4);
        registrar('G', 2, 1);
        registrar('H', 2, 3);
        registrar('I', 2, 5);
        registrar('J', 3, 0);
        registrar('K', 3, 2);
        registrar('L', 3, 4);
        registrar('M', 4, 1);
        registrar('N', 4, 3);
        registrar('O', 4, 5);
        registrar('P', 5, 0);
        registrar('Q', 5, 2);
        registrar('R', 5, 4);
    }

    private static void registrar(char letra, int linha, int col) {
        Posicao pos = new Posicao(linha, col);
        LETRA_PARA_POSICAO.put(letra, pos);
        POSICAO_PARA_LETRA.put(pos, letra);
    }

    private final char origem;
    private final char destino;
    private final boolean captura;
    private Jogada proximaCaptura;

    public Jogada(char origem, char destino) {
        this.origem = origem;
        this.destino = destino;
        this.captura = false;
    }

    public Jogada(char origem, char destino, boolean captura) {
        this.origem = origem;
        this.destino = destino;
        this.captura = captura;
    }

    public static char getLetraDaPosicao(int linha, int col) {
        Posicao busca = new Posicao(linha, col);
        Character letra = POSICAO_PARA_LETRA.get(busca);
        if (letra == null) {
            throw new IllegalArgumentException("Posição fora das casas válidas: " + linha + "," + col);
        }
        return letra;
    }

    public static Posicao getPosicaoDaLetra(char letra) {
        Posicao pos = LETRA_PARA_POSICAO.get(letra);
        if (pos == null) {
            throw new IllegalArgumentException("Letra de casa inválida: " + letra);
        }
        return pos;
    }

    public char getOrigem() {
        return origem;
    }

    public char getDestino() {
        return destino;
    }

    public boolean isCaptura() {
        return captura;
    }

    public Jogada getProximaCaptura() {
        return proximaCaptura;
    }

    public void setProximaCaptura(Jogada proximaCaptura) {
        this.proximaCaptura = proximaCaptura;
    }


    /**
     * Retorna a jogada como string de 2 caracteres, ex: "DG".
     * Se for sequência de capturas, mostra encadeado: "DK->KR"
     */
    @Override
    public String toString() {
        String s = "" + origem + destino;
        if (proximaCaptura != null) {
            s += "->" + proximaCaptura.toString();
        }
        return s;
    }
}
