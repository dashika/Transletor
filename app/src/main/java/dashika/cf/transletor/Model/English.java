package dashika.cf.transletor.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by programer on 17.02.17.
 */
@Table(name = "English", id = "_id")
public class English extends Model {
    @Column(name = "orth", notNull = true)
    public String orth = "";
    @Column(name = "pron")
    public String pron = "";
    @Column(name = "fRussian", onDelete = Column.ForeignKeyAction.CASCADE)
    public Russian russian;

    public English(){
        super();
    }

    public static English getByOrth(String orth) {
        return new Select()
                .from(English.class)
                .where("orth=?", orth)
                .executeSingle();
    }


    public static List<English> find(String str) {
        return new Select()
                .from(English.class)
                .where("orth like ?", ('%'+str+'%'))
                .execute();
    }

    public List<Russian> fRussian() {
        return getMany(Russian.class, "fRussian");
    }
}
