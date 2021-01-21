package chess.engine.player.ArtificialIntelligence;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.MoveTransition;
import chess.engine.player.Player;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.util.Collection;
import java.util.Comparator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

import static chess.engine.board.BoardUtils.mostValuableVictimLeastValuableAggressor;

public class MiniMax {

    private final StandardBoardEvaluation evaluator;
    private final int searchDepth, nThreads;
    private int quiescenceCount, moveCount;
    private static final int MAX_QUIESCENCE = 5000 * 5;

    private enum MoveSorter {

        MOVE_SORTER {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(BoardUtils.kingThreat(move1), BoardUtils.kingThreat(move2))
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(mostValuableVictimLeastValuableAggressor(move2), mostValuableVictimLeastValuableAggressor(move1))
                        .result()).immutableSortedCopy(moves);
            }
        };

        abstract Collection<Move> sort(Collection<Move> moves);
    }


    public MiniMax(final int searchDepth) {
        this.evaluator = new StandardBoardEvaluation();
        this.nThreads = Runtime.getRuntime().availableProcessors();
        if (this.nThreads > 4) {
            this.searchDepth = searchDepth + 1;
        } else {
            this.searchDepth = searchDepth;
        }
        this.moveCount = 0;
        this.quiescenceCount = 0;
    }

    public Move execute(final Board board) {
        final Player currentPlayer = board.currentPlayer();

        final AtomicInteger highestSeenValue = new AtomicInteger(Integer.MIN_VALUE);
        final AtomicInteger lowestSeenValue = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicInteger currentValue = new AtomicInteger(0);

        final AtomicBoolean isCheckMate = new AtomicBoolean(false);

        final AtomicReference<Move> bestMove = new AtomicReference<>();

        final ExecutorService executorService = Executors.newFixedThreadPool(this.nThreads);

        for (final Move move : MoveSorter.MOVE_SORTER.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            this.quiescenceCount = 0;

            if (isCheckMate.get()) {
                break;
            }
            executorService.execute(() -> {
                if (moveTransition.getMoveStatus().isDone()) {
                    final int currentVal = currentPlayer.getLeague().isWhite() ?
                            min(moveTransition.getLatestBoard(), this.searchDepth - 1, highestSeenValue.get(), lowestSeenValue.get()) :
                            max(moveTransition.getLatestBoard(), this.searchDepth - 1, highestSeenValue.get(), lowestSeenValue.get());

                    currentValue.set(currentVal);

                    if (currentPlayer.getLeague().isWhite() && currentValue.get() > highestSeenValue.get()) {
                        highestSeenValue.set(currentValue.get());
                        bestMove.set(move);
                        if(moveTransition.getLatestBoard().blackPlayer().isInCheckmate()) {
                            isCheckMate.set(true);
                        }
                    }
                    else if (currentPlayer.getLeague().isBlack() && currentValue.get() < lowestSeenValue.get()) {
                        lowestSeenValue.set(currentValue.get());
                        bestMove.set(move);
                        if(moveTransition.getLatestBoard().whitePlayer().isInCheckmate()) {
                            isCheckMate.set(true);
                        }
                    }
                    this.moveCount++;
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        return bestMove.get();
    }

    public int getMoveCount() { return this.moveCount; }

    private int max(final Board board,
                    final int depth,
                    final int highest,
                    final int lowest) {
        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            return this.evaluator.evaluate(board, depth);
        }
        int currentHighest = highest;
        for (final Move move : MoveSorter.MOVE_SORTER.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final Board toBoard = moveTransition.getLatestBoard();
                currentHighest = Math.max(currentHighest, min(toBoard,
                        calculateQuiescenceDepth(toBoard, depth), currentHighest, lowest));
                if (currentHighest >= lowest) {
                    return lowest;
                }
            }
        }
        return currentHighest;
    }

    private int min(final Board board,
                    final int depth,
                    final int highest,
                    final int lowest) {
        if (depth == 0 || BoardUtils.isEndGameScenario(board)) {
            return this.evaluator.evaluate(board, depth);
        }
        int currentLowest = lowest;
        for (final Move move : MoveSorter.MOVE_SORTER.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final Board toBoard = moveTransition.getLatestBoard();
                currentLowest = Math.min(currentLowest, max(toBoard,
                        calculateQuiescenceDepth(toBoard, depth), highest, currentLowest));
                if (currentLowest <= highest) {
                    return highest;
                }
            }
        }
        return currentLowest;
    }

    private int calculateQuiescenceDepth(final Board toBoard,
                                         final int depth) {
        if(depth == 1 && this.quiescenceCount < MAX_QUIESCENCE) {
            int activityMeasure = 0;
            if (toBoard.currentPlayer().isInCheck()) {
                activityMeasure += 1;
            }
            for(final Move move: BoardUtils.lastNMoves(toBoard, 2)) {
                if(move.isAttack()) {
                    activityMeasure += 1;
                }
            }
            if(activityMeasure >= 2) {
                this.quiescenceCount++;
                return 2;
            }
        }
        return depth - 1;
    }
}