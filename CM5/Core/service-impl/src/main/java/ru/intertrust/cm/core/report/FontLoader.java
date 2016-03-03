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
import org.springframework.core.io.Resource;
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
    public void init() {
        List<ModuleConfiguration> modules = moduleService.getModuleList();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (ModuleConfiguration module : modules) {
            if (module.getImportReports() != null && module.getImportReports().getFontDir() != null) {
                for (String fontDir : module.getImportReports().getFontDir()) {
                    //Загрузка всех найденых шрифтов с расширением ttf включая те что расположены в подкаталогах
                    Resource[] resources;
                    try {
                        resources = resolver.getResources(fontDir + "/**/*.ttf");
                    } catch (IOException e) {
                        logger.error("Error opening font directory " + fontDir + ": " + e.getMessage());
                        continue;
                    }
                    for (Resource resource : resources) {
                        try {
                            Font font = Font.createFont(Font.TRUETYPE_FONT, resource.getInputStream());
                            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                            logger.info("Load font " + font.getName());
                        } catch (FontFormatException | IOException e) {
                            logger.error("Error loading font " + resource.getFilename() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
