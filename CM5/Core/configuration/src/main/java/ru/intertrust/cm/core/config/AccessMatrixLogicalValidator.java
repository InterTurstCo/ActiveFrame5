package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 04.04.14
 *         Time: 12:57
 */
public class AccessMatrixLogicalValidator {

    private final ConfigurationExplorer configurationExplorer;

    private final static Logger logger = LoggerFactory.getLogger(AccessMatrixLogicalValidator.class);

    public AccessMatrixLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void validate() {
        List<LogicalErrors> validationLogicalErrors = new ArrayList<LogicalErrors>();
        Collection<AccessMatrixConfig> configList = configurationExplorer.getConfigs(AccessMatrixConfig.class);
        for (AccessMatrixConfig matrixConfig : configList) {
            LogicalErrors logicalErrors = LogicalErrors.getInstance(matrixConfig.getName(), "access-matrix");
            for (AccessMatrixStatusConfig accessMatrixStatus : matrixConfig.getStatus()) {
                for (BaseOperationPermitConfig permission : accessMatrixStatus.getPermissions()) {
                    if (ReadConfig.class.equals(permission.getClass())) {
                        if (Boolean.TRUE.equals(matrixConfig.isReadEverybody())) {
                            String error = "access-matrix configured with read-everybody=\"true\"" +
                                    " cannot include any <read> tags";
                            logicalErrors.addError(error);
                            logger.error(error);
                            break;
                        } else if (matrixConfig.isReadEverybody() != null  //that is, explicitly specified as "false"
                             && ((ReadConfig) permission).isPermitEverybody() != null)  {
                             String error = "Using read-everybody attribute of <access-matrix> tag together with " +
                                     "permit-everybody attribute of <read> tag is not allowed";
                             logicalErrors.addError(error);
                            break;
                        }
                    }
                }
            }
            if (logicalErrors.getErrorCount() > 0) {
                validationLogicalErrors.add(logicalErrors);
            }
        }
        if (!validationLogicalErrors.isEmpty()) {
            StringBuilder errorLogBuilder = new StringBuilder();
            for (LogicalErrors errors : validationLogicalErrors) {
                errorLogBuilder.append(errors.toString()).append("\n");
            }
            throw new ConfigurationException(errorLogBuilder.toString());
        }
    }


}
