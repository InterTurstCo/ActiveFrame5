package ru.intertrust.cm.core.dao.api;

/**
 * Created by vmatsukevich on 9.3.15.
 */
public interface DatabaseInfo {

    public enum Vendor {ORACLE, POSTGRESQL}

    Vendor getDatabaseVendor();
}
