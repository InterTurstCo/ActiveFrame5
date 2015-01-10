package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import ru.intertrust.cm.core.gui.api.client.WidgetNavigator;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.01.2015
 *         Time: 9:04
 */
public class WidgetNavigatorImpl<T extends Widget> implements WidgetNavigator<T> {

    private T current;
    private T previous;
    private Class<T> widgetClass;
    private WidgetCollection widgets;

    public WidgetNavigatorImpl(WidgetCollection widgets, Class<T> widgetClass) {
        this.widgetClass = widgetClass;
        this.widgets = widgets;

    }

    @Override
    public void forward() {
        int indexOfPreviousItem = widgets.indexOf(current);
        int index = indexOfPreviousItem == -1 ? 0 : indexOfPreviousItem + 1;
          for(;index < widgets.size(); index ++){
              Widget w = widgets.get(index);
              if(handleWidget(w)){
                  return;
              }
          }
       navigateOutOfRange();

    }

    @Override
    public void back() {
        int indexOfPreviousItem = widgets.indexOf(current);
        int index = indexOfPreviousItem == -1 ? widgets.size() - 1 : indexOfPreviousItem - 1;
        for(;index >= 0; index --){
                Widget w = widgets.get(index);
                if(handleWidget(w)){
                    return;
                }
            }
        navigateOutOfRange();
    }

    @Override
    public T getCurrent() {
        return current;
    }

    @Override
    public T getPrevious() {
        return previous;
    }

    public void reset(){
        current = null;
        previous = null;
    }

    private boolean handleWidget(Widget w){
        if(widgetClass.getCanonicalName().equalsIgnoreCase(w.getClass().getCanonicalName())){
            previous = current;
            current = (T) w;
            return true;
        }
        return false;
    }

    private void navigateOutOfRange(){
        previous = current;
        current = null;
    }
}
