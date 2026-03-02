package main;

import java.util.HashMap;

/**
 * Representa uma jogada no jogo de damas.
 * Mapeamento:
 *   0 1 2 3 4 5
 * 0 . A . B . C
 * 1 D . E . F .
 * 2 . G . H . I
 * 3 J . K . L .
 * 4 . M . N . O
 * 5 P . Q . R .
 */
public class Jogada {

    // HashMap: letra -> {linha, coluna} (O(1) lookup)
    private static final HashMap<Character, int[]> letraParaPosicao = new HashMap<>();

    // HashMap: chave composta (linha * 6 + col) -> letra (O(1) lookup)
    private static final HashMap<Integer, Character> posicaoParaLetra = new HashMap<>();

    static {
        // Inicialização manual pra ficar claro o mapeamento
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
        letraParaPosicao.put(letra, new int[] { linha, col });
        posicaoParaLetra.put(linha * 6 + col, letra);
    }

    private final char origem;
    private final char destino;
    private final boolean captura;
    private Jogada proximaCaptura; // Encadeamento para sequência de capturas

    public Jogada(char origem, char destino) {
        this.origem = origem;
        this.destino = destino;
        this.captura = false;
        this.proximaCaptura = null;
    }

    public Jogada(char origem, char destino, boolean captura) {
        this.origem = origem;
        this.destino = destino;
        this.captura = captura;
        this.proximaCaptura = null;
    }

    /**
     * Cria uma Jogada a partir de coordenadas (linha, coluna).
     */
    public Jogada(int linhaOrigem, int colOrigem, int linhaDestino, int colDestino, boolean captura) {
        this.origem = posicaoParaLetra(linhaOrigem, colOrigem);
        this.destino = posicaoParaLetra(linhaDestino, colDestino);
        this.captura = captura;
        this.proximaCaptura = null;
    }

    // --- Conversão entre letras e posições (via HashMap, O(1)) ---

    /**
     * Converte uma letra (A-R) para coordenadas {linha, coluna}.
     */
    public static int[] letraParaPosicao(char letra) {
        int[] pos = letraParaPosicao.get(letra);
        if (pos == null) {
            throw new IllegalArgumentException("Letra inválida: " + letra);
        }
        return pos;
    }

    /**
     * Converte coordenadas (linha, coluna) para a letra correspondente (A-R).
     */
    public static char posicaoParaLetra(int linha, int col) {
        Character letra = posicaoParaLetra.get(linha * 6 + col);
        if (letra == null) {
            throw new IllegalArgumentException("Posição não jogável: (" + linha + ", " + col + ")");
        }
        return letra;
    }

    // --- Getters ---

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

    public int[] getOrigemPosicao() {
        return letraParaPosicao(origem);
    }

    public int[] getDestinoPosicao() {
        return letraParaPosicao(destino);
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
