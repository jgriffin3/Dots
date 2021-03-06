package edu.harding.dots;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fmccown on 9/5/2017.
 *
 * Some of the game logic was derived from https://github.com/SteinarArnason/Dots/
 */

public class DotsGame {

    public static int NUM_COLORS = 5;
    public static int NUM_CELLS = 6;
    public static int INIT_MOVES = 15;
    public static int INIT_TIME = 30;


    public enum AddDotStatus { Added, Rejected, Removed, CompleteCycle };

    public enum GameTypes { Timed, Moves };
    private GameTypes mGameType;
    private boolean mGameStatus;

    private int mScore;
    public int mMoves;
    public int mTimer;

    private int mNumCells = NUM_CELLS;
    private Dot[][] mDots;

    private int mDotColors[];

    private ArrayList<Dot> mDotPath;

    public DotsGame(String gameType) {
        if ("Timed".equals(gameType))
        {
            mGameType = GameTypes.Timed;
            mTimer = INIT_TIME;
        }
        else if ("Moves".equals(gameType))
        {
            mGameType = GameTypes.Moves;
            mMoves = INIT_MOVES;
        }
        mScore = 0;

        mDotColors = new int[NUM_COLORS];
        for (int i = 0; i < NUM_COLORS; i++) {
            mDotColors[i] = i;
        }

        mDots = new Dot[mNumCells][mNumCells];

        for (int row = 0; row < mNumCells; row++) {
            for (int col = 0; col < mNumCells; col++) {
                mDots[row][col] = new Dot(row, col);
            }
        }

        mGameStatus = false;
        mDotPath = new ArrayList();
    }

    public void newGame() {
        mScore = 0;
        mGameStatus = false;

        for (int row = 0; row < mNumCells; row++) {
            for (int col = 0; col < mNumCells; col++) {
                mDots[row][col].changeColor();
            }
        }
    }

    public String getGameType() {
        if (mGameType == GameTypes.Moves)
        {
            return "Moves";
        }
        if (mGameType == GameTypes.Timed)
        {
            return "Timed";
        }
        return "None";
    }

    public String getMoves(){
        return "" + mMoves;
    }

    public String getTime(){
        return "" + mTimer;
    }

    public String getScore() {
        return mScore + "";
    }

    public void setScore(String scoreAmount) {
        mScore = Integer.parseInt(scoreAmount);
    }

    public Dot getDot(int row, int col) {
        return mDots[row][col];
    }

    public void timerTick()
    {
        mTimer--;
    }

    public ArrayList<Dot> getDotPath() {
        return mDotPath;
    }

    // Sort by rows
    private ArrayList<Dot> getSortedDotPath() {
        Collections.sort(mDotPath, new Comparator<Dot>() {
            public int compare(Dot dot1, Dot dot2) {
                return Integer.compare(dot1.row, dot2.row);
            }
        });

        return mDotPath;
    }

    public void clearDotPath() {
        for (Dot dot: mDotPath) {
            dot.selected = false;
        }
        mDotPath.clear();
    }

    public AddDotStatus addDotToPath(Dot dot) {
        if (mMoves <= 0 && mTimer <= 0)
        {
            // game is over, save score to highscores
            gameOver();
            return AddDotStatus.Rejected;
        }
        else if (!isGameOver()){
            if (mDotPath.size() == 0) {
                mDotPath.add(dot);
                dot.selected = true;
                return AddDotStatus.Added;
            }
            else {
                Dot lastDot = mDotPath.get(mDotPath.size() - 1);

                if (!mDotPath.contains(dot)) {
                    // New dot encountered
                    if (lastDot.color == dot.color && lastDot.isAdjacent(dot)) {
                        mDotPath.add(dot);
                        dot.selected = true;
                        return AddDotStatus.Added;
                    }
                }
                else if (mDotPath.size() > 1) {
                    // Backtracking or cycle
                    Dot secondLast = mDotPath.get(mDotPath.size() - 2);

                    // Backtracking
                    if (secondLast.equals(dot)) {
                        Dot removedDot = mDotPath.remove(mDotPath.size() - 1);
                        removedDot.selected = false;
                        return AddDotStatus.Removed;
                    }
                    else if (!lastDot.equals(dot) && lastDot.isAdjacent(dot)) {
                        // Made cycle, so add all dots of same color to path
                        mDotPath.clear();
                        for (int row = 0; row < mNumCells; row++) {
                            for (int col = 0; col < mNumCells; col++) {
                                Dot currentDot = getDot(row, col);
                                if (currentDot.color == dot.color) {
                                    dot.selected = true;
                                    mDotPath.add(currentDot);
                                }
                            }
                        }

                        return AddDotStatus.CompleteCycle;
                    }
                }
            }
        }

        return AddDotStatus.Rejected;
    }

    public void gameOver() {
        // save mScore to something
        mGameStatus = true;
    }
    public boolean isGameOver(){
        return mGameStatus;
    }

    public void checkGameOver(){
        if (mGameType == GameTypes.Moves && mMoves == 0)
        {
            gameOver();
        }
    }

    public void finishMove() {
        if (mDotPath.size() > 1) {
            // Move all dots above each dot in the path down
            for (Dot dot : getSortedDotPath()) {
                dot.selected = false;
                // Put new dots in place
                for (int row = dot.row; row > 0; row--) {
                    Dot dotToMove = getDot(row, dot.col);
                    Dot dotAbove = getDot(row - 1, dot.col);
                    dotToMove.color = dotAbove.color;
                }
                Dot topDot = getDot(0, dot.col);
                topDot.changeColor();
            }

            if (!isGameOver()) {
                mScore += mDotPath.size();
                if (mGameType == GameTypes.Moves)
                {
                    mMoves--;
                }
            }
        }
    }

    public ArrayList<Integer> getBoardState() {
        // return a list that has colors for each dot
        ArrayList<Integer> boardState = new ArrayList<>();
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                boardState.add(mDots[row][col].color);
            }
        }
        Log.d("test", "boardState = " + boardState);

        return boardState;
    }

    public void restoreState(ArrayList<Integer> boardState) {
        // Put the color values from boardState into mDots for each dot
        int index = 0;
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                mDots[row][col].color = boardState.get(index);
                index++;
            }
        }
    }
}
