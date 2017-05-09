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
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import dashika.cf.transletor.Model.Russian;
import dashika.cf.transletor.R;
import dashika.cf.transletor.TransletorApplication;


/**
 * Created by dashika on 17/12/16.
 */

public class InitDB extends AsyncTask<Void, Integer, Void> {

    List<Russian> result1 = new ArrayList<>();
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

        if (progress[0] == 100) {
            ActiveAndroid.beginTransaction();
            try {
                for (Russian russian : result1) {
                    russian.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();

            }

            mBuilder.setContentText(context.getString(R.string.download_complite))
                    .setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
            taskDelegateInitDB.GetResult(null);
        }
    }

    @Override
    protected void onPostExecute(Void result) {


    }

    @Override
    protected Void doInBackground(Void... voids) {
        GetWords(context.getString(R.string.admin), result -> {
                this.result1.addAll(result);
                publishProgress(50);


            if (!TransletorApplication.getUser().getUid().equals(context.getString(R.string.admin))) {
                GetWords(TransletorApplication.getUser().getUid(), result1 -> {
                    this.result1.addAll(result1);
                    publishProgress(100);
                });
            } else {
                publishProgress(100);
            }

        });


        return null;
    }

    private void GetWords(String key, TaskDelegateInitDB taskDelegateInitDB) {
        List<Russian> russianList = new ArrayList<>();
        TransletorApplication.getmDatabase().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    GsonBuilder builder = new GsonBuilder();
                    builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
                    Gson gson = builder.create();
                    String jsonString = gson.toJson(dataSnapshot.getValue());

                    try {
                        JSONObject str = new JSONObject(jsonString);
                        Russian obj = gson.fromJson(str.toString(), Russian.class);
                        if (obj.orth.isEmpty()) {
                            Russian objOld = gson.fromJson(str.get("english").toString(), Russian.class);
                            obj.orth = objOld.orth;
                            obj.pron = objOld.pron;
                        }
                        russianList.add(obj);
                    } catch (JSONException ignore) {
                        ignore.printStackTrace();
                        JSONArray jsonArray = new JSONArray(jsonString);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject str = jsonArray.getJSONObject(i);
                            Log.d("jsonObject", str.toString());
                            Russian obj = gson.fromJson(str.toString(), Russian.class);
                            if (obj.orth.isEmpty()) {
                                Russian objOld = gson.fromJson(jsonArray.getJSONObject(i).get("english").toString(), Russian.class);
                                obj.orth = objOld.orth;
                                obj.pron = objOld.pron;
                            }
                            russianList.add(obj);
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


    public void InsertRus(JSONObject cit) throws JSONException {
        Russian rus = new Russian();
        rus.quote = cit.getString("quote");
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
