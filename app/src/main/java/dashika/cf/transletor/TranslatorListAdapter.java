package dashika.cf.transletor;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import dashika.cf.transletor.Model.Russian;

/**
 * Created by dashika on 17/12/16.
 */

public class TranslatorListAdapter extends RecyclerView.Adapter<TranslatorListAdapter.WordViewHolder> {

    private List<Russian> words;
    private TextToSpeech textToSpeech;

    TranslatorListAdapter(Context context, List<Russian> words, TextToSpeech textToSpeech) {
        this.words = words;
        this.textToSpeech = textToSpeech;
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        WordViewHolder pvh = new WordViewHolder(v);

        return pvh;
    }

    public Russian retrieveContact(int position) {
        return words.get(position);
    }


    @Override
    public void onBindViewHolder(WordViewHolder personViewHolder, int i) {
        personViewHolder.textViewEng.setText(words.get(i).english.orth);
        personViewHolder.textViewRus.setText(words.get(i).quote);
        personViewHolder.itemView.setTag(words.get(i).getId());
        personViewHolder.textViewEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    textToSpeech.speak(((TextView) view).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void animateTo(List<Russian> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Russian> newModels) {
        for (int i = words.size() - 1; i >= 0; i--) {
            final Russian model = words.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Russian> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Russian model = newModels.get(i);
            if (!words.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Russian> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Russian model = newModels.get(toPosition);
            final int fromPosition = words.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }


    public Russian removeItem(int position) {
        final Russian model = words.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Russian model) {
        words.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Russian model = words.remove(fromPosition);
        words.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }


    public static class WordViewHolder extends RecyclerView.ViewHolder {

        TextView textViewEng, textViewRus;

        WordViewHolder(View itemView) {
            super(itemView);
            textViewEng = (TextView) itemView.findViewById(R.id.tw_word);
            textViewRus = (TextView) itemView.findViewById(R.id.tw_word_rus);
        }
    }
}
