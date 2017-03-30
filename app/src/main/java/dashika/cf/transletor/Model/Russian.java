package dashika.cf.transletor.Model;

import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by programer on 17.02.17.
 */
@Table(name = "Russian", id = "_id")
public class Russian extends Model {
    @Column(name = "fEnglish", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public English english;
    @Column(name = "Quote", notNull = true)
    public String quote = "";
    @Column(name = "itsCustom")
    public boolean itsCustom;

    public Russian() {
        super();
    }

    public static List<Russian> getAll() {
        return new Select()
                .all()
                .from(Russian.class)
                .execute();
    }

    public static List<Russian> getMy() {
        return new Select()
                .from(Russian.class)
                .where("itsCustom=?", true)
                .execute();
    }

    public static List<Russian> find(String str) {
        return new Select()
                .from(Russian.class)
                .where("Quote LIKE ?", ('%' + str + '%'))
                .execute();
    }

    public static List<Russian> findEng(String str) {
        try {
            List<English> englishes = English.find(str);
            String[] strings = new String[englishes.size()];
            int ie = 0;
            for (English english : englishes) {
                strings[ie++] = english.getId().toString();
            }
            Character[] placeholdersArray = new Character[englishes.size()];
            for (int i = 0; i < englishes.size(); i++) {
                placeholdersArray[i] = '?';
            }

            String placeholders = TextUtils.join(",", placeholdersArray);


            return new Select()
                    .from(Russian.class)
                    .where("fEnglish in (" + placeholders + ")", strings)
                    .execute();
        } catch (Exception e) {
            return getMy();
        }
    }

    public static Russian getByQuote(String quote) {
        return new Select()
                .from(Russian.class)
                .where("Quote=?", quote)
                .executeSingle();
    }


    public static long count() {
        return new Select().from(Russian.class).count();
    }


    public List<English> fEnglish() {
        return getMany(English.class, "fEnglish");
    }
}
