package chess.engine.board;

import java.util.ArrayList;
import java.util.List;

public final class MoveLog {

    private final List<Move> moves;

    public MoveLog() { this.moves = new ArrayList<>(); }

    public List<Move> getMoves() { return this.moves; }

    public void addMove(final Move move) { this.moves.add(move); }

    public int size() { return this.moves.size(); }

    public void clear() { this.moves.clear(); }

    public Move removeMove() { return this.moves.remove(this.moves.size() - 1); }
}