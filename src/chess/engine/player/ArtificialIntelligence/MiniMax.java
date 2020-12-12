package chess.engine.player.ArtificialIntelligence;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;

public final class MiniMax{

    private final StandardBoardEvaluation boardEvaluation;
    private final int searchDepth;
    private int moveCount;

    public MiniMax(final int searchDepth) {
        this.boardEvaluation = new StandardBoardEvaluation();
        this.searchDepth = searchDepth;
        this.moveCount = 0;
    }

    public Move execute(final Board board) {

        final long startTime = System.nanoTime();

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        for (final Move move : board.currentPlayer().getLegalMoves()) {

            this.moveCount++;

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {

                currentValue = board.currentPlayer().getLeague().isWhite() ? min(moveTransition.getBoard(), this.searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE) :
                        max(moveTransition.getBoard(), this.searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);

                if (board.currentPlayer().getLeague().isWhite() && currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;

                } else if (board.currentPlayer().getLeague().isBlack() && currentValue < lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;

                }
            }
        }
        final long executionTime = System.nanoTime() - startTime;
        System.out.println("Time taken to search best move: " + executionTime + " nanoseconds");
        return bestMove;
    }

    public int getMoveCount() {
        return this.moveCount;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.currentPlayer().isInCheckmate() ||
                board.currentPlayer().isInStalemate();
    }

    public int min(final Board board, final int depth, final int alpha, int beta) {

        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluation.evaluate(board, depth);
        }
        int lowestSeenValue = Integer.MAX_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);

            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getBoard(), depth - 1, alpha, beta);

                if (currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
                beta = Math.min(beta, currentValue);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return lowestSeenValue;
    }

    public int max(final Board board, final int depth, int alpha, final int beta) {

        if(depth == 0  || isEndGameScenario(board)) {
            return this.boardEvaluation.evaluate(board, depth);
        }
        int highestSeenValue = Integer.MIN_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {

            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);

            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getBoard(), depth - 1, alpha, beta);

                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
                alpha = Math.max(alpha, currentValue);
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return highestSeenValue;
    }
}