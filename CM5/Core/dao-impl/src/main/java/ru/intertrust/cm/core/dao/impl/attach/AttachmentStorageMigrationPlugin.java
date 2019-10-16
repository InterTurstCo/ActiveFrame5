package ru.intertrust.cm.core.dao.impl.attach;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.plugin.Plugin;
import ru.intertrust.cm.core.business.api.plugin.PluginHandler;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.dto.AttachmentInfo;

/**
 * Плагин, выполняющий миграцию (перенос) файлов вложений из старого хранилища в новое.
 * Предназначен для использования после переконфигурации хранилищ вложений в системе.
 * 
 * Плагин принимает строку параметров следующего вида:<br>
 * <i>параметр:значение;параметр:значение...</i><br>
 * Параметры могут следовать в любом порядке; все параметры являются необязательными
 * (т.е. строка параметров может быть пустой). Поддерживаются следующие параметры:<ul>
 * <li>type &mdash; тип доменных объектов вложений, которые необходимо мигрировать. Могут быть перечислены
 * несколько типов через запятую (,) либо указана звёздочка (*), что означает обработку <i>всех</i> типов вложений.
 * Звёздочка также является значением по умолчанию.
 * <li>date &mdash; позволяет ограничить миграцию только вложениями, созданными в определённый период времени.
 * Дата должна быть в формате ГГГГММДД, например: 20180214. Можно задать одну дату или период времени &mdash;
 * две даты, разделённые знаком "минус" (-). В качестве любого конца диапазона может быть также использована
 * звёздочка (*), означающая отсутствие ограничения с данного конца. Например, 20180101-* означает все вложения,
 * созданные начиная с 1 января 2018 г. Значение параметра по умолчанию: *-*
 * <li>path &mdash; ограничивает выборку вложений теми, <i>локальный</i> путь которых начинается со значения,
 * указанного в этом параметре. Значение по умолчанию отсутствует.
 * <li>from &mdash; задаёт путь к старому хранилищу вложений. Если задан этот параметр, то возможность системы
 * по поиску вложений в альтернативных хранилищах не используется, а файлы ищутся <i>только</i> в заданной папке.
 * Позволяет выполнить миграцию без конфигурирования альтернативных хранилищ. Следует помнить, однако, что
 * в этом случае вложения будут недоступны системе и её пользователям до завершения миграции.
 * </ul>
 * 
 * @author apirozhkov
 */
//@ScheduleTask(name = "AttachmentStorageMigrationTask", hour = "1", minute = "0", active = false,
//              taskTransactionalManagement = true)
@Plugin(name = "AttachmentStorageMigrationPlugin", description = "Миграция вложений из старого хранилища в новое",
        transactional = false)
public class AttachmentStorageMigrationPlugin implements PluginHandler {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentStorageMigrationPlugin.class);
    private static final int FETCH_SIZE = 100;

    @Autowired private ConfigurationExplorer configurationExplorer;
    @Autowired private AttachmentContentDaoImpl attachmentContentDao;   // need to access protected members of the class
    @Autowired private AttachmentStorageConfigHelper configHelper;
    @Autowired private DomainObjectDao domainObjectDao;
    @Autowired private CollectionsDao collectionsDao;
    @Autowired private AccessControlService accessControlService;
    @Autowired private DomainObjectTypeIdCache typeIdCache;

    private class Stats {
        int files;
        int types;
        int moved;
        int errors;
        long bytes;
        long copyTime;
    }

    private class Params {
        static final String TYPE = "type";
        static final String DATE = "date";
        static final String PATH = "path";
        static final String FROM = "from";
        static final String TYPE_ALL = "*";
        static final String DATE_ANY = "*";

        String[] types;
        Date startDate;
        Date endDate;
        String subPath;
        String fromPath;

        Params(String paramString) {
            if (paramString != null && paramString.length() > 0) {
                String[] options = paramString.split("\\s*;\\s*");
                for (String option : options) {
                    String[] split = option.split("\\s*:\\s*");
                    if (split.length != 2) {
                        throw new IllegalArgumentException("Option " + option + ": syntax error");
                    }
                    String name = split[0].toLowerCase();
                    if (TYPE.equals(name)) {
                        if (TYPE_ALL.equals(split[1])) {
                            // types will be filled at the end of the constructor, as if the option were not present
                            continue;
                        }
                        String[] types = split[1].split("\\s*,\\s*");
                        if (types.length == 0) {
                            throw new IllegalArgumentException("Syntax error: no types listed");
                        }
                        for (String type : types) {
                            if (!configurationExplorer.isAttachmentType(type)) {
                                throw new IllegalArgumentException(type + " is not an attachment type");
                            }
                        }
                        this.types = types;
                    } else if (DATE.equals(name)) {
                        String[] dates = split[1].split("\\s*-\\s*");
                        if (dates.length < 1 || dates.length > 2) {
                            throw new IllegalArgumentException("Option " + name + ": wrong syntax");
                        }
                        startDate = parseDate(dates[0]);
                        endDate = dates.length == 2 ? parseDate(dates[1]) : startDate;
                        if (endDate != null) {
                            endDate = new Date(endDate.getTime() + TimeUnit.DAYS.toSeconds(1));
                        }
                    } else if (PATH.equals(name)) {
                        subPath = split[1];
                    } else if (FROM.equals(name)) {
                        fromPath = split[1];
                    } else {
                        throw new IllegalArgumentException("Invalid option: " + name);
                    }
                }
            }
            if (types == null) {
                types = configurationExplorer.getAllAttachmentTypes();
            }
        }

        Date parseDate(String dateString) {
            if (dateString.isEmpty() || DATE_ANY.equals(dateString)) {
                return null;
            }
            if (dateString.length() != 8) {
                throw new IllegalArgumentException("Wrong date: " + dateString);
            }
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(dateString.substring(0, 4)),
                    Integer.parseInt(dateString.substring(4, 6)) - 1,
                    Integer.parseInt(dateString.substring(6, 8)));
            return cal.getTime();
        }
    }

    @Override
    public String execute(EJBContext ejbContext, String param) {
        Params params = new Params(param);
        if (params.fromPath == null && attachmentContentDao.getAlternateStorageNames().length == 0) {
            return "Nothing to do: no alternate storages";
        }

        Stats stats = new Stats();
        long started = System.currentTimeMillis();
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());

        mainCycle:
        for (String attachmentType : params.types) {
            String parentRefFieldName = configurationExplorer.getAttachmentParentType(attachmentType);
            BundleQuery query = new BundleQuery(attachmentType, params, accessToken);
            for (int num = 0; true; ++num) {
                IdentifiableObjectCollection bundle = query.getBundle(num);
                //List<DomainObject> bundle = domainObjectDao.findAll(attachmentType, offset, FETCH_SIZE, accessToken);
                if (num == 0 && bundle.size() > 0) {
                    ++stats.types;
                }
                for (int idx = 0; idx < bundle.size(); ++idx) {
                    IdentifiableObject attachmentData = bundle.get(idx);
                    ++stats.files;
                    //Id parentId = attachment.getReference(parentRefFieldName);
                    String storageName = configHelper.getStorageForAttachment(typeIdCache.getName(attachmentData.getId()),
                            typeIdCache.getName(attachmentData.getReference("ParentId")));
                    AttachmentStorage storage = attachmentContentDao.getStorageByName(storageName);
                    AttachmentInfo info = new AttachmentInfo();
                    info.setRelativePath(attachmentData.getString("Path"));
                    info.setContentLength(attachmentData.getLong("ContentLength"));
                    if (storage.hasContent(info)) {
                        continue;
                    }
                    if (params.fromPath == null) {
                        AttachmentStorage altStorage = attachmentContentDao.findInAlternateStorage(info, storageName);
                        if (altStorage == null) {
                            logger.error("Attachment " + info.getRelativePath() + " not found in any storage; is it lost?");
                            ++stats.errors;
                            continue;
                        }
                        moveAttachment(attachmentData.getId(), storage, altStorage, info.getRelativePath(),
                                params, stats, ejbContext, accessToken, parentRefFieldName);
                    } else {
                        
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        break mainCycle;
                    }
                }
                if (bundle.size() < FETCH_SIZE) {
                    break;
                }
            }
        }
        long finished = System.currentTimeMillis();
        return String.format("%s %dms; %d attachments of %d types checked, %d files moved%s, %d errors",
                Thread.currentThread().isInterrupted() ? "Interrupted after" : "Complete in",
                finished - started, stats.files, stats.types, stats.moved,
                stats.moved > 0 ? String.format(" [%d bytes copied in %dms]", stats.bytes, stats.copyTime) : "",
                stats.errors);
    }

    private void moveAttachment(Id attachmentId, AttachmentStorage targetStorage, AttachmentStorage sourceStorage,
            String relativePath, Params params, Stats stats, EJBContext ejbContext, final AccessToken accessToken,
            final String parentRefFieldName) {
        InputStream content = null;
        try {
            ejbContext.getUserTransaction().begin();
            final DomainObject attachment = domainObjectDao.find(attachmentId, accessToken);

            if (sourceStorage != null) {
                content = sourceStorage.getContent(relativePath);
            } else {
                Path fullPath = Paths.get(params.fromPath, relativePath);
                content = new FileInputStream(fullPath.toFile());
            }
            logger.info("Copying attachment " + relativePath);
            long copyStarted = System.currentTimeMillis();
            AttachmentInfo newInfo = targetStorage.saveContent(content,
                    new AttachmentStorage.CachedContext(new AttachmentStorage.Context() {
                        @Override
                        public DomainObject getParentObject() {
                            Id parentId = attachment.getReference(parentRefFieldName);
                            return domainObjectDao.find(parentId, accessToken);
                        }

                        @Override
                        public String getFileName() {
                            return attachment.getString("Name");
                        }

                        @Override
                        public Calendar getCreationTime() {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(attachment.getCreatedDate());
                            return cal;
                        }

                        @Override
                        public String getAttachmentType() {
                            return attachment.getTypeName();
                        }
                    }));
            stats.copyTime += System.currentTimeMillis() - copyStarted;
            attachment.setString("Path", newInfo.getRelativePath());
            domainObjectDao.save(attachment, accessToken);
            if (sourceStorage != null) {
                sourceStorage.deleteContent(relativePath);
            }   // Do not remove when copying from folder
            ejbContext.getUserTransaction().commit();
            ++stats.moved;
            stats.bytes += newInfo.getContentLength();
        } catch (Exception e) {
            logger.error("Problem migrating attachment " + relativePath, e);
            ++stats.errors;
            try {
                ejbContext.getUserTransaction().rollback();
            } catch (Exception ee) {
                logger.error("Transaction rollback failed", ee);
            }
        }finally {
            try {
                if (content != null) {
                    content.close();
                }
            } catch (IOException ignoreEx) {
            }
        }
    }

    private class BundleQuery {
        String query;
        @SuppressWarnings("rawtypes")
        Value[] params;
        AccessToken accessToken;

        BundleQuery(String type, Params params, AccessToken accessToken) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT id, Path, ContentLength, ")
                    .append(configurationExplorer.getAttachmentParentType(type))
                    .append(" as ParentId FROM ").append(type);
            List<String> filters = new ArrayList<>();
            @SuppressWarnings("rawtypes")
            List<Value> filterParams = new ArrayList<>();
            int paramNum = 0;
            if (params.startDate != null) {
                filters.add(String.format("created_date >= {%d}", paramNum++));
                filterParams.add(new DateTimeValue(params.startDate));
            }
            if (params.endDate != null) {
                filters.add(String.format("created_date < {%d}", paramNum++));
                filterParams.add(new DateTimeValue(params.endDate));
            }
            if (params.subPath != null) {
                filters.add(String.format("path LIKE {%d}", paramNum++));
                filterParams.add(new StringValue(params.subPath + "%"));
            }
            String glue = " WHERE ";
            for (String filter : filters) {
                query.append(glue).append(filter);
                glue = " AND ";
            }
            query.append(" ORDER BY id");
            this.query = query.toString();
            this.params = filterParams.toArray(new Value[filterParams.size()]);
            this.accessToken = accessToken;
        }

        IdentifiableObjectCollection getBundle(int index) {
            return collectionsDao.findCollectionByQuery(query, Arrays.asList(params),
                    index * FETCH_SIZE, FETCH_SIZE, accessToken);
        }
    }
}
