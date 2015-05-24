package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.IdentifiedConfig;
import ru.intertrust.cm.core.config.gui.form.extension.IdentifiedFormExtensionOperation;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.05.2015
 *         Time: 11:18
 */
public class ExtensionProcessorHelper {
    public static <T extends IdentifiedConfig> void processReplaceConfigs(List<T> target, List<T> source,
                                                                          ExtensionOperationStatus operationStatus) {
        for (int i = 0; i < source.size(); i++) {
            T identifiedConfig = source.get(i);
            processReplaceConfig(identifiedConfig, target, operationStatus);
        }

    }
    private static <T extends IdentifiedConfig> void processReplaceConfig(T sourceItem, List<T> target,
                                                                          ExtensionOperationStatus operationStatus) {
        String id = sourceItem.getId();
        int index = findIndexById(id, target);
        if (index == -1) {
            operationStatus.putError(id, String.format("Could not replace config with id '%s'", id));
        } else {
            target.remove(index);
            target.add(index, sourceItem);
            operationStatus.addSuccessful(id);
        }

    }

    public static <T extends IdentifiedConfig> void processAddConfigsBefore(String id, List<T> target, List<T> source,
                                                                            ExtensionOperationStatus operationStatus) {
        int index = findIndexById(id, target);
        if (index == -1) {
            operationStatus.putError(id, String.format("Could not add configs before config with id '%s'", id));
        } else {
            index = index == 0 ? 0 : index - 1;
            target.addAll(index, source);
            operationStatus.addSuccessful(id);
        }

    }

    public static <T extends IdentifiedConfig> void processAddConfigsAfter(String id, List<T> target, List<T> source,
                                                                           ExtensionOperationStatus operationStatus) {
        int index = findIndexById(id, target);
        if (index == -1) {
            operationStatus.putError(id, String.format("Could not add configs after config with id '%s'", id));
        } else {
            target.addAll(index + 1, source);
            operationStatus.addSuccessful(id);
        }

    }

    public static <T extends IdentifiedConfig> void processAddConfigs(List<T> target, List<T> source){
            target.addAll(source);
    }

    public static <T extends IdentifiedConfig, S extends IdentifiedConfig> void processDeleteConfigs(List<T> target, List<S> source,
                                                                         ExtensionOperationStatus operationStatus) {
        for (int i = 0; i < source.size(); i++) {
            IdentifiedConfig identifiedConfig = source.get(i);
            String id = identifiedConfig.getId();
            boolean deleted = processDeleteConfig(id, target);
            if (deleted) {
                operationStatus.addSuccessful(id);
            } else {
                operationStatus.putError(id, String.format("Could not delete config with id '%s'", id));
            }

        }
    }

    public static boolean isProcessedSuccessful(Collection<ExtensionOperationStatus> extensionOperationStatuses) {
        for (ExtensionOperationStatus extensionOperationStatus : extensionOperationStatuses) {
            if (extensionOperationStatus.isNotSuccessful()) {
                return false;
            }
        }
        return true;
    }

    public static <T extends IdentifiedConfig> void fillErrors(Collection<ExtensionOperationStatus> extensionStatuses,
                                                         List<String> errors) {
        for (ExtensionOperationStatus operationStatus : extensionStatuses) {
            if (operationStatus.isNotSuccessful()) {
                errors.add(operationStatus.toErrorString());
            }
        }

    }

    public static void fillErrors(ExtensionOperationStatus operationStatus, List<String> errors) {
        if (operationStatus.isNotSuccessful()) {
            errors.add(operationStatus.toErrorString());
        }
    }

    private static <T extends IdentifiedConfig> boolean processDeleteConfig(String id, List<T> target) {
        boolean deleted = false;
        Iterator<T> iterator = target.iterator();
        while (iterator.hasNext() && !deleted) {
            IdentifiedConfig identifiedConfig = iterator.next();

            if (id.equalsIgnoreCase(identifiedConfig.getId())) {
                iterator.remove();
                deleted = true;
            }
        }
        return deleted;

    }

    private static int findIndexById(String id, List<? extends IdentifiedConfig> target) {
        for (int i = 0; i < target.size(); i++) {
            IdentifiedConfig identifiedConfig = target.get(i);
            if (id.equalsIgnoreCase(identifiedConfig.getId())) {
                return i;
            }

        }
        return -1;
    }

    public static  <T extends IdentifiedConfig> Map<IdentifiedFormExtensionOperation<T>, ExtensionOperationStatus> createOperationMap(List<IdentifiedFormExtensionOperation<T>> operations) {
        Map<IdentifiedFormExtensionOperation<T>, ExtensionOperationStatus> result =
                new LinkedHashMap<>(operations.size()); //exceptions order is important
        for (IdentifiedFormExtensionOperation<T> operation : operations) {
            result.put(operation, new ExtensionOperationStatus());
        }
        return result;
    }
}
