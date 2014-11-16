package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.user.client.ui.CheckBox;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.11.2014
 *         Time: 13:12
 */
public class CheckBoxInabilityManager {
    private List<CheckBox> checkBoxes = new ArrayList<CheckBox>();
    private Predicate<CheckBox> predicate;

    public CheckBoxInabilityManager() {
        initPredicate();
    }

    public void handleCheckBoxesInability() {
        List<CheckBox> selectedCheckBoxes = GuiUtil.filter(checkBoxes, predicate);
        if (selectedCheckBoxes.size() == 1) {
            selectedCheckBoxes.get(0).setEnabled(false);
        } else {
            enableAll();
        }
    }

    private void initPredicate() {
        predicate = new Predicate<CheckBox>() {
            @Override
            public boolean evaluate(CheckBox checkBox) {
                return checkBox.getValue();
            }
        };
    }

    public void addCheckBox(CheckBox checkBox) {
        checkBoxes.add(checkBox);
    }

    private void enableAll() {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setEnabled(true);
        }
    }

}
