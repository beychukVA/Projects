package beychuk.ua.snake;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener
{

    /**
     * БД
     */
    public static MySQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    /**
     * Музыка в главном меню
     */
    public static MediaPlayer MEDIA_PLAYER;
    public static int BTN_IMAGE_ID = R.drawable.sound_play;
    public static boolean PLAYER_IN_PAUSE_OR_PLAY = true;
    public static boolean GAME_SOUND_IN_PAUSE_OR_PLAY = true;
    public static int GAME_BTN_IMAGE = R.drawable.sound_play;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        //чтобы небыло заголовка активити
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //убрать статусную строку
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        //--- Подключаюсь к БД ------------
        this.dbHelper = new MySQLiteOpenHelper(this);
        this.db = this.dbHelper.getWritableDatabase();

        //================ ЗВУКИ =========================================
        MEDIA_PLAYER = MediaPlayer.create(this, R.raw.skyrim);
        MEDIA_PLAYER.setLooping(true);
        //================================================================

        //--- Отобразаю начальный экран (заставку) 5 секунд
        Thread splash = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    int timer = 0;
                    while (timer < 5000)
                    {
                        sleep(100);
                        timer += 100;
                    }
                    //--- по истечении 5 сек перехожу на экран "Меню игры"
                    startActivity(new Intent("ua.beychuk.snake.MENU_GAME"));
                    MEDIA_PLAYER.start();
                }
                catch (InterruptedException ie)
                {
                    Log.d("---", ie.getMessage());
                }
            }
        };
        splash.start();

    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        MEDIA_PLAYER.start();
    }

    @Override
    public void onBackPressed() {}
}
