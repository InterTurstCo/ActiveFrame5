package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.json.client.JSONParser;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.history.UserSettingsObject;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.ColumnSettingsObject;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 01.08.2014
 * 0:19
 */

public class UserSettingsUtil {
    public static ColumnSettingsObject getColumnSettingsObject(final UserSettingsObject userSettingsObject, final String key) {
        ColumnSettingsObject result = userSettingsObject.getAttr(key).cast();
        if (result == null) {
            result = ColumnSettingsObject.createObject();
            userSettingsObject.setAttr(key, result);
        }
        return result;
    }

    // FIXME merge
//    public static UserSettingsObject getUserSettingsObjectForColumns(String collectionIdentifier) {
//        final String columnSettingsAsString = Application.getInstance().getHistoryManager()
//                .getValue(collectionIdentifier, UserSettingsHelper.COLUMN_SETTINGS_KEY);
//        UserSettingsObject result = UserSettingsObject.createObject().cast();
//        if (columnSettingsAsString != null && !columnSettingsAsString.isEmpty()) {
//            try {
//                result = JSONParser.parseStrict(columnSettingsAsString).isObject().getJavaScriptObject().cast();
//            } catch (Exception ignored) {
//            }
//        }
//        return result;
//    }

}
