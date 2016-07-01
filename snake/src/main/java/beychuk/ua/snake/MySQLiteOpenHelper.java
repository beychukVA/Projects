package beychuk.ua.snake;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Класс для работы с БД
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper
{

    public MySQLiteOpenHelper(Context context)
    {
        super(context, "DB_Scores", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String query = "CREATE TABLE Score(" +
                "_id integer not null primary key autoincrement," +
                "name varchar (64) not null," +
                "score integer not null" +
                ")";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

}
