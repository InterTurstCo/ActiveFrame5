package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:22
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGuiFactory {

    public Widget buildGroup(HierarchyGroupConfig aGroupConfig){
        return new HierarchyGroupView(aGroupConfig);
    }

    public Widget buildCollection(HierarchyCollectionConfig aCollectionConfig){
        VerticalPanel lines = new VerticalPanel();

        CollectionRowItem aRow = new CollectionRowItem();
        aRow.setId(new RdbmsId(5067,1));
        aRow.setRow(new HashMap<String,Value>());
        aRow.getRow().put("ID",new LongValue(1));
        aRow.getRow().put("Name",new StringValue("Санкт-Петербург"));
        aRow.getRow().put("Population",new LongValue(3000000));

        lines.add(new HierarchyCollectionView(aCollectionConfig, aRow));
        return lines;
    }


}
