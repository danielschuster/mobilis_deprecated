/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.media.activities;

import android.content.res.Resources;
import de.tudresden.inf.rn.mobilis.media.R;

public class ConcreteRepositoryActivity extends RepositoryActivity {

	@Override
	protected void registerSubActivities() {
		final Resources r = this.getResources();
		this.registerSubActivity(new RepositoryActivity.SubActivity(
				RepositoryMapActivity.class,
				R.drawable.tab_repository_map_light,
				R.drawable.tab_repository_map_dark,
				r.getString(R.string.repository_map_caption)
			));
		this.registerSubActivity(new RepositoryActivity.SubActivity(
				RepositoryListActivity.class,
				R.drawable.tab_repository_list_light,
				R.drawable.tab_repository_list_dark,
				r.getString(R.string.repository_list_caption)
			));
	}
	
}
