package ru.intertrust.cm.plugins.permission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.dao.access.DynamicGroupProcessor;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.plugins.PluginBase;

@Plugin(name = "RecalcAllGroupMembersPlugin",
        description = "Пересчет состава всех динамических групп по конфигурации. Поддерживаются параметры packageSize и threadCount", transactional = false)
public class RecalcAllGroupMembersPlugin extends PluginBase implements PluginHandler {
    private static final int DEFAULT_PACKAGE_SIZE = 1000;
    private static final int DEFAULT_THREAD_COUNT = 8;
    private static final String PACKAGE_SIZE = "packageSize";
    private static final String THREAD_COUNT = "threadCount";

    @Autowired
    private CollectionsService collectionService;

    @Autowired
    private DynamicGroupProcessor dynamicGroupProcessor;

    @Override
    public String execute(EJBContext context, String param) {
        //Размер пакета по умолчанию
        int packageSize = DEFAULT_PACKAGE_SIZE;
        //Количество потоков по умолчанию
        int threadCount = DEFAULT_THREAD_COUNT;

        Map<String, String> params = getParametersMap(param);
        if (params.containsKey(PACKAGE_SIZE)) {
            packageSize = Integer.parseInt(params.get(PACKAGE_SIZE));
        }

        if (params.containsKey(THREAD_COUNT)) {
            threadCount = Integer.parseInt(params.get(THREAD_COUNT));
        }

        try {
            info("Start plugin RecalcAllGroupMembersPlugin packageSize={0}, threadCount={1}", packageSize, threadCount);

            ///Получение состава всех групп
            String query = "select id from user_group";
            IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);

            info("Found {0} groups for calculation", collection.size());

            List<Future> futures = new ArrayList<Future>();
            int groupCount = 0;

            Set<Id> groupPackage = new HashSet<>();

            for (IdentifiableObject identifiableObject : collection) {
                if (((SessionContext) context).wasCancelCalled()) {
                    info("Terminate work of plugin RecalcAllGroupMembersPlugin.");
                    break;
                }

                groupPackage.add(identifiableObject.getId());
                groupCount++;

                if (groupPackage.size() >= packageSize || collection.size() == groupCount) {
                    recalcPackage(futures, groupPackage, threadCount);
                    groupPackage = new HashSet<>();
                }

            }

            //Ожидаем окончание работы асинхронных процессов
            for (int i = (futures.size() - 1); i >= 0; i--) {
                futures.get(i).get();
            }

            info("Finish plugin RecalcAllGroupMembersPlugin. Recalc {0} groups", groupCount);
            return getLog();
        } catch (Exception ex) {
            throw new FatalException("Error execute RecalcAllGroupMembersPlugin", ex);
        }
    }

    private void recalcPackage(List<Future> futures, Set<Id> groupPackage, int threadCount) {
        while (true) {
            //Удаление из массива потоков которые завершились
            for (int i = (futures.size() - 1); i >= 0; i--) {
                if (futures.get(i).isDone()) {
                    futures.remove(i);
                }
            }

            // Проверка на наличие свободных потоков
            if (futures.size() <= threadCount) {
                Future future = dynamicGroupProcessor.calculateDynamicGroupAcync(groupPackage);
                info("Start calculate async processor for {0} groups", groupPackage.size());
                futures.add(future);
                break;
            } else {
                //Ждем немного и повторяем попытку заполучить поток
                debug("All threads for recalc group is work, sleep. Work thread count={0}", futures.size());
                try {
                    Thread.currentThread().sleep(100);
                } catch (Exception ignoreEx) {
                    error("Error on sleep", ignoreEx);
                }
            }
        }
    }
}
