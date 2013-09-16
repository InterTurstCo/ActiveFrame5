package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс определяет строку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 12:33
 */
@Root(name = "tr")
public class RowConfig implements Dto {
    @Attribute(name = "height", required = false)
    private String height;

    @Attribute(name = "v-align", required = false)
    private String defaultVerticalAlignment;

    @ElementList(inline = true)
    private List<CellConfig> cells = new ArrayList<CellConfig>();

    /**
     * Возвращает высоту строки
     * @return высоту строки
     */
    public String getHeight() {
        return height;
    }

    /**
     * Устанавливает высоту строки
     * @param height высота строки
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * Возвращает выравнивание по умолчанию в данной строке
     * @return выравнивание по умолчанию в данной строке
     */
    public String getDefaultVerticalAlignment() {
        return defaultVerticalAlignment;
    }

    /**
     * Устанавливает выравнивание по умолчанию в данной строке
     * @param defaultVerticalAlignment выравнивание по умолчанию в данной строке
     */
    public void setDefaultVerticalAlignment(String defaultVerticalAlignment) {
        this.defaultVerticalAlignment = defaultVerticalAlignment;
    }

    public List<CellConfig> getCells() {
        return cells;
    }

    public void setCells(List<CellConfig> cells) {
        this.cells = cells;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RowConfig rowConfig = (RowConfig) o;

        if (cells != null ? !cells.equals(rowConfig.cells) : rowConfig.cells != null) {
            return false;
        }
        if (defaultVerticalAlignment != null ? !defaultVerticalAlignment.equals(rowConfig.defaultVerticalAlignment) : rowConfig.defaultVerticalAlignment != null) {
            return false;
        }
        if (height != null ? !height.equals(rowConfig.height) : rowConfig.height != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = height != null ? height.hashCode() : 0;
        result = 31 * result + (defaultVerticalAlignment != null ? defaultVerticalAlignment.hashCode() : 0);
        result = result + (cells != null ? cells.hashCode() : 0);
        return result;
    }
}
