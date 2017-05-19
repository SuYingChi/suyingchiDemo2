package com.ihs.inputmethod.feature.boost.plus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class BoostTextView extends TextView implements Runnable {

    private static final int COUNT_DECELERATE = 60;
    private static final int DECELERATE_DEFAULT = 1;
    private long currentNumber;
    private long endNumber;
    private long timeInterval;
    private boolean isEnd;
    private boolean isRunning;
    private long decelerateNumber;
    private long intervalNumber = COUNT_DECELERATE;

    public BoostTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoostTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BoostTextView(Context context) {
        super(context);
    }
    
    public void startAnimation (long duration, long startNumber, long endNumber) {
        if (isRunning) {
            isRunning = false;
            removeCallbacks(this);
            startAnimation(duration, currentNumber, endNumber);
            return;
        }
        if (currentNumber == 0) {
            this.currentNumber = startNumber;
        }
        this.endNumber = endNumber;
        if (startNumber > endNumber) {
            this.isEnd = false;
            if ((startNumber - endNumber) < COUNT_DECELERATE) {
                intervalNumber = (startNumber - endNumber);
            }
        } else {
            this.isEnd = true;
        }

        this.timeInterval = duration / intervalNumber;
        decelerateNumber = (startNumber - endNumber) / intervalNumber;
        if (decelerateNumber == 0) {
            decelerateNumber = DECELERATE_DEFAULT;
        }
        run();
    }

    public void stopImmediately() {
        isEnd = true;
        isRunning = false;
        currentNumber = 0;
        removeCallbacks(this);
    }

    public void stopDecelerate() {
        removeCallbacks(this);
        startAnimation(500, currentNumber, 0);
    }

    @Override
    public void run() {
        isRunning = true;
        if (isEnd) {
            isRunning = false;
            currentNumber = 0;
            removeCallbacks(this);
            return;
        }

        currentNumber -= decelerateNumber;
        if (currentNumber < 0) {
            currentNumber = 0;
        }
        setText(String.valueOf(currentNumber));

        if (currentNumber <= endNumber) {
            isEnd = true;
        }

        postDelayed(this, timeInterval);
    }
}
