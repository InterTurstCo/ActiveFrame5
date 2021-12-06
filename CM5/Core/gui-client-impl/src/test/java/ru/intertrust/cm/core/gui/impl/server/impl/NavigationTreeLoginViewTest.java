package ru.intertrust.cm.core.gui.impl.server.impl;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePluginView;

import static org.junit.Assert.assertEquals;

public class NavigationTreeLoginViewTest {

    @Test
    public void testCreateLoginUrl(){
        NavigationTreePluginView view = (NavigationTreePluginView)new NavigationTreePlugin().createView();

        // Штатный адрес с app
        String loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "http://localhost:8080/cm/app/xxx/BusinessUniverse.html");
        assertEquals(loginPath, "http://localhost:8080/cm/Login.html");

        // Штатный адрес без app
        loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "http://localhost:8080/cm/BusinessUniverse.html");
        assertEquals(loginPath, "http://localhost:8080/cm/Login.html");

        // Штатный адрес без порта
        loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "http://localhost/cm/BusinessUniverse.html");
        assertEquals(loginPath, "http://localhost/cm/Login.html");

        // В имени сервера есть имя контекста CMFIVE-35311
        loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "http://sdoagm101lv:8080/agm/BusinessUniverse.html");
        assertEquals(loginPath, "http://sdoagm101lv:8080/agm/Login.html");

        // То же самое но https
        loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "https://sdoagm101lv:8080/agm/BusinessUniverse.html");
        assertEquals(loginPath, "https://sdoagm101lv:8080/agm/Login.html");

        // Не возможно распарсить, возвращать должно исходный URL
        loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "xxxxx://sdoagm101lv:8080/agm/BusinessUniverse.html");
        assertEquals(loginPath, "xxxxx://sdoagm101lv:8080/agm/BusinessUniverse.html");

        // Регистр не должен измениться
        loginPath = ReflectionTestUtils.invokeMethod(view, "getLoginPath", "HTTP://SdoAgm101lv:8080/Agm/BusinessUniverse.html");
        assertEquals(loginPath, "HTTP://SdoAgm101lv:8080/Agm/Login.html");
    }
}
