package ru.intertrust.cm.core.report;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Сервис подгрузги дополнительных шрифтов
 * @author larin
 * 
 */
public class FontLoader {
    private static final Logger logger = LoggerFactory.getLogger(FontLoader.class);

    @Autowired
    private ModuleService moduleService;

    @PostConstruct
    public void init() throws FontFormatException, IOException {
        List<ModuleConfiguration> modules = moduleService.getModuleList();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (ModuleConfiguration module : modules) {
            if (module.getImportReports() != null && module.getImportReports().getFontDir() != null) {
                for (String fontDir : module.getImportReports().getFontDir()) {
                    //Загрузка всех найденых шрифтов с расширением ttf включая те что расположены в подкаталогах
                    org.springframework.core.io.Resource[] resources = resolver.getResources(fontDir + "/**/*.ttf");
                    for (org.springframework.core.io.Resource resource : resources) {
                        Font font = Font.createFont(Font.TRUETYPE_FONT, resource.getInputStream());
                        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                        logger.info("Load font " + font.getName());
                    }
                }
            }
        }
    }
}
