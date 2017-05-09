package dashika.cf.transletor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;

import java.util.ArrayList;
import java.util.List;

import dashika.cf.transletor.Model.Russian;
import dashika.cf.transletor.Model.Yandex.Example;
import dashika.cf.transletor.Util.ApiKeys;
import dashika.cf.transletor.Util.InitDB;
import dashika.cf.transletor.Util.Language;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by dashika on 03/03/17.
 */

class MainActivityPresenter {

    private static List<Russian> recently, duble;
    private InitDB initDB;
    private Context context;

    private TranslatorListAdapter adapter;

    private TextToSpeech textToSpeech;

    MainActivityPresenter(Context context) {
        this.context = context;
        recently = Russian.getMy();
     //   if (duble == null)
     //       duble = Russian.getAll();
        textToSpeech = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(context, R.string.textspeechnotsupport, Toast.LENGTH_SHORT).show();
            }
        });
        adapter = new TranslatorListAdapter(context, recently, textToSpeech);
    }

    void InitDb(Activity activity, LinearLayout progressBar) {
        //if DB is empty - that's mean first run - we need init offline DB
        if (Russian.count() < (2286+140)) {
            if (initDB == null) {
                progressBar.setVisibility(View.VISIBLE);
                initDB = new InitDB(activity, result -> {
                    activity.runOnUiThread(() -> {
                        ActiveAndroid.beginTransaction();
                        try {
                            for (Russian russian : result) {

                                russian.save();

                            }
                            ActiveAndroid.setTransactionSuccessful();
                        } finally {
                            ActiveAndroid.endTransaction();

                        }
                        onQueryTextChange("");
                    });
                    progressBar.setVisibility(View.GONE);
                });
                initDB.execute();
            }
        }
    }

    void AddOwnTranslate(Activity activity) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog, null);
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setTitle(R.string.add_new_translate);
        final EditText et_eng = (EditText) view.findViewById(R.id.et_eng);
        final EditText et_rus = (EditText) view.findViewById(R.id.et_rus);
        Button btn_positive = (Button) view.findViewById(R.id.btn_positive);
        Button btn_negative = (Button) view.findViewById(R.id.btn_negative);

        dialog.show();
        btn_negative.setOnClickListener(view1 -> dialog.dismiss());

        btn_positive.setOnClickListener(view12 -> {
            String ceng = et_eng.getText().toString();
            String crus = et_rus.getText().toString();
            if (ceng.isEmpty()) {
                et_eng.setError(activity.getString(R.string.empry_value));
                return;
            } else if (ceng.charAt(0) > 1000) {
                et_eng.setError(activity.getString(R.string.must_eng));
                return;
            }
            if (crus.isEmpty()) {
                et_rus.setError(activity.getString(R.string.empry_value));
                return;
            } else if (crus.charAt(0) < 1000) {
                et_rus.setError(activity.getString(R.string.must_rus));
                return;
            }
            Russian rus = new Russian();
            rus.quote = crus;
            rus.orth = ceng;
            rus.itsCustom = true;
            rus.save();
            recently.add(0, rus);
            adapter.notifyDataSetChanged();
            SaveRussianTranslate();
            dialog.dismiss();
        });
    }

    void Swipe(int direction, int pos) {
        Russian rus = adapter.retrieveContact(pos);
        if (direction == ItemTouchHelper.LEFT) {
            rus.itsCustom = false;
            rus.save();
            recently.remove(rus);
            adapter.animateTo(recently);
        } else {
            rus.itsCustom = true;
            rus.save();
            recently.add(rus);
            adapter.animateTo(recently);
        }
        adapter.notifyDataSetChanged();
        SaveRussianTranslate();
    }

    private void SaveRussianTranslate() {
        try {
            TransletorApplication.getmDatabase().child(TransletorApplication.getUser().getUid()).setValue(Russian.getMy());
        } catch (NullPointerException ignore) {

        }
    }

    void showDialog(Activity activity, final Language from, final String text, final String result, final boolean itsOnline) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_result, null);
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle(R.string.filter_title);
        ((TextView)view.findViewById(R.id.et_result)).setText(result);
        dialog.setContentView(view);
        (view.findViewById(R.id.btn_positive)).setOnClickListener(view1 -> {
            Russian rus = new Russian();
            if (itsOnline) {
                if (from.equals(Language.ENGLISH)) {
                    rus.orth = text;
                    rus.quote = result;
                } else {
                    rus.orth = result;
                    rus.quote = text;
                }
            } else {
                if (from.equals(Language.ENGLISH)) {
                    rus = Russian.getByQuote(result);
                } else {
                    rus = Russian.getByOrth(text);
                }
            }
            rus.itsCustom = true;
            rus.save();
            recently.add(0, rus);
            SaveRussianTranslate();
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        (view.findViewById(R.id.btn_negative)).setOnClickListener(view12 -> dialog.dismiss());

        dialog.show();
    }

    void Destroy() {
        textToSpeech.shutdown();
    }

    boolean onQueryTextChange(String query) {
        final List<Russian> filteredModelList = filter(query);
        if (adapter == null) return false;
        if (filteredModelList == null) return false;
        adapter.animateTo(filteredModelList);

        return true;
    }

    private List<Russian> filter(String query) {
        if (query.isEmpty()) {
            return recently = Russian.getMy();
        } else {
            query = query.toLowerCase();
            //List<Russian> recentlyList = duble;
            List<Russian> filteredModelList = new ArrayList<>();

                if (query.charAt(0) <= Byte.MAX_VALUE)
                    filteredModelList = Russian.findEnglish(query);
            else filteredModelList = Russian.find(query);



            return filteredModelList;
        }
    }

    void callTranslator(Activity activity, final String text, final Language from, Language to) {
        try {
            String opt = TransletorApplication.Path(from.toString(), to.toString());
            TransletorApplication.getiTransletor().getData(ApiKeys.YANDEX_API_KEY, text, opt).enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Call<Example> call, Response<Example> response) {
                    showDialog(activity, from, text, response.body().getText().get(0), true);
                }

                @Override
                public void onFailure(Call<Example> call, Throwable t) {
                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    boolean SwipeLeft(int pos) {
        return adapter.retrieveContact(pos).itsCustom;
    }

    void SetAdapter(RecyclerView recyclerView) {
        if (recyclerView == null || adapter == null) return;
        recyclerView.setAdapter(adapter);
    }

}
