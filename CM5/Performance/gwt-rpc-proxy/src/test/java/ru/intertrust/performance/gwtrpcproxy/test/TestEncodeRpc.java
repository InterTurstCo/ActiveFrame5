package ru.intertrust.performance.gwtrpcproxy.test;

import ru.intertrust.performance.jmetertools.GwtRpcRequest;
import ru.intertrust.performance.jmetertools.GwtUtil;

import com.google.gwt.user.client.rpc.SerializationException;

public class TestEncodeRpc {
    public static void main(String[] args) throws SerializationException {
        String requestString =
                "7|0|535|http://localhost:8090/cm-sochi/ru.intertrust.cm.core.gui.impl.BusinessUniverseEntryPoint/|054A665A66D6A7C42043EAEA61DC385B|ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService|executeCommand|ru.intertrust.cm.core.gui.model.Command/931633538|save.action|executeAction|ru.intertrust.cm.core.gui.model.action.SaveActionContext/2769412909|ru.intertrust.cm.core.gui.model.form.FormState/2543100334|java.util.HashMap/1797211028|java.lang.String/2004016611|AuthorizationError|Ошибка авторизации.  |Password|Пароль|MayMonthShortStandalone|Май|[DOMAIN_OBJECT]country|Страна|validate.length.too-small|Длина поля ${field-name} не может быть меньше чем${min-length}|NotificationException|Ошибка отправки сообщения.|SeptemberMonthShort|сент.|GuiException.SearchError|Ошибка при поиске:|NoNewNotifications|Новых уведомлений нет|WeekSwitchButton|Суббота, Воскресенье|SundayFirstCapital|В|[SEARCH_FIELD]Employee.Position|Должность|EventTriggerException|Ошибка инициализации тригера.|NovemberMonthShort|нояб.|ResetSettings|Сбросить настройки|OctoberMonthShortStandalone|Окт.|GuiException.CommandExecutionError|Команда не может быть выполнена: |LightTheme|Светлая тема|SignOn|Войти|ObjectNotFoundException|Данных не найдено.|ForLastYear|За год|[SEARCH_DOMAIN_OBJECT]Internal_Document|Внутренний документ|CollectionConfigurationException|Ошибка конфигурации коллекции.|ProfileException|Ошибка обработки профиля системы/пользователя.|Monday|Понедельник|[SEARCH_FIELD]Employee.FirstName|Имя|ScheduleException|Ошибка работы подсистемы периодических заданий|GuiException.VersionError|Ошибка получения версии: |[SEARCH_FIELD]Employee.LastName|Фамилия|SaveButton|Сохранить|BlackTheme|Тёмная тема|JunMonthShort|июня|GuiException.ManyDefaultForms|There's more than 1 default form for type: |SeptemberMonthShortStandalone|Сент|GuiException.NoProcessName|Не задано имя процесса|Info|Справка|AugustMonthShortStandalone|Авг.|SearchException|Ошибка работы подсистемы поиска|ForLastWeek|За неделю|AprilMonthShort|апр.|Wednesday|Среда|ReportServiceException|Ошибка обработки отчета|validate.range.too-small|Значение поля ${field-name} не может быть меньше чем ${range-start}|AddConfigFiles|Добавьте файлы конфигурации:|[SEARCH_FIELD]Employee.EMail|Email|RememberMe|Запомнить меня|CrudException|Ошибка операции с БД.|Friday|Пятница|GuiException.ProcessNotSupported|Process '${processType}' not supported.|ForYesterday|За вчера|ResetAllSettings|Сбросить все настройки|GuiException.RefPathNotSupported|Reference ${path} not supported|AprilMonthShortStandalone|Апр.|JulyMonthShortStandalone|Июль|ActionServiceException|Невозможно получить список действий для объекта.|SystemException|Системная ошибка при выполнении ${commandName}, обратитесь к администратору.|ProcessException|Ошибка выполнения workflow процесса.|[FIELD]country.population|Население|JanuaryMonthShortStandalone|Янв.|JanuaryMonthShort|янв.|ForToday|За сегодня|ColumnsDisplayTooltip|Отображение видимых колонок|AugustMonthShort|авг.|OctoberMonthShort|окт.|GuiException.CollectionViewError|Collection view config has no display tags configured|ConfigurationException|Ошибка конфигурации объекта.|[SEARCH_FIELD]Country.independence_day|День Независимости|FridayFirstCapital|П|validate.integer|'${value}' должно быть целым!|[SEARCH_AREA]Countries-Search-Area|Страны|GuiException.SortingFieldNotFound|Couldn't find sorting '${field}'|AddReportFiles|Добавьте файлы шаблона отчета:|OpenInFullWindowButton|Открыть в полном окне|NovemberMonthShortStandalone|Нояб.|GuiException.UnknownUrl|Неизвестный url|validate.length.too-big|Длина поля ${field-name} не может быть больше чем ${max-length}|Exit|Выход|GuiException.ObjectNotExist|Object with id: ${objectId} doesn't exist|MonthSwitchButton|Задачи дня|ChooseLanguage|Выбрать язык|ContinueButton|Продолжить|[FIELD]country.capital|Столица|[SEARCH_DOMAIN_OBJECT]Employee|Сотрудник|GuiException.ReportNameNotFound|Имя отчета не сконфигурировано ни в плагине, ни форме!|GuiException.SingleFieldPath|Only single field-path is supported|FebruaryMonthShort|февр.|validate.precision|Значение поля ${field-name} должно иметь точность ${precision}|CoreVersion|Версия платформы:|NoResults|Результаты отсутствуют|EMail|EMail:|DoelException|Ошибка обработки DOEL выражения.|Superuser|Супер-администратор|NoConnectionError|Ошибка авторизации. Невозможно подключиться к серверу.|MailNotificationException|Ошибка отправки/приема сообщения по email.|MondayFirstCapital|GuiException.ObjectNotSaved|Объект ещё не сохранён|Tuesday|Вторник|ExtensionPointException|Ошибка инициализации точки расширения.|JunMonthShortStandalone|Июнь|ThursdayFirstCapital|Ч|\ufeffAccessException|У вас нет прав доступа к даному объекту.|LucemTheme|Lucem тема|validate.max|Поле ${field-name} не может быть больше чем ${value}!|[SEARCH_FIELD]Employee.Department|Департамент|Saturday|Суббота|AttachmentUnavailable|Невозможно скачать вложение|[SEARCH_DOMAIN_OBJECT]Country|FilterTooltip|Отобразить/Скрыть фильтры|DoelParseException|Ошибка разбора DOEL выражения.|MarchMonthShort|марта|Thursday|Четверг|CloseButton|Закрыть|SizeActionTooltip|Распахнуть/Свернуть|UserName|Имя пользователя|ChooseTheme|Выбрать тему|GuiException.CommandCallError|Ошибка вызова команды: |WrongPswError|Ошибка авторизации. Проверте правильность введенных данных.|MarchMonthShortStandalone|Март|FatalException|Фатальная ошибка приложения.|DefaultTheme|Основная тема|validate.positive-int|'${value}' должно быть целым положительным!|ChoosePeriod|Выбрать период|validate.decimal|'${value}' должно быть десятичным!|Version|Версия:|ServerValidationException|Server-side validation failed|FirstName|Имя:|[FIELD]country.square|Площадь|validate.positive-dec|'${value}' должно быть десятичным положительным!|validate.length.not-equal|Длина поля ${field-name} должна быть равна ${length}|WednesdayFirstCapital|С|[SEARCH_FIELD]Internal_Document.Registrant|Регистратор|MayMonthShort|мая|FavoriteActionTooltip|Показать/Скрыть избранное|CollectionQueryException|Ошибка в SQL запросе.|CancellationButton|Отмена|GuiException.HierarchCollection|Ошибка в конфигурации иерархической коллекции|Login|Логин:|validate.scale|Значение поля ${field-name} должно иметь ${scale} знаков после запятой|SaturdayFirstCapital|GuiException.NoProcessType|Не задано тип процесса|GuiException.ReportFormError|Конфигурация формы отчета не найдена или некорректна! Форма: '${formName}', отчет: '${reportName}'|validate.unique-field|Поле ${field-name} со значением '${value}' уже существует!|[FIELD]country.name|Название|Administrator|Администратор|validate.pattern|Поле ${field-name} должно соответствовать шаблону ${pattern}!|UnexpectedException|Неизвестная ошибка приложения.|ColumnsWidthTooltip|Отобразить все колонки на экране|LastName|Фамилия:|CancelButton|Отменить|InvalidIdException|Некоректный идентификатор ДО.|FindButton|Найти|Sunday|Воскресенье|TuesdayFirstCapital|GuiException.WidgetIdNotFound|Widget, id: ${widgetId} is not configured with Field Path|DecemberMonthShortStandalone|Дек.|AuthenticationException|Ошибка авторизации.|GuiException.CommandNotFound|Команда ${commandName} не найдена|validate.range.too-big|Значение поля ${field-name} не может быть больше чем ${range-end}|JulyMonthShort|июля|DecemberMonthShort|дек.|OptimisticLockException|Сохранение невозможно, данные изменены другим пользователем.|validate.not-empty|Поле ${field-name} не должно быть пустым!|ChangeButton|Изменить|ValidationException|Исправьте ошибки перед сохранением|PermissionException|Ошибка создания группы.|DoneButton|Готово|Search|Поиск|GuiException.MultipleFieldPaths|Multiply fieldPaths should be all reference type or all backreference type|FebruaryMonthShortStandalone|Февр.|country_form|ru.intertrust.cm.core.gui.model.form.FormObjects/208332213|ru.intertrust.cm.core.gui.model.form.FieldPath/4003589935||ru.intertrust.cm.core.gui.model.form.SingleObjectNode/2597964450|ru.intertrust.cm.core.business.api.dto.GenericDomainObject/1930765710|country|java.util.LinkedHashMap/3008245022|created_date|ru.intertrust.cm.core.business.api.dto.DateTimeValue/4282871590|java.util.Date/3385151746|updated_date|name|ru.intertrust.cm.core.business.api.dto.StringValue/171161245|Необходимо ввести наименование страны!!!|java.util.LinkedHashSet/95640124|id|status|created_by|updated_by|short_name|independence_day|population|is_country_rich|square|capital|most_famous_city|country_union|description|flag|person_document|city|city^country|ru.intertrust.cm.core.gui.model.form.MultiObjectNode/1138205203|java.util.ArrayList/4159755760|country_most_famous_city^country.city|country_most_famous_city|country_fauna^country.animal|country_fauna|country_attachment^country|country_attachment|country_pdf_attachment^country|country_pdf_attachment|1|label|2|text-box|3|4|integer-box|5|6|decimal-box|7|9|10|date-box|11|12|text-area|13|100|101|radio-button|cities_label|flag_attachment|attachment-box|attachmentLbl|best_desc_label|cities|suggest-box|flagAttachmentLbl|actionExecutorId|action-executor|test|attachmentViewer|attachment-viewer|17a|table-browser|best_friend_label|7a|attachmentPdfLbl|city_table|linked-domain-objects-table|fauna_attachment|8a|list-box|summary|faunaAttachmentLbl|summary_label|best_friend|ru.intertrust.cm.core.gui.model.form.widget.TextState/3034406799|${__RandomString(50,wertyuiopasdfghjklzxcvbnm)}|ru.intertrust.cm.core.business.api.dto.Constraint/3820746904|max-length|128|field-name|widget-id|domain-object-type|ru.intertrust.cm.core.business.api.dto.Constraint$Type/2780199869|ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState/877195015|java.lang.Long/4227064769|pattern|integer|ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxState/2917726815|java.math.BigDecimal/8151472|decimal|scale|precision|15|ru.intertrust.cm.core.gui.model.form.widget.DateBoxState/4153728316|ru.intertrust.cm.core.gui.model.DateTimeContext/3823528771|GMT-4|date-pattern|dd.MM.yyyy HH:mm:ss|1024|ru.intertrust.cm.core.gui.model.form.widget.RadioButtonState/176903789|ru.intertrust.cm.core.gui.model.form.widget.RadioButtonState$Layout/123652275|ru.intertrust.cm.core.gui.model.form.widget.ListBoxState/50684465|ru.intertrust.cm.core.business.api.dto.impl.RdbmsId/3936109561|not-empty|ru.intertrust.cm.core.gui.model.form.widget.SuggestBoxState/611657939|ru.intertrust.cm.core.gui.model.form.widget.LinkedDomainObjectsTableState/1725912574|ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig/3720076234|ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig/1431529992|Cities|ru.intertrust.cm.core.config.gui.form.widget.LinkedFormConfig/3182161856|city_form|ru.intertrust.cm.core.config.gui.form.title.TitleConfig/3988341041|ru.intertrust.cm.core.config.gui.form.title.ExistingObjectConfig/1829757717|ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig/3907027861|ru.intertrust.cm.core.config.gui.form.widget.DateFormatConfig/2400442184|dd-mm-yyyy HH-mm-ss|short|ru.intertrust.cm.core.config.gui.form.widget.TimeZoneConfig/3192440636|Europe/Kiev|ru.intertrust.cm.core.config.gui.form.widget.FieldPathsConfig/1003098082|ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig/782996961|ru.intertrust.cm.core.config.gui.form.widget.PatternConfig/4151484682|{id}, {created_date}|ru.intertrust.cm.core.config.gui.form.title.NewObjectConfig/1643402117|new form |300px|500px|ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig/871073959|ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig/2722924466|ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig/2398195726|asc|ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig/3446098967|java.lang.Boolean/476441737|ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig/839666214|ru.intertrust.cm.core.config.gui.form.widget.SummaryTableColumnConfig/1797621092|dd-mm-yyyy|date_of_birth|ru.intertrust.cm.core.config.gui.form.widget.NumberFormatConfig/2343134263|###.####|age|ru.intertrust.cm.core.config.gui.form.widget.LinkedTablePatternConfig/1207603731|{name}|ru.intertrust.cm.core.config.gui.form.widget.SummaryTableActionColumnConfig/2143731312|ru.intertrust.cm.core.config.gui.form.widget.ColumnDisplayConfig/527108332|ru.intertrust.cm.core.config.gui.form.widget.ColumnDisplayImageConfig/354092494|/images/icons/pcontrol.png|ru.intertrust.cm.core.config.gui.form.widget.ColumnDisplayTextConfig/48171374|Отобразить|before|Отобразить на форме|view|{population}|edit|{category}|city_category|Категория|ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig/130951234|ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig/438417082|ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder/3816150570|, 22-45-2015 09-45-17|ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState/1545364389|ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState/2193010652|ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig/677473702|ru.intertrust.cm.core.gui.model.plugin.FormPluginState/1343425564|ru.intertrust.cm.core.config.gui.action.ActionConfig/985704273|ru.intertrust.cm.core.config.gui.action.ActionDisplayType/4049623642|actionBar-save|aSave|Сохранить изменения|ru.intertrust.cm.core.config.gui.action.ActionType/3921025904|Write|java.lang.Integer/3438268394|not preview|1|2|3|4|1|5|5|6|0|7|8|9|10|169|11|12|11|13|11|14|11|15|11|16|11|17|11|18|11|19|11|20|11|21|11|22|11|23|11|24|11|25|11|26|11|27|11|28|11|29|11|30|11|31|11|32|11|33|11|34|11|35|11|36|11|37|11|38|11|39|11|40|11|41|11|42|11|43|11|44|11|45|11|46|11|47|11|48|11|49|11|50|11|51|11|52|11|53|11|54|11|55|11|56|11|57|11|58|11|59|11|60|11|61|11|62|11|63|11|64|11|65|11|66|11|67|11|68|11|69|11|70|11|71|11|72|11|73|11|74|11|75|11|76|11|77|11|78|11|79|11|80|11|81|11|82|11|83|11|84|11|85|11|86|11|87|11|88|11|89|11|90|11|91|11|92|11|93|11|94|11|95|11|96|11|97|11|98|11|99|11|100|11|101|11|102|11|103|11|104|11|105|11|106|11|107|11|108|11|109|11|110|11|111|11|112|11|113|11|114|11|115|11|116|11|117|11|118|11|119|11|120|11|121|11|122|11|123|11|124|11|125|11|126|11|127|11|128|11|129|11|130|11|131|11|132|11|133|11|134|11|135|11|136|11|137|11|138|11|139|11|140|11|141|11|142|11|143|11|144|11|145|11|146|11|147|11|148|11|149|11|150|11|151|11|152|11|153|11|154|11|155|11|156|11|157|11|158|11|159|11|160|11|161|11|162|11|163|11|164|11|165|11|166|11|167|11|168|11|169|11|170|11|171|11|172|11|173|11|174|11|175|11|176|11|177|11|178|11|179|11|180|11|181|11|182|11|183|11|184|11|185|11|186|11|187|11|188|11|189|11|190|11|191|11|192|11|193|11|194|11|195|11|196|11|197|11|198|11|199|11|200|-140|11|201|11|202|11|203|11|204|11|205|11|206|11|207|11|208|11|209|11|210|11|211|11|212|11|213|11|214|11|215|11|216|11|217|11|218|11|219|11|220|11|221|11|222|11|223|-12|11|224|11|225|11|226|11|227|11|228|11|229|11|230|11|231|11|232|11|233|11|234|11|235|11|236|11|237|11|238|11|239|11|240|11|241|11|242|11|243|11|244|11|245|11|246|11|247|11|248|11|249|11|250|11|251|11|252|11|253|11|254|11|255|11|256|11|257|11|258|11|259|11|260|11|261|11|262|11|263|11|264|11|265|11|266|11|267|11|268|11|269|11|270|11|271|11|272|11|273|11|274|11|275|11|276|11|277|11|278|11|279|11|280|11|281|11|282|11|283|11|284|11|285|11|286|-262|11|287|11|288|11|289|11|290|11|291|11|292|11|293|11|294|11|295|11|296|11|297|11|298|11|299|11|300|11|301|11|302|11|303|11|304|11|305|11|306|11|307|11|308|11|309|11|310|11|311|11|312|11|313|-26|11|314|11|315|11|316|11|317|11|318|11|319|11|320|11|321|11|322|11|323|11|324|11|325|11|326|11|327|11|328|11|329|11|330|11|331|11|332|11|333|11|334|11|335|11|336|11|337|11|338|11|339|11|340|11|341|11|342|11|343|11|344|11|345|346|347|10|7|348|349|350|351|352|1|353|0|3|11|354|355|356|U4aA406|11|357|355|-347|11|358|359|360|0|361|18|11|362|-345|-348|11|363|11|364|11|365|-350|11|366|11|367|11|368|11|369|11|370|11|371|11|372|11|373|11|374|11|375|11|376|352|348|371|350|0|377|348|378|379|380|0|377|348|381|379|380|0|382|348|383|379|380|0|384|348|385|379|380|0|386|348|387|379|380|0|388|0|0|10|36|11|389|11|390|11|391|11|392|11|393|-387|11|394|11|395|11|396|-387|11|397|11|398|11|399|-387|11|400|-387|11|401|11|402|11|403|-387|11|404|11|405|11|406|-387|11|407|-387|11|408|11|409|11|410|-387|11|411|11|412|11|413|-387|11|414|-387|11|415|11|416|11|388|-409|11|417|-387|11|418|11|419|11|420|-387|11|421|11|422|11|423|11|424|11|425|-387|11|426|-387|11|427|-387|11|428|11|429|11|430|-409|11|431|11|432|11|433|-387|11|434|-387|11|435|-387|11|386|-409|11|436|-389|10|15|-388|437|0|0|0|438|380|1|439|10|4|11|440|11|441|11|442|-350|11|443|-388|11|444|11|352|445|1|0|0|10|0|-391|446|447|B|380|1|439|10|4|11|448|11|449|-443|-359|-444|-391|-445|-446|445|0|0|0|10|0|-394|450|451|389|380|2|439|10|4|-454|11|452|-443|-361|-444|-394|-445|-446|-456|439|10|5|11|453|-388|11|454|11|455|-443|-361|-444|-394|-445|-446|445|5|0|0|10|0|-398|456|0|457|0|6|458|0|0|0|380|1|439|10|4|-443|-358|-444|-398|-445|-446|11|459|11|460|445|6|0|0|10|0|-401|437|0|0|0|0|380|1|439|10|4|-441|11|461|-443|-365|-444|-401|-445|-446|-447|0|0|10|0|-405|462|463|1|0|353|0|0|0|0|380|0|0|0|10|0|-429|464|0|0|380|1|380|1|465|p|5073|353|0|0|0|0|380|1|439|10|4|-454|11|466|-443|-362|-444|-429|-445|-446|-456|0|0|10|0|-412|467|361|1|465|CB|5073|0|0|0|0|0|0|0|353|0|0|0|0|380|0|0|0|10|0|-426|468|353|0|0|0|469|470|471|0|0|0|472|0|1|0|0|473|0|474|475|0|476|0|477|478|479|480|481|482|380|1|483|0|0|0|0|0|0|0|354|0|484|485|486|0|0|484|487|0|488|489|0|490|0|0|1|0|491|380|1|492|358|493|494|0|495|0|496|0|380|3|497|476|0|477|498|479|0|482|380|1|483|0|0|0|0|0|0|0|499|500|501|482|380|1|483|0|0|0|0|0|0|0|502|380|1|503|0|504|505|0|380|0|506|507|508|509|510|511|512|0|0|0|513|0|391|0|294|497|0|380|1|503|0|514|505|0|380|0|0|0|0|0|515|0|394|0|127|497|0|380|1|503|0|516|0|0|517|0|518|483|0|0|0|0|0|0|0|381|0|428|0|488|0|353|0|0|0|10|0|519|380|1|520|377|377|380|0|380|0|10|0|521|522|487|0|380|0|1|0|10|0|-435|437|0|0|0|0|380|1|439|10|4|-441|-442|-443|-357|-444|-435|-445|-446|-447|0|0|10|0|-434|523|0|0|0|380|0|380|0|0|0|0|0|0|0|0|0|0|380|0|0|0|10|0|-414|523|0|0|0|380|0|380|0|0|0|0|0|0|0|0|0|0|380|0|0|0|10|0|-408|523|0|0|0|380|0|380|0|0|0|0|0|0|0|0|0|0|380|0|0|0|10|0|-428|523|0|0|0|380|0|380|0|0|0|0|0|0|0|0|0|0|380|0|0|0|10|0|-421|524|0|0|361|0|0|380|0|0|0|0|0|0|0|353|0|0|0|0|380|0|0|0|10|0|525|0|380|0|526|1|1|1|527|0|0|0|0|0|6|0|0|528|0|0|529|0|0|1|530|71|531|532|1|0|0|533|10|0|0|0|1|534|200|0|535|0|0|0|0|";

        String responceString =
                "//OK[87,86,85,41,84,41,83,41,82,41,81,41,80,41,79,41,78,41,77,41,76,41,75,41,74,41,73,41,72,41,71,41,70,41,69,41,68,41,67,41,66,41,65,41,64,41,63,41,62,41,61,41,60,41,59,41,58,41,57,41,56,41,55,41,54,41,53,41,52,41,51,41,50,41,49,41,48,41,47,41,46,41,45,41,44,41,43,41,42,41,44,16,40,39,0,38,28,37,36,0,35,28,34,33,0,32,28,31,30,1,29,28,4,16,0,27,0,26,25,24,17,23,22,21,17,20,19,18,17,3,16,15,14,0,0,13,12,11,10,0,9,-12,2,0,8,7,6,5,4,-2,2,0,3,500,2,1,[\"ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization/85760131\",\"java.lang.Integer/3438268394\",\"0.5.15-3\",\"RU\",\"dpro\",\"dpro@mail.ru\",\"Имя\",\"java.util.HashMap/1797211028\",\"help/page404.html\",\"Фамилия\",\"images/logo.png\",\"СЭД CM6Base: \",\"\",\"ru.intertrust.cm.core.config.SettingsPopupConfig/1472070380\",\"ru.intertrust.cm.core.config.LanguagesConfig/3080520641\",\"java.util.ArrayList/4159755760\",\"ru.intertrust.cm.core.config.LanguageConfig/3592934186\",\"Русский\",\"images/flags/Russian Federation.png\",\"ru\",\"English\",\"images/flags/United Kingdom(Great Britain).png\",\"en\",\"Українська\",\"images/flags/Ukraine.png\",\"ua\",\"ru.intertrust.cm.core.config.ThemesConfig/3159352096\",\"ru.intertrust.cm.core.config.ThemeConfig/3905352028\",\"default-theme\",\"Основная тема\",\"resources/common/images/default-theme-preview.png\",\"dark-theme\",\"Тёмная тема\",\"resources/common/images/dark-theme-preview.png\",\"light-theme\",\"Светлая тема\",\"resources/common/images/light-theme-preview.png\",\"lucem-theme\",\"Lucem тема\",\"resources/common/images/lucem-theme-preview.png\",\"java.lang.String/2004016611\",\"По умолчанию\",\"Локальная\",\"Оригинальная\",\"GMT-12\",\"GMT-11\",\"GMT-10\",\"GMT-9:30\",\"GMT-9\",\"GMT-8\",\"GMT-7\",\"GMT-6\",\"GMT-5\",\"GMT-4:30\",\"GMT-4\",\"GMT-3:30\",\"GMT-3\",\"GMT-2\",\"GMT-1\",\"GMT\",\"GMT+1\",\"GMT+2\",\"GMT+3\",\"GMT+3:07\",\"GMT+3:30\",\"GMT+4\",\"GMT+4:30\",\"GMT+5\",\"GMT+5:30\",\"GMT+5:45\",\"GMT+6\",\"GMT+6:30\",\"GMT+7\",\"GMT+8\",\"GMT+8:45\",\"GMT+9\",\"GMT+9:30\",\"GMT+10\",\"GMT+10:30\",\"GMT+11\",\"GMT+11:30\",\"GMT+12\",\"GMT+12:45\",\"GMT+13\",\"GMT+14\",\"ru.intertrust.cm.core.gui.model.PlainTextUserExtraInfo/911390105\",\"PostPersons\"],0,7";


        //Расшифровка запроса
        GwtRpcRequest request = GwtRpcRequest.decode(requestString, "http://localhost:8080");
        
        System.out.println(request.asString());
        System.out.println(request.encode("http://localhost:8090/cm-sochi/ru.intertrust.cm.core.gui.impl.BusinessUniverseEntryPoint", "054A665A66D6A7C42043EAEA61DC385B"));
        
        //Повторная расшифровка
        request = GwtRpcRequest.decode(request.encode("http://localhost:8090/cm-sochi/ru.intertrust.cm.core.gui.impl.BusinessUniverseEntryPoint", "054A665A66D6A7C42043EAEA61DC385B"));
        

        //Расшифровка ответа
        Object responceObj = GwtUtil.decodeResponce("http://localhost:8090/cm-sochi/ru.intertrust.cm.core.gui.impl.BusinessUniverseEntryPoint", "054A665A66D6A7C42043EAEA61DC385B", responceString, null);
        System.out.println(responceObj);
    }
}
