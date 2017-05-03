package dashika.cf.transletor;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import dashika.cf.transletor.API.ITransletor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static javax.xml.transform.OutputKeys.ENCODING;

/**
 * Created by programer on 17.02.17.
 */
public class TransletorApplication extends Application {

    private static final String YANDEX_BASE_URL = "https://translate.yandex.net/";

    public static ITransletor getiTransletor() {
        return iTransletor;
    }

    private static ITransletor iTransletor;

    public static DatabaseReference getmDatabase() {
        return mDatabase;
    }

    private static DatabaseReference mDatabase;


    static String Path(String from, String to) throws UnsupportedEncodingException {
        return from + "-" + to;
    }

    public static FirebaseUser getUser() {
        return user;
    }

    public static void setUser(FirebaseUser user) {
        TransletorApplication.user = user;
    }

    private static FirebaseUser user;

    //

    @Override
    public void onCreate() {
        super.onCreate();


        mDatabase = FirebaseDatabase.getInstance().getReference();

        ActiveAndroid.initialize(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(YANDEX_BASE_URL)
                .addConverterFactory(
                        GsonConverterFactory.create(new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create()))
                .client(new OkHttpClient())
                .build();

        iTransletor = retrofit.create(ITransletor.class);

    }

}
