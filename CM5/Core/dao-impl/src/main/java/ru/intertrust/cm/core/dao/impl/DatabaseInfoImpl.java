package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.dao.api.DatabaseInfo;
import ru.intertrust.cm.core.model.FatalException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by vmatsukevich on 9.3.15.
 */
public class DatabaseInfoImpl implements DatabaseInfo {

    @Autowired
    private DataSource dataSource;

    public Vendor getDatabaseVendor() {
        try (final Connection connection = dataSource.getConnection()){
            DatabaseMetaData metaData = connection.getMetaData();
            String dbProductName = Case.toLower(metaData.getDatabaseProductName());

            if (dbProductName.contains("oracle")) {
                return Vendor.ORACLE;
            } else if (dbProductName.contains("postgresql")) {
                return Vendor.POSTGRESQL;
            } else {
                throw new FatalException("Application is running with unknown database vendor");
            }
        } catch (SQLException e) {
            throw new FatalException("Failed to define database vendor", e);
        }
    }
}
