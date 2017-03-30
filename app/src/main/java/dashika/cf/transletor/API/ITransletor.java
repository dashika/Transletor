package dashika.cf.transletor.API;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import dashika.cf.transletor.Model.Yandex.Example;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static javax.xml.transform.OutputKeys.ENCODING;

/**
 * Created by programer on 17.02.17.
 */

public interface ITransletor {


    @GET("/api/v1.5/tr.json/translate")
    Call<Example> getData(
            @Query("key") String key,
            @Query("text") String text,
            @Query("lang") String lang);

}
