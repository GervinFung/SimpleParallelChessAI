package chess.engine.player;

import chess.engine.board.Board;

public final class MoveTransition {

    private final Board transitionBoard;
    private final MoveStatus moveStatus;

    public MoveTransition(final Board transitionBoard, final MoveStatus moveStatus) {
        this.transitionBoard = transitionBoard;
        this.moveStatus = moveStatus;

    }

    public MoveStatus getMoveStatus() {
        return this.moveStatus;
    }

    public Board getBoard() {
        return this.transitionBoard;
    }
}