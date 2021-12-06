package ru.intertrust.cm.core.wsserver.impl;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.workflow.WorkflowTaskData;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.core.wsserver.model.CollectionRowData;
import ru.intertrust.cm.core.wsserver.model.DomainObjectData;
import ru.intertrust.cm.core.wsserver.model.FieldData;
import ru.intertrust.cm.core.wsserver.model.FindCollectionRequest;
import ru.intertrust.cm.core.wsserver.model.ValueData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@RestController
@Api(value = "platformWsService")
public class PlatformRestService {

    private DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
    private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private DateTimeFormatter dateTimeWithTimeZoneFormat =  DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Autowired
    private CrudService crudService;

    @Autowired
    private IdService idService;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CollectionsService collectionService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private ProcessService processService;

    @ApiOperation(value = "Find domain object",
            nickname = "find",
            notes = "",
            response = DomainObjectData.class,
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = DomainObjectData.class),
            @ApiResponse(code = 400, message = "not found")}
    )
    @RequestMapping(value = "/crud/find/{objectId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<DomainObjectData> find(@PathVariable(value = "objectId") String id) {
        Id objectId = idService.createId(id);
        DomainObject domainObject = null;
        try {
            domainObject = crudService.find(objectId);
        } catch (ObjectNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DomainObjectData result = getDomainObjectData(domainObject);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "Save domain object",
            nickname = "save",
            notes = "",
            response = DomainObjectData.class,
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = DomainObjectData.class)
    }
    )
    @RequestMapping(value = "/crud/save", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<DomainObjectData> save(@RequestBody DomainObjectData domainObjectInfo) {
        try {
            DomainObject domainObject = null;
            if (domainObjectInfo.getId() == null) {
                domainObject = crudService.createDomainObject(domainObjectInfo.getType());
            } else {
                domainObject = crudService.find(idService.createId(domainObjectInfo.getId()));
            }

            if (domainObjectInfo.getFields() != null) {
                for (FieldData fieldInfo : domainObjectInfo.getFields()) {
                    domainObject.setValue(fieldInfo.getName(), getFieldValue(domainObjectInfo.getType(), fieldInfo));
                }
            }

            domainObject = crudService.save(domainObject);
            DomainObjectData result = getDomainObjectData(domainObject);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception ex) {
            throw new FatalException("Error save domain object", ex);
        }
    }

    @ApiOperation(value = "Save domain objects",
            nickname = "saveAll",
            notes = "",
            response = DomainObjectData.class,
            responseContainer = "List",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = DomainObjectData.class, responseContainer = "List")
    }
    )
    @RequestMapping(value = "/crud/saveall", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<List<DomainObjectData>> saveAll(@RequestBody List<DomainObjectData> domainObjectsData) {
        try {
            List<DomainObject> domainObjects = new ArrayList<>();
            for (DomainObjectData domainObjewctdata : domainObjectsData) {
                DomainObject domainObject = null;
                if (domainObjewctdata.getId() == null) {
                    domainObject = crudService.createDomainObject(domainObjewctdata.getType());
                } else {
                    domainObject = crudService.find(idService.createId(domainObjewctdata.getId()));
                }

                if (domainObjewctdata.getFields() != null) {
                    for (FieldData fieldInfo : domainObjewctdata.getFields()) {
                        domainObject.setValue(fieldInfo.getName(), getFieldValue(domainObjewctdata.getType(), fieldInfo));
                    }
                }

                domainObjects.add(domainObject);
            }

            domainObjects = crudService.save(domainObjects);
            List<DomainObjectData> result = new ArrayList<>();
            for (DomainObject domainObject : domainObjects) {
                result.add(getDomainObjectData(domainObject));
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception ex) {
            throw new FatalException("Error save domain objects", ex);
        }
    }

    @ApiOperation(value = "Delete domain object",
            nickname = "delete",
            notes = "",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation")
    }
    )
    @RequestMapping(value = "/crud/delete/{objectId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable(value = "objectId") String id){
        Id objectId = idService.createId(id);
        crudService.delete(objectId);
    }

    @ApiOperation(value = "Delete all domain object",
            nickname = "deleteAll",
            notes = "",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation")
    }
    )
    @RequestMapping(value = "/crud/deleteall", method = RequestMethod.DELETE)
    public void deleteAll(@RequestBody List<String> ids){
        List<Id> idList = new ArrayList<>();
        for (String id : ids) {
            Id objectId = idService.createId(id);
            idList.add(objectId);
        }
        crudService.delete(idList);
    }

    @ApiOperation(value = "Find linked domain objects",
            nickname = "findLinked",
            notes = "",
            response = DomainObjectData.class,
            responseContainer = "List",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = DomainObjectData.class, responseContainer = "List")}
    )
    @RequestMapping(value = "/crud/findlinked/{objectId}/{linkedType}/{linkedField}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<DomainObjectData>> findLinked(@PathVariable(value = "objectId") String objectId,
                                                             @PathVariable(value = "linkedType") String linkedType,
                                                             @PathVariable(value = "linkedField") String linkedField) {
        Id id = idService.createId(objectId);

        List<DomainObject> linkedDomainObjects = crudService.findLinkedDomainObjects(id, linkedType, linkedField);
        List<DomainObjectData> result = new ArrayList<>();
        for (DomainObject domainObject : linkedDomainObjects) {
            result.add(getDomainObjectData(domainObject));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "Find data by query",
            nickname = "findByQuery",
            notes = "",
            response = CollectionRowData.class,
            responseContainer = "List",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = CollectionRowData.class, responseContainer = "List")}
    )
    @RequestMapping(value = "/crud/findbyquery", method = RequestMethod.POST)
    @ResponseBody
    public List<CollectionRowData> findByQuery(@RequestBody FindCollectionRequest request){
        List<Value> params = new ArrayList<>();
        for (ValueData requestParam : request.getParams()){
            Value param = getValue(requestParam.getType(), requestParam.getValue());
            params.add(param);
        }

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(
                request.getQuery(), params, request.getOffset(), request.getLimit());

        List<CollectionRowData> result = new ArrayList<>();
        for (IdentifiableObject row : collection) {
            result.add(getCollectionRowData(collection.getFieldsConfiguration(), row));
        }
        return result;
    }

    @ApiOperation(value = "Load attachment",
            nickname = "loadAttachment",
            notes = "",
            response = File.class,
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = File.class)}
    )
    @RequestMapping(value = "/crud/attachment/{attachentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> loadAttachment(@PathVariable(value = "attachentId") String attachentId)
    {
        RemoteInputStream remoteInputStream = null;
        InputStream contentStream = null;

        try {
            Id id = idService.createId(attachentId);
            DomainObject attachmentInfo = crudService.find(id);
            remoteInputStream = attachmentService.loadAttachment(id);
            contentStream = RemoteInputStreamClient.wrap(remoteInputStream);

            long size = attachmentInfo.getLong("contentlength");

            Resource result = null;
            if (size < 1000000) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                StreamUtils.copy(contentStream, out);
                result = new ByteArrayResource(out.toByteArray());
            }else{
                File tmp = File.createTempFile("rest-", "-attachment");
                try (FileOutputStream out = new FileOutputStream(tmp)) {
                    StreamUtils.copy(contentStream, out);
                }
                result = new FileSystemResource(tmp);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Length", String.valueOf(size))
                    .header("Content-Disposition", "inline; filename=" + attachmentInfo.getString("name"))
                    .body(result);

        }catch(Exception ex){
            throw new FatalException("Error read attachment " + attachentId, ex);
        }finally {
            try {
                contentStream.close();
                remoteInputStream.close(true);
            }catch (Exception ignoreEx){
            }
        }
    }

    @ApiOperation(value = "Create attachment",
            nickname = "createAttachment",
            notes = "",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation")}
    )
    @RequestMapping(value = "/crud/attachment/{objectId}/{attachmentType}",
            method = RequestMethod.POST, consumes = { "multipart/form-data" })
    @ResponseBody public ResponseEntity<DomainObjectData> createAttachment(@PathVariable String objectId,
                                                             @PathVariable String attachmentType,
                                                             @RequestPart("file") @ApiParam(value="File", required=true) MultipartFile file){
        try {
            Id ownerId = idService.createId(objectId);
            DomainObject attachment = attachmentService.
                    createAttachmentDomainObjectFor(ownerId, attachmentType);
            attachment.setString("name", file.getOriginalFilename());

            try (InputStream in = file.getInputStream()) {
                SimpleRemoteInputStream simpleRemoteInputStream = new SimpleRemoteInputStream(in);

                RemoteInputStream remoteInputStream;
                remoteInputStream = simpleRemoteInputStream.export();
                DomainObject result = attachmentService.saveAttachment(remoteInputStream, attachment);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(getDomainObjectData(result));
            }
        }catch(Exception ex){
            throw new FatalException("Error create attachment", ex);
        }
    }

    @ApiOperation(value = "Update attachment",
            nickname = "updateAttachment",
            notes = "",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation")}
    )
    @RequestMapping(value = "/crud/attachment/{attachmentId}",
            method = RequestMethod.PUT, consumes = { "multipart/form-data" })
    @ResponseBody public ResponseEntity<DomainObjectData> updateAttachment(@PathVariable String attachmentId, @RequestPart("file") @ApiParam(value="File", required=true) MultipartFile file){
        try {
            Id attachnebtObjectId = idService.createId(attachmentId);
            DomainObject attachment = crudService.find(attachnebtObjectId);

            try (InputStream in = file.getInputStream()) {
                SimpleRemoteInputStream simpleRemoteInputStream = new SimpleRemoteInputStream(in);

                RemoteInputStream remoteInputStream;
                remoteInputStream = simpleRemoteInputStream.export();
                DomainObject result = attachmentService.saveAttachment(remoteInputStream, attachment);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(getDomainObjectData(result));
            }
        }catch(Exception ex){
            throw new FatalException("Error create attachment", ex);
        }
    }

    @ApiOperation(value = "Get attachments info",
            nickname = "getAttachmentsInfo",
            response = DomainObjectData.class,
            responseContainer = "List",
            notes = "",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = DomainObjectData.class, responseContainer = "List")}
    )
    @RequestMapping(value = "/crud/attachmentinfo/{objectId}", method = RequestMethod.GET)
    @ResponseBody public ResponseEntity<List<DomainObjectData>> getAttachmentInfo(@PathVariable String objectId){
        Id ownerId = idService.createId(objectId);
        List<DomainObject> attachments = attachmentService.findAttachmentDomainObjectsFor(ownerId);
        List<DomainObjectData> result = new ArrayList<>();
        for (DomainObject domainObject : attachments) {
            result.add(getDomainObjectData(domainObject));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete attachment",
            nickname = "deleteAttachment",
            notes = "",
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation")
    }
    )
    @RequestMapping(value = "/crud/deleteattachment/{attachmentId}", method = RequestMethod.DELETE)
    public void deleteAttachment(@PathVariable(value = "attachmentId") String attachmentId){
        Id id = idService.createId(attachmentId);
        attachmentService.deleteAttachment(id);
    }

    @ApiOperation(value = "Set domain object status",
            nickname = "setStatus",
            notes = "",
            response = DomainObjectData.class,
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = DomainObjectData.class)
    }
    )
    @RequestMapping(value = "/crud/status/{objectId}/{statusName}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<DomainObjectData> setStatus(@PathVariable(value = "objectId") String objectId, @PathVariable(value = "statusName") String statusName) {
        try {
            Id id = idService.createId(objectId);
            DomainObject domainObject = crudService.setStatus(id, statusName);
            DomainObjectData result = getDomainObjectData(domainObject);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception ex) {
            throw new FatalException("Error save domain object", ex);
        }
    }

    @ApiOperation(value = "Assign task",
            nickname = "assignTask",
            notes = "",
            response = String.class,
            authorizations = {@Authorization("basicAuth")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = String.class)
    }
    )
    @RequestMapping(value = "/process/assigntask", method = RequestMethod.POST)
    public String assignTask(@RequestBody WorkflowTaskData taskData){
        return processService.assignTask(taskData).toStringRepresentation();
    }

    private CollectionRowData getCollectionRowData(List<FieldConfig> fieldConfigs, IdentifiableObject row) {
        CollectionRowData result = new CollectionRowData();
        List<FieldData> fields = new ArrayList<>();
        result.setFields(fields);

        Map<String, FieldConfig> fieldConfigByName = new HashMap<>();
        for (FieldConfig fieldConfig : fieldConfigs){
            fieldConfigByName.put(fieldConfig.getName(), fieldConfig);
        }

        for (String fieldName : row.getFields()) {
            Value fieldValue = row.getValue(fieldName);
            FieldConfig fieldConfig = fieldConfigByName.get(fieldName);
            fields.add(new FieldData(fieldName, fieldConfig.getFieldType(), getFieldInfoValue(fieldConfig, fieldValue)));
        }

        return result;
    }

    private DomainObjectData getDomainObjectData(DomainObject domainObject) {
        DomainObjectData result = new DomainObjectData();
        result.setType(domainObject.getTypeName());
        result.setId(domainObject.getId().toStringRepresentation());

        List<FieldData> fields = new ArrayList<>();
        result.setFields(fields);

        for (String fieldName : configurationExplorer.getDomainObjectTypeAllFieldNamesLowerCased(domainObject.getTypeName())) {
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(domainObject.getTypeName(), fieldName);
            Value value = domainObject.getValue(fieldName);
            if (value != null && value.get() != null) {
                FieldData field = new FieldData(fieldName, fieldConfig.getFieldType(), getFieldInfoValue(fieldConfig, value));
                fields.add(field);
            }
        }
        return result;
    }

    private Value getValue(FieldType fieldType, String data){
        Value result = null;
        switch (fieldType) {
            case STRING:
                result = new StringValue(data);
                break;
            case BOOLEAN:
                result = new BooleanValue(Boolean.parseBoolean(data));
                break;
            case DATETIME:
                LocalDateTime date = LocalDateTime.parse(data, dateTimeFormat);
                result = new DateTimeValue(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()));
                break;
            case DATETIMEWITHTIMEZONE:
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(data, dateTimeWithTimeZoneFormat);
                DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone(
                        Date.from(zonedDateTime.toInstant()), TimeZone.getTimeZone(zonedDateTime.getZone().getId()));
                result = new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
                break;
            case DECIMAL:
                result = new DecimalValue(BigDecimal.valueOf(Double.parseDouble(data)));
                break;
            case LONG:
                result = new LongValue(Long.parseLong(data));
                break;
            case REFERENCE:
                result = new ReferenceValue(idService.createId(data));
                break;
            case TIMELESSDATE:
                LocalDate localDate = LocalDate.parse(data, dateFormat);
                result = new TimelessDateValue(new TimelessDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()));
                break;
            case TEXT:
                result = new StringValue(data);
                break;
        }
        return result;
    }

    private Value getFieldValue(String typeName, FieldData fieldInfo) throws ParseException {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(typeName, fieldInfo.getName());
        return getValue(fieldConfig.getFieldType(), fieldInfo.getValue());
    }

    private String getFieldInfoValue(FieldConfig fieldConfig, Value value) {
        String result = null;
        switch (fieldConfig.getFieldType()) {
            case STRING:
                result = ((StringValue) value).get();
                break;
            case BOOLEAN:
                result = ((BooleanValue) value).get().toString();
                break;
            case DATETIME:
                result =  ((DateTimeValue) value).get().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormat);
                break;
            case DATETIMEWITHTIMEZONE:
                DateTimeWithTimeZone dateTimeWithTimeZone = ((DateTimeWithTimeZoneValue) value).get();
                result = ZonedDateTime.of(dateTimeWithTimeZone.getYear(), dateTimeWithTimeZone.getMonth()+1, dateTimeWithTimeZone.getDayOfMonth(),
                        dateTimeWithTimeZone.getHours(), dateTimeWithTimeZone.getMinutes(), dateTimeWithTimeZone.getSeconds(),
                        dateTimeWithTimeZone.getMilliseconds() * 1000000,
                        ZoneId.of(dateTimeWithTimeZone.getTimeZoneContext().getTimeZoneId())).format(dateTimeWithTimeZoneFormat);
                break;
            case DECIMAL:
                result = ((DecimalValue) value).get().toString();
                break;
            case LONG:
                result = ((LongValue) value).get().toString();
                break;
            case REFERENCE:
                result = ((ReferenceValue) value).get().toStringRepresentation();
                break;
            case TIMELESSDATE:
                TimelessDate timelessDate = ((TimelessDateValue) value).get();
                result = LocalDate.of(timelessDate.getYear(), timelessDate.getMonth(), timelessDate.getDayOfMonth()).format(dateFormat);
                break;
            case TEXT:
                result = ((StringValue) value).get();
                break;
        }
        return result;
    }

}
