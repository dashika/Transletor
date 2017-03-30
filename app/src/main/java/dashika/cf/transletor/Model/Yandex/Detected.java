
package dashika.cf.transletor.Model.Yandex;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Detected {

    @SerializedName("lang")
    @Expose
    private String lang;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
