package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Sergey.Okolot
 *         Created on 22.09.2014 13:19.
 */
@Root(name = "simple-action")
public class SimpleActionConfig extends ActionConfig {
    
    public SimpleActionConfig(){
        super();
    }

    public SimpleActionConfig(String actionHandler){
        super();
        this.actionHandler = actionHandler;
    }

    @Attribute(name = "action-handler")
    private String actionHandler;

    @Attribute(name = "re-read-in-same-transaction", required = false)
    private boolean reReadInSameTransaction;

    @Override
    public String getComponentName() {
        if (super.getComponentName() != null && !super.getComponentName().isEmpty()){
            return super.getComponentName();
        }else {
            return "simple.action";
        }
    }

    public String getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(String actionHandler) {
        this.actionHandler = actionHandler;
    }
    
    public boolean reReadInSameTransaction() {
        return reReadInSameTransaction;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleActionConfig that = (SimpleActionConfig) o;

        if (reReadInSameTransaction != that.reReadInSameTransaction) return false;
        if (actionHandler != null ? !actionHandler.equals(that.actionHandler) : that.actionHandler != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = actionHandler != null ? actionHandler.hashCode() : 0;
        return result;
    }
}
