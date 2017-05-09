package dashika.cf.transletor;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import dashika.cf.transletor.Model.Russian;
import dashika.cf.transletor.Util.Language;
import dashika.cf.transletor.Util.NetworkStateReceiver;

/**
 * Created by dashika on 03/03/17.
 */

public class ServiceDialog extends Activity implements NetworkStateReceiver.NetworkStateReceiverListener{

    private NetworkStateReceiver networkStateReceiver;
    protected boolean Online;

    private Dialog dialog;
    private RecyclerView transletorList;
    private Paint paint = new Paint();
    private MainActivityPresenter mainActivityPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkStateReceiver = new NetworkStateReceiver();

        View view = getLayoutInflater().inflate(R.layout.msg_dialog, null);

        mainActivityPresenter = new MainActivityPresenter(this);
        EditText editText = ((EditText) view.findViewById(R.id.et_translation));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                onQueryTextChange(editable.toString());
            }
        });
        ((ImageButton) view.findViewById(R.id.imageButtonSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onQueryTextSubmit(editText.getText().toString());
            }
        });
        transletorList = (RecyclerView) view.findViewById(R.id.transletor_list);
        transletorList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        transletorList.setLayoutManager(llm);
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        if (mainActivityPresenter.SwipeLeft(viewHolder.getAdapterPosition())) {
                            return makeMovementFlags(0, ItemTouchHelper.LEFT);
                        } else {
                            return makeMovementFlags(0, ItemTouchHelper.RIGHT);
                        }
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        mainActivityPresenter.Swipe(direction, viewHolder.getAdapterPosition());
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        Bitmap icon;
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                            View itemView = viewHolder.itemView;
                            float height = itemView.getBottom() - itemView.getTop();
                            float width = height / 3;

                            if (dX > 0) {
                                paint.setColor(getResources().getColor(R.color.colorAccent));
                                RectF background = new RectF(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom());
                                c.drawRect(background, paint);
                                icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_note_add_white_24dp);
                                RectF icon_dest = new RectF(itemView.getLeft() + width, itemView.getTop() + width, itemView.getLeft() + 2 * width, itemView.getBottom() - width);
                                c.drawBitmap(icon, null, icon_dest, paint);
                            } else {
                                paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
                                RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                                c.drawRect(background, paint);
                                icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_sweep_white_24dp);
                                RectF icon_dest = new RectF(itemView.getRight() - 2 * width, itemView.getTop() + width, itemView.getRight() - width, itemView.getBottom() - width);
                                c.drawBitmap(icon, null, icon_dest, paint);
                            }
                        }
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(transletorList);
        mainActivityPresenter.SetAdapter(transletorList);



        ImageButton exit = (ImageButton) view.findViewById(R.id.exit);
        exit.setOnClickListener(view1 -> {
            dialog.dismiss();
            onBackPressed();
        });

        (view.findViewById(R.id.title_dialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ServiceDialog.this, MainActivity.class));
            }
        });

        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        mainActivityPresenter.Destroy();
        super.onDestroy();
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    public boolean onQueryTextSubmit(String query) {
        boolean notCyrillic = query.charAt(0) < 1000;
        if (Online)
            mainActivityPresenter.callTranslator(ServiceDialog.this,query, notCyrillic ? Language.ENGLISH : Language.RUSSIAN, notCyrillic ? Language.RUSSIAN : Language.ENGLISH);
        else {
            try {
                if (notCyrillic) {
                    String res = Russian.getByOrth(query).quote;
                    mainActivityPresenter.showDialog(ServiceDialog.this,Language.ENGLISH, query, res, false);
                } else {
                    String res = Russian.getByQuote(query).orth;
                    mainActivityPresenter.showDialog(ServiceDialog.this,Language.RUSSIAN, query, res, false);
                }
            } catch (NullPointerException e) {
                Snackbar.make(transletorList, R.string.nothing_found, Snackbar.LENGTH_LONG).show();
            }
        }
        return false;
    }

    public boolean onQueryTextChange(String query) {
        mainActivityPresenter.onQueryTextChange(query);
        transletorList.scrollToPosition(0);
        return true;
    }

    @Override
    public void networkUnavailable() {
        Online = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (networkStateReceiver != null) {
            networkStateReceiver.removeListener(this);
            this.unregisterReceiver(networkStateReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void networkAvailable() {
        Online = true;
    }
}
