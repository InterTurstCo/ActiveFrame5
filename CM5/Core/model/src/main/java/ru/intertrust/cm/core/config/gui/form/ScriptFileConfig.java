package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name = "script-file")
public class ScriptFileConfig implements Dto {

    @Attribute(name = "url")
    private String url;


    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ScriptFileConfig that = (ScriptFileConfig) o;

      if (url != null ? !url.equals(that.url) : that.url != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return url != null ? url.hashCode() : 0;
    }
}
