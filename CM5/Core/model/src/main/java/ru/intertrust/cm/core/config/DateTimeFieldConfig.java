package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:13 AM
 */
public class DateTimeFieldConfig extends FieldConfig {

    public DateTimeFieldConfig(){
    }

    public DateTimeFieldConfig( String name, boolean notNull, boolean immutable) {
        super(name, notNull, immutable);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.DATETIME;
    }

    @Override
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = super.getConstraints();
        HashMap<String, String> params = new HashMap<>();
        constraints.add(new Constraint(Constraint.Type.DATE, params));

        return constraints;
    }
}
