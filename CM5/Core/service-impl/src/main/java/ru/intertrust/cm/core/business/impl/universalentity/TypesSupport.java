package ru.intertrust.cm.core.business.impl.universalentity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimeOnly;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.util.Args;

abstract class TypesSupport {

    private static final Map<Class<?>, Ts> tsAll = new HashMap<Class<?>, Ts>() {

        {
            this.put(String.class, new TsString());
            this.put(Boolean.class, new TsBoolean());
            this.put(Integer.class, new TsInteger());
            this.put(Long.class, new TsLong());
            this.put(BigDecimal.class, new TsBigDecimal());
            this.put(Id.class, new TsId());
            this.put(Date.class, new TsDate());
            this.put(TimelessDate.class, new TsTimelessDate());
            this.put(TimeOnly.class, new TsTimeOnly());
            this.put(DateTimeWithTimeZone.class, new TsDateTimeWithTimeZone());
        }

    };

    static <T> T fromRdbmsValue (final Value<?> rdbmsValue, final @Nonnull Class<T> clazz) {

        Object v = (rdbmsValue == null) ? null : rdbmsValue.get();

        if (v == null) {
            return null;
        }

        if (Args.notNull(clazz, "clazz") == TimeOnly.class) {
            v = TsTimeOnly.strToTimeOnly((String)v);
        } else if (clazz == Integer.class) {
            v = ((Long)v).intValue();
        } else if (clazz.isEnum()) {
            return ConfigurationSupport.getEnumProcessor(clazz).fromLongValue((Long)v);
        }

        return clazz.cast(v);

    }

    static <T> Value<?> fromJavaValue (final T value, final @Nonnull Class<T> clazz) {
        if (value == null) {
            return null;
        } else {
            return get(Args.notNull(clazz, "clazz")).toRdbmsValue(clazz.isEnum() ? ConfigurationSupport.getEnumProcessor(clazz).fromEnumValue(value) : value);
        }
    }

    @Nonnull
    private static Ts get (final @Nonnull Class<?> clazz) {

        final Ts ts = tsAll.get(Args.notNull(clazz, "clazz"));

        if (ts == null) {
            throw new RuntimeException("unsupported value-type '" + clazz + "'");
        }

        return ts;

    }

    private TypesSupport () {
    }

    private interface Ts {

        @Nonnull
        abstract Value<?> toRdbmsValue (Object value);

    }

    private static class TsTimeOnly implements Ts {

        static TimeOnly strToTimeOnly (final String dbValue) {

            if (dbValue == null) {
                return null;
            } else if (dbValue.length() != 6) {
                throw new IllegalArgumentException("dbValue must has 6 symbols length");
            }

            return new TimeOnly(Integer.parseInt(dbValue.substring(0, 2)), Integer.parseInt(dbValue.substring(2, 4)), Integer.parseInt(dbValue.substring(4)));

        }

        @Override
        public StringValue toRdbmsValue (final Object v) {
            final TimeOnly timeOnly = (TimeOnly)v;
            return new StringValue(String.format("%02d%02d%02d", timeOnly.getHours(), timeOnly.getMinutes(), timeOnly.getSeconds()));
        }

    }

    private static class TsString implements Ts {

        @Override
        public StringValue toRdbmsValue (final Object v) {
            return new StringValue((String)v);
        }

    }

    private static class TsInteger implements Ts {

        @Override
        public LongValue toRdbmsValue (final Object v) {
            return new LongValue((Integer)v);
        }

    }

    private static class TsLong implements Ts {

        @Override
        public LongValue toRdbmsValue (final Object v) {
            return new LongValue((Long)v);
        }

    }

    private static class TsId implements Ts {

        @Override
        public ReferenceValue toRdbmsValue (final Object v) {
            return new ReferenceValue((Id)v);
        }

    }

    private static class TsBoolean implements Ts {

        @Override
        public BooleanValue toRdbmsValue (final Object v) {
            return new BooleanValue((Boolean)v);
        }

    }

    private static class TsDate implements Ts {

        @Override
        public DateTimeValue toRdbmsValue (final Object v) {
            return new DateTimeValue((Date)v);
        }

    }

    private static class TsTimelessDate implements Ts {

        @Override
        public TimelessDateValue toRdbmsValue (final Object v) {
            return new TimelessDateValue((TimelessDate)v);
        }

    }

    private static class TsDateTimeWithTimeZone implements Ts {

        @Override
        public DateTimeWithTimeZoneValue toRdbmsValue (final Object v) {
            return new DateTimeWithTimeZoneValue((DateTimeWithTimeZone)v);
        }

    }

    private static class TsBigDecimal implements Ts {

        @Override
        public DecimalValue toRdbmsValue (final Object v) {
            return new DecimalValue((BigDecimal)v);
        }

    }

}