package beychuk.ua.snake;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Окно рекордов
 */
public class RecordsActivity extends Activity
{
    private ArrayAdapter<Score> adapter;
    private ListView lvRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        //--- Обработчик кнопки "домой"
        Button btn_home = (Button) findViewById(R.id.btn_records_home);
        btn_home.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(RecordsActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        //--- Создаю адаптер ------
        this.adapter = new ArrayAdapter<Score>(this, R.layout.list_score, R.id.tvID, new ArrayList<Score>())
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = getLayoutInflater().inflate(R.layout.list_score, null);
                TextView tvID = (TextView) view.findViewById(R.id.tvID);
                TextView tvRes = (TextView) view.findViewById(R.id.tvRes);
                TextView tvName = (TextView) view.findViewById(R.id.tvName);

                Score score = RecordsActivity.this.adapter.getItem(position);

                tvID.setTextColor(Color.BLUE);
                tvID.setText(String.valueOf(position + 1) + ". ");

                tvName.setTextColor(Color.BLUE);
                tvName.setText(String.valueOf(score.name));

                tvRes.setTextColor(Color.BLUE);
                tvRes.setText(String.valueOf(score.score));

                return view;
            }
        };
        //-- Список
        this.lvRecords = (ListView) findViewById(R.id.lvRecords);
        this.lvRecords.setAdapter(this.adapter);
        //--- Загружаю список результатов ----
        this.loadRecordsIntoListView();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //--- Загружаю список результатов ----
        this.loadRecordsIntoListView();
    }

    /**
     * Метод загрузки товаров в ListView
     */
    public void loadRecordsIntoListView()
    {
        //Подключились для чтения
        SQLiteDatabase db = MainActivity.dbHelper.getReadableDatabase();
        //Извлекли товары
        Cursor cursor = db.query("Score", null, null, null, null, null, "score DESC");
        //Стаем на первую позицию
        if(cursor.moveToFirst())
        {
            //--- Получени индексов столбцов -----------------
            int indexID = cursor.getColumnIndex("_id");
            int indexName = cursor.getColumnIndex("name");
            int indexSCORE = cursor.getColumnIndex("score");

            //--- Очищаем адаптер от старых записей ------------
            this.adapter.clear();
            //--- Добавляем товары в адаптер данных ------------
            do
            {
                this.adapter.add(new Score(cursor.getInt(indexID), cursor.getString(indexName), cursor.getInt(indexSCORE)));
            }
            while (cursor.moveToNext());

            //---- Закрываем курсор -----------
            cursor.close();
        }
        else
        {
            Log.d("------", "Не удалосб позиционироваться на первую строчку курсора!");
        }
    }

    class Score
    {
        private String name;
        private int id;
        private int score;

        public Score(int id, String name, int score)
        {
            this.name = name;
            this.id = id;
            this.score = score;
        }

        @Override
        public String toString()
        {
            return this.id + ". " + this.name + " " + this.score;
        }
    }

    @Override
    public void onBackPressed() {}
}
