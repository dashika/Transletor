package dashika.cf.transletor;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import dashika.cf.transletor.Model.Russian;

/**
 * Created by dashika on 03/03/17.
 */

public class TransletorWidget extends AppWidgetProvider {

    final static String ACTION_OPEN_DIALOG = "dashika.cf.transletor.OpenServiceDialog";

    private Timer t;
    final String LOG_TAG = "myLogs";
    private static List<Russian> russianList;


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        russianList = Russian.getMy();
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
        if(russianList.size()<2)return;
        Russian rus = russianList.get(new Random().nextInt(russianList.size()-1));
        widgetView.setTextViewText(R.id.tvEng, rus.english.orth);
        widgetView.setTextViewText(R.id.tvRus, rus.quote);

        Intent intent = new Intent(context, ServiceDialog.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        widgetView.setOnClickPendingIntent(R.id.widget, pendingIntent);

      //  Intent configIntent = new Intent(context, TransletorWidget.class);
      //  configIntent.setAction(ACTION_OPEN_DIALOG);
      //  configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
       // PendingIntent pIntent = PendingIntent.getActivity(context, widgetId, configIntent, 0);
      //  widgetView.setOnClickPendingIntent(R.id.card_view, pIntent);
        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
    }

}
