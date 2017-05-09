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
@Table(name = "Transletor", id = "_id")
public class Russian extends Model {
    @Column(name = "quote", notNull = true)
    public String quote = "";
    @Column(name = "itsCustom")
    public boolean itsCustom;
    @Column(name = "orth", notNull = true)
    public String orth = "";
    @Column(name = "pron")
    public String pron = "";

    public static List<Russian> findEnglish(String str) {
        return new Select()
                .from(Russian.class)
                .where("orth like ?", ('%'+str+'%'))
                .execute();
    }

    public static Russian getByOrth(String orth) {
        return new Select()
                .from(Russian.class)
                .where("orth=?", orth)
                .executeSingle();
    }

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

    public static Russian getByQuote(String quote) {
        return new Select()
                .from(Russian.class)
                .where("Quote=?", quote)
                .executeSingle();
    }


    public static long count() {
        return new Select().from(Russian.class).count();
    }

}
