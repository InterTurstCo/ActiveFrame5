package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.view.client.SingleSelectionModel;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

public class CheckedSelectionModel<T extends CollectionRowItem> extends SingleSelectionModel<T> {

    private boolean dirtySensitivity = false;

    public CheckedSelectionModel() {
        this(false);
    }

    public CheckedSelectionModel(boolean dirtySensitivity) {
        this.dirtySensitivity = dirtySensitivity;
    }

    @Override
    public void setSelected(final T object, final boolean selected) {
        if (object != null) {
            if (dirtySensitivity) {
                Application.getInstance().getActionManager().checkChangesBeforeExecution(new ConfirmCallback() {
                    @Override
                    public void onAffirmative() {
                        CheckedSelectionModel.super.setSelected(object, selected);
                    }

                    @Override
                    public void onCancel() {
                    }
                });

            } else {
                super.setSelected(object, selected);
            }
        }
    }
}
