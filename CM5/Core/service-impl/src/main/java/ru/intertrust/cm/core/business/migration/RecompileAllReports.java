package ru.intertrust.cm.core.business.migration;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.dao.api.Migrator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

/**
 * Миграционный компонент для перекомпиляции всех отчетов. Нужен при смене версии JasperReports.
 * @author larin
 *
 */
@ServerComponent(name = "RecompileAllReports")
public class RecompileAllReports implements Migrator{

    @Autowired
    private ReportServiceAdmin reportServiceAdmin;
    
    @Override
    public void execute() {
        reportServiceAdmin.recompileAll();        
    }

}
