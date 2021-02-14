package chess.gui;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.MoveLog;


import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.List;

public final class GameHistoryPanel extends JPanel {

    private final DataModel model;
    private final JScrollPane scrollPane;

    public GameHistoryPanel() {
        this.setLayout(new BorderLayout());
        this.model = new DataModel();
        final JTable table = new JTable(model);
        table.setRowHeight(15);
        this.scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setPreferredSize(new Dimension(100, 400));
        this.add(scrollPane);
        this.setVisible(true);
    }

    protected void redo(final Board board, final MoveLog moveHistory) {

        int currentRow = 0;
        this.model.clear();
        for (final Move move : moveHistory.getMoves()) {

            final String moveText = move.toString();
            if (move.getMovedPiece().getLeague().isWhite()) {
                this.model.setValueAt(moveText, currentRow, 0);
            } else if (move.getMovedPiece().getLeague().isBlack()) {
                this.model.setValueAt(moveText, currentRow, 1);
                currentRow++;
            }
        }
        if (moveHistory.size() > 0) {
            final Move lastMove = moveHistory.getMoves().get(moveHistory.size() - 1);
            final String moveText = lastMove.toString();

            if (lastMove.getMovedPiece().getLeague().isWhite()) {
                this.model.setValueAt(moveText + calculateCheckAndCheckmateHash(board), currentRow, 0);
            } else if (lastMove.getMovedPiece().getLeague().isBlack()) {
                this.model.setValueAt(moveText + calculateCheckAndCheckmateHash(board), currentRow - 1, 1);
            }
        }
        final JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setValue(verticalBar.getMaximum());
    }

    private String calculateCheckAndCheckmateHash(final Board board) {
        if (board.currentPlayer().isInCheckmate()) {
            return "#";
        } else if (board.currentPlayer().isInCheck()) {
            return "+";
        }
        return "";
    }

    private static final class DataModel extends DefaultTableModel {
        private final List<Row> values;
        private final static String [] NAMES = {"White", "Black"};

        private DataModel () {
            this.values = new ArrayList<>();
        }

        public void clear() {
            this.values.clear();
        }

        @Override
        public int getRowCount() {
            if (this.values == null) {
                return 0;
            }
            return this.values.size();
        }

        @Override
        public int getColumnCount() {
            return NAMES.length;
        }

        @Override
        public Object getValueAt(final int row, final int column) {
            final Row currentRow = this.values.get(row);
            if (column == 0) {
                return currentRow.getWhiteMove();
            } else if (column == 1) {
                return currentRow.getBlackMove();
            }
            return null;
        }

        @Override
        public void setValueAt(final Object aValue, final int row, final int column) {

            final Row currentRow;
            if (this.values.size() <= row) {
                currentRow = new Row();
                this.values.add(currentRow);
            } else {
                currentRow = this.values.get(row);
            }
            if (column == 0) {
                currentRow.setWhiteMove((String)aValue);
                fireTableRowsInserted(row, column);
            } else if (column == 1) {
                currentRow.setBlackMove((String)aValue);
                fireTableCellUpdated(row, column);
            }
        }

        @Override
        public Class<?> getColumnClass(final int column) {
            return Move.class;
        }

        @Override
        public String getColumnName(final int column) {
            return NAMES[column];
        }
    }

    private static final class Row {

        private Row() {}

        private String whiteMove, blackMove;

        public void setWhiteMove(final String whiteMove) {
            this.whiteMove = whiteMove;
        }

        public void setBlackMove(final String blackMove) {
            this.blackMove = blackMove;
        }

        public String getWhiteMove() {
            return this.whiteMove;
        }

        public String getBlackMove() {
            return this.blackMove;
        }
    }
}