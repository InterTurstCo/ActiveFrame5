package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by andrey on 16.01.14.
 */
class RowItemMapper {

    private List<SummaryTableColumnConfig> summaryTableColumnConfigs;
    private DataExtractor dataExtractor;

    public RowItemMapper(DomainObject domainObject, List<SummaryTableColumnConfig> summaryTableColumnConfigs) {
        dataExtractor = new DomainObjectDataExtractor(domainObject);
        this.summaryTableColumnConfigs = summaryTableColumnConfigs;
    }

    public RowItemMapper(FormState formState, FormConfig formConfig,
                         List<SummaryTableColumnConfig> summaryTableColumnConfigs) {
        dataExtractor = new FormStateDataExtractor(formState, formConfig);
        this.summaryTableColumnConfigs = summaryTableColumnConfigs;
    }

    public RowItem map() {
        RowItem rowItem = new RowItem();
        for (SummaryTableColumnConfig columnConfig : summaryTableColumnConfigs) {
            PatternConfig patternConfig = columnConfig.getPatternConfig();
            String columnPattern = patternConfig.getValue();

            List<String> fieldsInPattern = takeFieldNamesFromPattern(columnPattern);
            FormattingConfig formattingConfig = columnConfig.getFormattingConfig();

            Map<String, String> formattedFields = new HashMap<>();

            if (formattingConfig != null) {
                NumberFormatConfig numberFormatConfig = formattingConfig.getNumberFormatConfig();
                if (numberFormatConfig != null) {
                    formatFields(new NumberFormatter(dataExtractor), formattedFields, numberFormatConfig.getPattern(), numberFormatConfig.getFieldsPathConfig());
                }
                DateFormatConfig dateFormatConfig = formattingConfig.getDateFormatConfig();
                if (dateFormatConfig != null) {
                    formatFields(new DateFormatter(dataExtractor), formattedFields, dateFormatConfig.getPattern(), dateFormatConfig.getFieldsPathConfig());
                }
            }
            for (String fieldName : fieldsInPattern) {
                if (!formattedFields.containsKey(fieldName)) {
                    Value value = dataExtractor.getValue(fieldName);
                    if (value != null) {
                        formattedFields.put(fieldName, value.toString());
                    }
                }
            }
            String columnValue = applyColumnPattern(columnPattern, formattedFields);
            rowItem.setValueByKey(columnConfig.getWidgetId(), columnValue);
        }
        return rowItem;
    }

    private List<String> takeFieldNamesFromPattern(String columnPattern) {
        Matcher matcher = fieldPatternMatcher(columnPattern);
        List<String> fieldNames = new ArrayList<>();
        while (matcher.find()) {
            String fieldName = takeFieldNameFromMatchedSequence(matcher);
            fieldNames.add(fieldName);
        }
        return fieldNames;
    }

    private void formatFields(FieldFormatter fieldFormatter, Map<String, String> formattedFields, String formatPattern, FieldPathsConfig fieldsPathConfig) {
        for (FieldPathConfig fieldPathConfig : fieldsPathConfig
                .getFieldPathConfigsList()) {
            String fieldName = fieldPathConfig.getValue();
            if (formatPattern != null) {
                formattedFields.put(fieldName, fieldFormatter.format(fieldName, formatPattern));
            }
        }
    }

    private String applyColumnPattern(String columnPattern, Map<String, String> formattedFields) {
        Matcher matcher = fieldPatternMatcher(columnPattern);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fieldName = takeFieldNameFromMatchedSequence(matcher);
            String replacement = formattedFields.get(fieldName);
            if (replacement != null) {
                matcher.appendReplacement(result, replacement);
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String takeFieldNameFromMatchedSequence(Matcher matcher) {
        String group = matcher.group();
        return group.substring(1, group.length() - 1);
    }

    private Matcher fieldPatternMatcher(String pattern) {
        Pattern fieldPlaceholderPattern = Pattern.compile(WidgetHandler.FIELD_PLACEHOLDER_PATTERN);
        return fieldPlaceholderPattern.matcher(pattern);
    }

    private class DomainObjectDataExtractor implements DataExtractor {
        private DomainObject domainObject;

        public DomainObjectDataExtractor(DomainObject domainObject) {
            this.domainObject = domainObject;
        }

        @Override
        public Value getValue(String key) {
            return domainObject.getValue(key);
        }

    }

    private class FormStateDataExtractor implements DataExtractor {
        private FormState formState;
        private FormConfig formConfig;

        public FormStateDataExtractor(FormState formState, FormConfig formConfig) {
            this.formState = formState;
            this.formConfig = formConfig;
        }

        @Override
        public Value getValue(String key) {
            return null;
        }

    }
}
