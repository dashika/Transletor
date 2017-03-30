package dashika.cf.transletor.Util;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dashika.cf.transletor.Model.English;
import dashika.cf.transletor.Model.Russian;
import dashika.cf.transletor.R;
import dashika.cf.transletor.TransletorApplication;


/**
 * Created by dashika on 17/12/16.
 */

public class InitDB extends AsyncTask<Void, Integer, Void> {

    private Context context;
    private static final String TAG = "Init DB";
    private int id = 1;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private TaskDelegateInitDB taskDelegateInitDB;

    public InitDB(Context context, TaskDelegateInitDB taskDelegateInitDB) {
        this.context = context;
        this.taskDelegateInitDB = taskDelegateInitDB;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mNotifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(context.getString(R.string.download_dictionary))
                .setContentText(context.getString(R.string.download_progress))
                .setSmallIcon(R.mipmap.ic_launcher);

    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mBuilder.setProgress(100, progress[0], false);
        mNotifyManager.notify(id, mBuilder.build());
        if (progress[0] > 50)
            taskDelegateInitDB.GetResult(null);

    }

    @Override
    protected void onPostExecute(Void result) {
    }

    @Override
    protected Void doInBackground(Void... voids) {
        GetWords(TransletorApplication.getUser().getUid(), result -> {
            ActiveAndroid.beginTransaction();
            try {
                for (Russian russian : result) {
                    russian.english.save();
                    russian.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
                publishProgress(50);

            }
            GetWords("default", result1 -> {
                ActiveAndroid.beginTransaction();
                try {
                    for (Russian russian : result1) {
                        russian.english.save();
                        russian.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                    publishProgress(100);
                    mBuilder.setContentText(context.getString(R.string.download_complite))
                            .setProgress(0, 0, false);
                    mNotifyManager.notify(id, mBuilder.build());

                }
            });

        });
        return null;
    }

    private void GetWords(String key, TaskDelegateInitDB taskDelegateInitDB) {
        List<Russian> russianList = new ArrayList<>();
        TransletorApplication.getmDatabase().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(dataSnapshot.getValue());

                    try {
                        JSONObject str = new JSONObject(jsonString);
                        Russian obj = gson.fromJson(str.toString(), Russian.class);
                        russianList.add(obj);
                    } catch (JSONException ignore) {
                        ignore.printStackTrace();
                        try {
                            JSONArray jsonArray = new JSONArray(jsonString);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject str = jsonArray.getJSONObject(i);
                                Log.d("jsonObject", str.toString());
                                Russian obj = gson.fromJson(str.toString(), Russian.class);
                                russianList.add(obj);
                            }
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    taskDelegateInitDB.GetResult(russianList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void InsertRus(JSONObject cit, English eng) throws JSONException {
        Russian rus = new Russian();
        rus.quote = cit.getString("quote");
        rus.english = eng;
        try {
            rus.save();
            Log.v(TAG, "Insert rus " + rus.quote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // emulate download offline dictionary
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
