package dashika.cf.transletor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;

import dashika.cf.transletor.Model.English;
import dashika.cf.transletor.Model.Russian;
import dashika.cf.transletor.Util.Language;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    private RecyclerView transletorList;
    private Paint paint = new Paint();
    private MainActivityPresenter mainActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainActivityPresenter = new MainActivityPresenter(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            mainActivityPresenter.AddOwnTranslate(MainActivity.this);
        });

        ((SearchView) findViewById(R.id.et_translation)).setOnQueryTextListener(this);
        transletorList = (RecyclerView) findViewById(R.id.transletor_list);
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
    }

    @Override
    public void onDestroy() {
        mainActivityPresenter.Destroy();
        super.onDestroy();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        boolean notCyrillic = query.charAt(0) < 1000;
        if (Online)
            mainActivityPresenter.callTranslator(MainActivity.this,query, notCyrillic ? Language.ENGLISH : Language.RUSSIAN, notCyrillic ? Language.RUSSIAN : Language.ENGLISH);
        else {
            try {
                if (notCyrillic) {
                    String res = English.getByOrth(query).russian.quote;
                    mainActivityPresenter.showDialog(MainActivity.this,Language.ENGLISH, query, res, false);
                } else {
                    String res = Russian.getByQuote(query).english.orth;
                    mainActivityPresenter.showDialog(MainActivity.this,Language.RUSSIAN, query, res, false);
                }
            } catch (NullPointerException e) {
                Snackbar.make(transletorList, R.string.nothing_found, Snackbar.LENGTH_LONG).show();
            }
        }
        return false;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        mainActivityPresenter.onQueryTextChange(query);
        transletorList.scrollToPosition(0);
        return true;
    }

    @Override
    public void networkUnavailable() {
        Online = false;
        Snackbar.make(transletorList, R.string.offline_mode, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void GoogleLogin() {
        LinearLayout progressBar = (LinearLayout) findViewById(R.id.progressBarLoader);
        mainActivityPresenter.InitDb(MainActivity.this, progressBar);
    }

}
