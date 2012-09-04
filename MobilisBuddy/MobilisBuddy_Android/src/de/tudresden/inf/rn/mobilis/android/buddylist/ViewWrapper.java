package de.tudresden.inf.rn.mobilis.android.buddylist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableRow;

/**
 * Wraps the checkable list row.
 * 
 * Original sources from:
 * http://androidguys.com/?p=644
 * 
 * @author Dirk
 */
class ViewWrapper {
    
    ViewGroup base;
    View guts=null;
    CheckBox cb=null;

    ViewWrapper(ViewGroup base) {
        this.base=base;
    }

    CheckBox getCheckBox() {
        if (cb==null) {
            cb=(CheckBox)((TableRow)base.getChildAt(0)).getChildAt(1);
        }

        return(cb);
    }

    void setCheckBox(CheckBox cb) {
        this.cb=cb;
    }

    View getGuts() {
        if (guts==null) {
            guts=((TableRow)base.getChildAt(0)).getChildAt(0);
        }

        return(guts);
    }

    void setGuts(View guts) {
        this.guts=guts;
    }
}
