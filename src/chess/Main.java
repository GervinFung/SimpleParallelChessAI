package chess;

import chess.gui.Table;

public final class Main {

    public static void main(final String[] args) {
        Table.getSingletonInstance().start();
    }
}