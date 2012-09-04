package de.tudresden.inf.rn.mobilis.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.android.services.InfoViewer;

/**
 * Horizontal status bar widget, which is able to simply show an information, warning or progress.
 * @author Dirk
 */
public class InfoBar extends LinearLayout implements InfoViewer, Runnable {
   
    private TextView textView;
    private FrameLayout iconStack;
    private View currentIcon;
    private Handler mainThreadHandler;
    private int action;
    private String message;
    private static final int ACTION_SHOW_INFO = 1;
    private static final int ACTION_SHOW_PROGRESS = 2;
    private static final int ACTION_SHOW_WARNING = 3;
    
    /**
     * Constructs InfoBar, initializing with any attributes we understand from a
     * layout file.
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public InfoBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // retrieve the widget layout from infobar.xml
        View layout = inflate(getContext(), R.layout.infobar, null);
        this.addView(layout);
        
        textView = (TextView) findViewById(R.id.infobar_text);
        iconStack = (FrameLayout) findViewById(R.id.infobar_iconstack);
        
        // hide all other icons
        for (int i=0; i < iconStack.getChildCount(); i++) {
            View child = iconStack.getChildAt(i);
            child.setVisibility(View.INVISIBLE);
        }
        
        currentIcon = iconStack.findViewById(R.id.infobar_icon_info);
        currentIcon.setVisibility(View.VISIBLE);
        
        // retrieve attributes from individual used layout xml
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.InfoBar);

        CharSequence s = a.getString(R.styleable.InfoBar_text);
        if (s != null) {
            setText(s.toString());
        }

        // finished reading out all attributes
        a.recycle();
    }
    
    public void setMainThreadHandler(Handler handler) {
    	mainThreadHandler = handler;
    }

    @Override
    public void showInfo(String message) {
    	this.action = ACTION_SHOW_INFO;
    	this.message = message;
        mainThreadHandler.post(this);
    }

    @Override
    public void showProgress(String message) {
    	this.action = ACTION_SHOW_PROGRESS;
    	this.message = message;
        mainThreadHandler.post(this);
    }

    @Override
    public void showWarning(String message) {
    	this.action = ACTION_SHOW_WARNING;
    	this.message = message;
        mainThreadHandler.post(this);
    }
        
    /**
     * Shows only the given icon, specified by its ID.
     * @param iconId the ID of the icon to show
     */
    private void setIcon(int iconId){
        // hide the current icon
        currentIcon.setVisibility(View.INVISIBLE);
        // show the specified icon
        currentIcon = iconStack.findViewById(iconId);
        currentIcon.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the given text message in the InfoBar.
     * @param message the text message to show
     */
    private void setText(String message) {
        textView.setText(message + " ");
    }

    /**
     * Updates the GUI, gets posted to the main thread message queue
     */
	@Override
	public void run() {
    	switch(action) {
    	case ACTION_SHOW_INFO:
    		setIcon(R.id.infobar_icon_info);
            setText(message);
    		break;
    	case ACTION_SHOW_PROGRESS:
            setIcon(R.id.infobar_progress);
            setText(message);
    		break;
    	case ACTION_SHOW_WARNING:
            setIcon(R.id.infobar_icon_warning);
            setText(message);
    		break;
    	}
	}
}
