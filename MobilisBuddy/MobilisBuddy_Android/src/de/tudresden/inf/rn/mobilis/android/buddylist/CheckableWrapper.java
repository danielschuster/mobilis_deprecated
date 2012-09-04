package de.tudresden.inf.rn.mobilis.android.buddylist;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import de.tudresden.inf.rn.mobilis.android.services.BuddyListService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;

/**
 * Constructs a wrapping TableLayout around the original list row contents with a checkbox to the right.
 * Also keeps track of selected checkboxes. Can be initialized with a list of Elements to be checked.
 * 
 * Original sources from:
 * http://androidguys.com/?p=644
 * 
 * @author Dirk
 */
public class CheckableWrapper extends AdapterWrapper {

    Context ctxt=null;
    boolean[] states=null;
    ListAdapter adapter;
    private BuddyListService buddyListService;

    public CheckableWrapper(Context ctxt, ListAdapter delegate) {
        this(ctxt, delegate, null);
    }
    
    public <T> CheckableWrapper(Context ctxt, ListAdapter delegate, List<T> marked) {
        super(delegate);
        this.adapter = delegate;
        this.ctxt=ctxt;
        this.states=new boolean[delegate.getCount()];
        this.buddyListService = SessionService.getInstance().
            getSocialNetworkManagementService().getBuddyListService();
        
        // initialize the checkboxes as checked, which are supplied by the marked-list
        ArrayList<T> markedList;
        if (marked == null) {
            markedList = new ArrayList<T>();
        }
        else {
            markedList = (ArrayList<T>) marked;
        }
        for (int i=0;i<delegate.getCount();i++) {
            if (markedList.contains(getItem(i))) {
                this.states[i] = true;
            }
            else {
                this.states[i] = false;
            }
        }
    }

    public List<Integer> getCheckedPositions() {
        List<Integer> result=new ArrayList<Integer>();

        for (int i=0;i<delegate.getCount();i++) {
            if (states[i]) {
                result.add(new Integer(i));
            }
        }

        return(result);
    }

    public List<Object> getCheckedObjects() {
        List<Object> result=new ArrayList<Object>();

        for (int i=0;i<delegate.getCount();i++) {
            if (states[i]) {
                result.add(delegate.getItem(i));
            }
        }

        return(result);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewWrapper wrap=null;
        View row=convertView;

        if (convertView==null) {
                        
            TableLayout layout = new TableLayout(ctxt);
            layout.setColumnStretchable(0, true);
            layout.setColumnShrinkable(0, true);
            
            TableRow tableRow = new TableRow(ctxt);           
            View guts=delegate.getView(position, null, parent);        
            CheckBox cb=new CheckBox(ctxt);
            
            cb.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            guts.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.FILL_PARENT));

            cb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View buttonView) {
                    CheckBox checkBox = (CheckBox) buttonView;
                    int listIndex = ((Integer)checkBox.getTag()).intValue();
                    boolean isChecked = checkBox.isChecked();
                    states[listIndex] = isChecked;
                    handleCheckBoxOnClick(checkBox, listIndex, isChecked);
                }
            });
            
            // ! order determines indexes to get in ViewWrapper getCheckBox(), getGuts() !
            tableRow.addView(guts);
            tableRow.addView(cb);
            layout.addView(tableRow);
            
            wrap=new ViewWrapper(layout);
            wrap.setGuts(guts);
            layout.setTag(wrap);

            cb.setTag(new Integer(position));
            cb.setChecked(states[position]);    // set marked if initialized that way

            row=layout;
        }
        else {
            wrap=(ViewWrapper)convertView.getTag();
            wrap.setGuts(delegate.getView(position, wrap.getGuts(), parent));
            wrap.getCheckBox().setTag(new Integer(position));
            wrap.getCheckBox().setChecked(states[position]);    // set marked if previously marked
        }

        return(row);
    }
    
    private void handleCheckBoxOnClick(CheckBox checkBox, int listIndex, boolean isChecked) {
        Object buddy = adapter.getItem(listIndex);
        if (isChecked) {
            buddyListService.addToRoster(buddy.toString());
        } else {
            buddyListService.deleteFromRoster(buddy.toString());
        }
    }
}
