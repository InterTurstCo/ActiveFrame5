package ru.intertrust.cm.core.dao.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

/**
 * @author vmatsukevich
 *         Date: 6/25/13
 *         Time: 2:39 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationDAOImplTest {
    @InjectMocks
    private final ConfigurationDAOImpl configurationDAO = new ConfigurationDAOImpl();
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void testSave() throws Exception {
        assertEquals(configurationDAO.generateSaveQuery(), ConfigurationDAOImpl.SAVE_QUERY);

        configurationDAO.save("test configuration string");
        verify(jdbcTemplate).update(anyString(), anyMap());
    }

    @Test
    public void testReadLastSavedConfiguration() throws Exception {
        assertEquals(configurationDAO.generateReadLastLoadedConfiguration(),
                ConfigurationDAOImpl.READ_LAST_SAVED_CONFIGURATION_QUERY);

        configurationDAO.readLastSavedConfiguration();
        verify(jdbcTemplate).queryForObject(anyString(), anyMap(), any(Class.class));
    }
}
