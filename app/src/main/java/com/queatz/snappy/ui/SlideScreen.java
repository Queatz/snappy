package com.queatz.snappy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jacob on 10/19/14.
 */
public class SlideScreen extends View implements ActionBar.OnPageChangeListener {
    public interface ScreenAdapter {
        public int getCount();
        public View getView(int page, View convertView, ViewGroup parent);
    }

    private ActionBar mActionBar;
    private View mScreen;

    public SlideScreen(Context context) {
        super(context);
    }

    public SlideScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onPageChange(int page) {
        // Smooth slide, call mActionBar.setSlide() constantly, same for dragging
    }
}
