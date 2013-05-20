package ru.intertrust.cm.core.business.api.dto;

import java.math.BigDecimal;
import java.util.*;

/**
 * Бизнес-объект - основная именованная сущность системы. Включает в себя набор именованных полей со значениями
 * аналогично тому, как класс Java включает в себя именованные поля.
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 15:57
 */
public class BusinessObject {
    private Id id;
    private Map<String, Integer> fieldIndexes;
    private ArrayList<Value> fieldValues;
    private Date createdDate;
    private Date modifiedDate;

    /**
     * Создаёт бизнес-объект
     */
    public BusinessObject() {
        fieldIndexes = new HashMap<>();
        fieldValues = new ArrayList<>();
    }

    /**
     * Возвращает идентификатор бизнес-объекта
     * @return идентификатор бизнес-объекта
     */
    public Id getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор бизнес-объекта
     * @param id идентификатор бизнес-объекта
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * Добавляет именованное поле к бизнес-объекту, если его ещё не существует. Если такое поле уже есть в наличии, не
     * совершает никаких действий.
     * @param field именованное поле
     * @return индекс добавленного или существующего поля
     */
    public int addField(String field) {
        Integer fieldIndex = fieldIndexes.get(field);
        if (fieldIndex != null) {
            return fieldIndex;
        }

        fieldIndex = fieldValues.size();
        fieldValues.add(null);
        fieldIndexes.put(field, fieldIndex);
        return fieldIndex;
    }

    /**
     * Добавляет именованные поля к бизнес-объекту. Логика добавления каждого поля аналогична тому, как если бы каждое
     * из них добавлялось методом {@link BusinessObject#addField(String)}
     * @param fields именованные поля
     * @return индексы добавленных и/или существующих полей
     */
    public int[] addFields(List<String> fields) {
        if (fields == null || fields.size() == 0) {
            return new int[0];
        }

        return addFields(fields.toArray(new String[fields.size()]));
    }

    /**
     * Добавляет именованные поля к бизнес-объекту. Логика добавления каждого поля аналогична тому, как если бы каждое
     * из них добавлялось методом {@link BusinessObject#addField(String)}
     * @param fields именованные поля
     * @return индексы добавленных и/или существующих полей
     */
    public int[] addFields(String... fields) {
        if (fields == null || fields.length == 0) {
            return new int[0];
        }

        int[] fieldIndexes = new int[fields.length];
        for (int i = 0; i < fields.length; ++i) {
            fieldIndexes[i] = addField(fields[i]);
        }
        return fieldIndexes;
    }

    /**
     * Устанавливает значение поля по его индексу. Данный метод является рекомендуемым с точки зрения
     * производительности, особенно при работе с массивами бизнес-объектов
     * @param index индекс поля
     * @param value значение поля
     * @throws IndexOutOfBoundsException если поля с таким индексом не существует
     */
    public void setValue(int index, Value value) {
        fieldValues.set(index, value);
    }

    /**
     * Возвращает значение поля по его индексу. Данный метод является рекомендуемым с точки зрения производительности,
     * особенно при работе с массивами бизнес-объектов
     * @param index индекс поля
     * @return значение поля
     * @throws IndexOutOfBoundsException если поля с таким индексом не существует
     */
    public Value getValue(int index) {
        return fieldValues.get(index);
    }

    /**
     * Устанавливает значение поля по его названию. При работе с массивами бизнес-объектов рекомендуется использовать
     * индексированный доступ {@link BusinessObject#setValue(int, Value)}
     * @param field название поля
     * @param value значение поля
     * @throws NullPointerException если поле не существует
     */
    public void setValue(String field, Value value) {
        fieldValues.set(getFieldIndex(field), value);
    }

    /**
     * Возвращает значение поля по его названию. При работе с массивами бизнес-объектов рекомендуется использовать
     * индексированный доступ {@link BusinessObject#getValue(int)}
     * @param field название поля
     * @return значение поля
     * @throws NullPointerException если поле не существует
     */
    public Value getValue(String field) {
        return fieldValues.get(getFieldIndex(field));
    }

    /**
     * Возвращает индекс поля по его названию или null в случае его отсутствия
     * @param field название поля
     * @return индекс поля или null в случае его отсутствия
     */
    public Integer getFieldIndex(String field) {
        return fieldIndexes.get(field);
    }

    /**
     * Возвращает карту индексов полей бизнес-объекта. Ключами карты являются названия полей, значениями - индексы.
     * @return карту индексов полей бизнес-объекта
     */
    public Map<String, Integer> getFieldIndexes() {
        return Collections.unmodifiableMap(fieldIndexes);
    }

    /**
     * Возвращает поля бизнес-объекта.
     * @return поля бизнес-объекта
     */
    public Set<String> getFields() {
        return Collections.unmodifiableSet(fieldIndexes.keySet());
    }

    /**
     * Возвращает поля бизнес-объекта в их натуральном порядке (порядке, в котором они были добавлены)
     * @return поля бизнес-объекта в их натуральном порядке
     */
    public ArrayList<String> getFieldsInOrder() {
        // данный массив можно было поддерживать как поле класса, но в целях экономии памяти мы этого не делаем
        int fieldsQty = fieldValues.size();
        ArrayList<String> result = new ArrayList<>(fieldsQty);
        if (fieldsQty == 0) {
            return result;
        }

        for (int i = 0; i < fieldsQty; ++i) {
            result.add(null);
        }
        Set<String> fields = fieldIndexes.keySet();
        for (String field : fields) {
            result.set(fieldIndexes.get(field), field);
        }
        return result;
    }

    /**
     * Возвращает дату создания данного бизнес-объекта
     * @return дату создания данного бизнес-объекта
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Устанавливает дату создания данного бизнес-объекта
     * @param createdDate дата создания данного бизнес-объекта
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Возвращает дату модификации данного бизнес-объекта
     * @return дату модификации данного бизнес-объекта
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Устанавливает дату модификации данного бизнес-объекта
     * @param modifiedDate дата модификации данного бизнес-объекта
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String toString() {
        final String TABULATOR = "    ";
        ArrayList<String> fields = getFieldsInOrder();
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append("Id = ").append(id).append('\n');
        result.append("Fields: [").append('\n');
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            result.append(TABULATOR).append(field).append(" = ").append(getValue(i)).append('\n');
        }
        result.append(']').append('\n');
        result.append("Created Date = ").append(createdDate).append('\n');
        result.append("Modified Date = ").append(modifiedDate).append('\n');
        result.append('}');
        return result.toString();
    }

    public static void main(String[] args) {
        // todo: move to unit tests after
        BusinessObject bo = new BusinessObject();
        bo.addField("A");
        bo.addFields("B", "C");
        bo.setValue(1, new IntegerValue(2));
        bo.setValue("C", new DecimalValue(new BigDecimal(Math.PI)));
        System.out.println(bo);
        System.out.println(bo.getValue(1));
        System.out.println(bo.getValue("C"));
        //bo.getValue(3);
        //bo.setValue("O", new IntegerValue(2));
    }
}
