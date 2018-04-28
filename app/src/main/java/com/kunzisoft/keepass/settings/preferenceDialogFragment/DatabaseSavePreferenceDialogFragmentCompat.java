/*
 * Copyright 2018 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.settings.preferenceDialogFragment;

import android.view.View;

import com.kunzisoft.keepass.R;
import com.kunzisoft.keepass.app.App;
import com.kunzisoft.keepass.database.Database;
import com.kunzisoft.keepass.database.edit.OnFinish;
import com.kunzisoft.keepass.database.edit.SaveDB;
import com.kunzisoft.keepass.tasks.ProgressTask;

public abstract class DatabaseSavePreferenceDialogFragmentCompat  extends InputPreferenceDialogFragmentCompat {

    protected Database database;

    private OnFinish afterSaveDatabase;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        database = App.getDB();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ( positiveResult ) {
            assert getContext() != null;

            if (database != null && afterSaveDatabase != null) {
                SaveDB save = new SaveDB(getContext(), database, afterSaveDatabase);
                ProgressTask pt = new ProgressTask(getContext(), save, R.string.saving_database);
                pt.run();
            }
        }
    }

    public void setAfterSaveDatabase(OnFinish afterSaveDatabase) {
        this.afterSaveDatabase = afterSaveDatabase;
    }
}
