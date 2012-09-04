package de.tud.android.locpairs;

import java.util.List;

import de.tud.android.locpairs.model.Instance;
import de.tud.android.locpairs.model.Player;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

/**
 * The Class GameListAdapter.
 */
public class GameListAdapter extends BaseAdapter {

	/** The m_context. */
	private Context m_context;

	/** The m_game instance list. */
	private List<Instance> m_gameInstanceList;
	
	/** The m_n selected position. */
	private int m_nSelectedPosition;

	/**
	 * Instantiates a new game list adapter.
	 * 
	 * @param context
	 *            the context
	 * @param gameInstanceList
	 *            the game instance list
	 */
	public GameListAdapter(Context context, List<Instance> gameInstanceList) {
		m_context = context;
		m_gameInstanceList = gameInstanceList;
		m_nSelectedPosition = Adapter.NO_SELECTION;
	}

	/**
	 * Gets the selected position.
	 * 
	 * @return the selected position
	 */
	public int getSelectedPosition() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return m_gameInstanceList.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int arg0) {
		return m_gameInstanceList.get(arg0);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		if (arg1 == null) {
			GameInstanceView temp = new GameInstanceView(m_context, m_gameInstanceList.get(arg0));
			arg1 = temp;
			return arg1;
		} else {
			GameInstanceView temp = (GameInstanceView) arg1;
			temp.setGameInstance(m_gameInstanceList.get(arg0));
			arg1 = temp;
			return arg1;
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

}
