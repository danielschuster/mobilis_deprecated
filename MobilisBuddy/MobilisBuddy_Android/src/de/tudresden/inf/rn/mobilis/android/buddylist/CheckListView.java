package de.tudresden.inf.rn.mobilis.android.buddylist;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Abstracted ListView with checkboxes.
 * 
 * Original sources from:
 * http://androidguys.com/?p=644
 * 
 * @author Dirk
 */
public class CheckListView extends ListView {
    public CheckListView(Context context) {
        super(context);
    }

    public CheckListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(new CheckableWrapper(getContext(), adapter));
    }
    
    public <T> void setAdapter(ListAdapter adapter, List<T> marked) {
        super.setAdapter(new CheckableWrapper(getContext(), adapter, marked));
    }

    public List<Integer> getCheckedPositions() {
        return(((CheckableWrapper)getAdapter()).getCheckedPositions());
    }

    public List<Object> getCheckedObjects() {
        return(((CheckableWrapper)getAdapter()).getCheckedObjects());
    }
}
