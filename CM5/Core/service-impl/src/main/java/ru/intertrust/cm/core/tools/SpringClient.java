package ru.intertrust.cm.core.tools;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.util.SpringApplicationContext;


/**
 * Базовый класс для сервисных классов, не являющихся по своей природе spring бинами (например классы в workflow)
 * и создаются не спринг движком. Наследование от данного класса позволит наследникам использовать конструкцию  @Autowired
 * @author larin
 *
 */
public class SpringClient {
    /**
     * Конструктор по умолчанию.
     * @throws SpringClient
     */
    public SpringClient() {
        initAutowire(this);
    }

    public static void initAutowire(Object obj){
        ApplicationContext ctx = SpringApplicationContext.getContext();
        ctx.getAutowireCapableBeanFactory().autowireBeanProperties(obj, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                false);

    }

}
