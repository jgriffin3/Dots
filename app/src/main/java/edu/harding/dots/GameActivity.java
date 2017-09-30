package edu.harding.dots;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.media.SoundPool;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import static android.graphics.Color.parseColor;

public class GameActivity extends AppCompatActivity {

    private DotsGame mGame;

    private TextView[] mGameTextViews;
    private TextView mGameTimerValue;
    private TextView mGameTimer;
    private TextView mScoreValue;

    private String mGameType;

    private String RED = "×";
    private String GREEN = "+";
    private String BLUE = "Δ";
    private String YELLOW = "–";
    private String PURPLE = "~";

    private Boolean mIsColorBlind = false;


    public SoundPool mSoundPool;
    private ArrayList<Integer> mSoundIds;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        
        mSoundPool = createSoundPool();
        mSoundIds = new ArrayList<>();
        mSoundIds.add(mSoundPool.load(this, R.raw.background, 1));
        mSoundIds.add(mSoundPool.load(this, R.raw.select, 1));
        mSoundIds.add(mSoundPool.load(this, R.raw.deselect, 1));
        
        this.findViewById(android.R.id.content).setBackgroundColor(parseColor(getIntent().getStringExtra("bgColor")));

        mIsColorBlind = getIntent().getBooleanExtra("isColorBlind", false);

        mGameTimerValue = (TextView) findViewById(R.id.timeValue);
        mGameTimer = (TextView) findViewById(R.id.time);
        mScoreValue = (TextView) findViewById(R.id.scoreValue);

        mGameTextViews = new TextView[(DotsGame.NUM_CELLS * DotsGame.NUM_CELLS)];

        GridLayout gridLayout = (GridLayout) findViewById(R.id.gameBoard);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            mGameTextViews[i] = (TextView) gridLayout.getChildAt(i);
        }
        gridLayout.setOnTouchListener(GridTouchListener);
        if ("Timed".equals(getIntent().getStringExtra("extraGameType")))
        {
            mGameType = "Timed";
            mGameTimerValue.setText(DotsGame.INIT_TIME + "");
            countdownTimer();
        }
        else if ("Moves".equals(getIntent().getStringExtra("extraGameType")))
        {
            mGameType = "Moves";
            mGameTimerValue.setText(DotsGame.INIT_MOVES + "");
            mGameTimer.setText(R.string.movesText);
        }

        mGame = new DotsGame(mGameType);
        mScoreValue.setText(mGame.getScore());
        drawBoard();
    }
    
        protected SoundPool createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return createNewSoundPool();
        } else {
            return createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected SoundPool createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        return new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    protected SoundPool createOldSoundPool(){
        return new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    }

    private void countdownTimer() {
        // code from https://developer.android.com/reference/android/os/CountDownTimer.html
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                mGameTimerValue.setText("" + millisUntilFinished / 1000);
                mGame.timerTick();
            }

            public void onFinish() {
                mGameTimerValue.setText("" + 0);
                mGame.gameOver();
            }
        }.start();
    }

    public void newGameClick(View view) {
        // May need more code?
        mGame = new DotsGame(mGameType);
        mScoreValue.setText(mGame.getScore());
        if (mGame.getGameType().equals("Moves"))
        {
            mGameTimerValue.setText(mGame.getMoves());
        }
        else if (mGame.getGameType().equals("Timed"))
        {
            mGameTimerValue.setText(mGame.getTime());
            countdownTimer();
        }
        drawBoard();
    }

    public void menuClick(View view) {
        super.onBackPressed();
    }

    public void drawBoard() {
        int i = 0;
        for (int row = 0; row < DotsGame.NUM_CELLS; row++) {
            for (int col = 0; col < DotsGame.NUM_CELLS; col++) {
                if (i < mGameTextViews.length)
                {
                    if (mGame.getDot(row, col).color == 0)
                    {
                        if (mIsColorBlind) {mGameTextViews[i].setText(RED);}
                        mGameTextViews[i].setBackground(ContextCompat.getDrawable(this, R.drawable.dot_red));
                    }
                    else if (mGame.getDot(row, col).color == 1)
                    {
                        if (mIsColorBlind) {mGameTextViews[i].setText(GREEN);}
                        mGameTextViews[i].setBackground(ContextCompat.getDrawable(this, R.drawable.dot_green));
                    }
                    else if (mGame.getDot(row, col).color == 2)
                    {
                        if (mIsColorBlind) {mGameTextViews[i].setText(BLUE);}
                        mGameTextViews[i].setBackground(ContextCompat.getDrawable(this, R.drawable.dot_blue));
                    }
                    else if (mGame.getDot(row, col).color == 3)
                    {
                        if (mIsColorBlind) {mGameTextViews[i].setText(YELLOW);}
                        mGameTextViews[i].setBackground(ContextCompat.getDrawable(this, R.drawable.dot_yellow));
                    }
                    else if (mGame.getDot(row, col).color == 4)
                    {
                        if (mIsColorBlind) {mGameTextViews[i].setText(PURPLE);}
                        mGameTextViews[i].setBackground(ContextCompat.getDrawable(this, R.drawable.dot_purple));
                    }
                    i++;
                }
            }
        }

    }

    private View.OnTouchListener GridTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            // Figure out which textView was touched
            int cellWidth = view.getWidth() / 6 ;
            int cellHeight = view.getHeight() / 6;
            int x = (int) event.getX();
            int y = (int) event.getY();
            int col = x / cellWidth;
            int row = y / cellHeight;
            int index = row * 6 + col;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // add code for updating view
                mGame.addDotToPath(mGame.getDot(row, col));
                mSoundPool.play(mSoundIds.get(1), 1, 1, 1, 0, 1);
                drawBoard();
                return true;
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                // add code for updating view
                mGame.addDotToPath(mGame.getDot(row, col));                
                mSoundPool.play(mSoundIds.get(1), 1, 1, 1, 0, 1);
                drawBoard();
                return true;
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                // add code for updating view
                mGame.addDotToPath(mGame.getDot(row, col));
                mGame.finishMove();
                mScoreValue.setText(mGame.getScore());
                if (mGame.getGameType().equals("Moves"))
                {
                    mGameTimerValue.setText(mGame.getMoves());
                }
                mGame.clearDotPath();
                mSoundPool.play(mSoundIds.get(2), 1, 1, 1, 0, 1);
                drawBoard();
                return true;
            }

            return false;
        }
    };
}
