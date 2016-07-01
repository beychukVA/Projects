package beychuk.ua.snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Меню игры
 */
public class MenuActivity extends Activity
{
    private AlertDialog.Builder builder;
    public static AlertDialog dialog;
    private EditText etInputName;
    public static String PLAYER_NAME = "";
    private Button btnStart;
    private Button btnScore;
    private Button btnHelp;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //--- Кнопки главного меню
        btnStart = (Button)findViewById(R.id.btnStart);
        btnScore = (Button)findViewById(R.id.btnScore);
        btnHelp = (Button)findViewById(R.id.btnHelp);
        btnExit = (Button)findViewById(R.id.btnExit);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        btnScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, RecordsActivity.class);
                startActivity(intent);
            }
        });
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
//            System.runFinalizersOnExit(true);
                System.runFinalization();
                System.exit(0);
            }
        });

        //--- Кнопка Play/Pause sound menu
        Button btn_pay_pause = (Button) this.findViewById(R.id.btn_menu_sound_play_pause);
        btn_pay_pause.setBackgroundResource(MainActivity.BTN_IMAGE_ID);

        //--- Диалоговое окно ввода имени ----
        this.builder = new AlertDialog.Builder(this);
        this.builder.setCancelable(false);
        View viewName = getLayoutInflater().inflate(R.layout.dialog_name, null);
        this.etInputName = (EditText) viewName.findViewById(R.id.etInputName);
        Button btn_ok = (Button) viewName.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) viewName.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(etInputName.getText().toString().isEmpty())
                {
                    Toast.makeText(MenuActivity.this, "Введите имя!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    btnStart.setOnClickListener(null);
                    btnScore.setOnClickListener(null);
                    btnHelp.setOnClickListener(null);
                    btnExit.setOnClickListener(null);
                    MenuActivity.dialog.hide();
                    PLAYER_NAME = etInputName.getText().toString();
                    Intent intent = new Intent(MenuActivity.this, StartGameActivity.class);
                    startActivity(intent);
                    MainActivity.MEDIA_PLAYER.pause();
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MenuActivity.dialog.hide();
            }
        });
        this.builder.setView(viewName);
        dialog = builder.create();
    }

    public void btnClick(View view)
    {
        if(view.getId() == R.id.btnStart)
        {
            dialog.show();
        }
        else
        if(view.getId() == R.id.btnScore)
        {
            Intent intent = new Intent(MenuActivity.this, RecordsActivity.class);
            startActivity(intent);
        }
        else
        if(view.getId() == R.id.btnHelp)
        {
            Intent intent = new Intent(MenuActivity.this, HelpActivity.class);
            startActivity(intent);
        }
        else
        if (view.getId() == R.id.btnExit)
        {
            moveTaskToBack(true);
//            System.runFinalizersOnExit(true);
            System.runFinalization();
            System.exit(0);
        }
    }


    @Override
    public void onBackPressed() {}

    public void btnPlayMute(View view)
    {
        if(MainActivity.MEDIA_PLAYER.isPlaying())
        {
            MainActivity.MEDIA_PLAYER.pause();
            MainActivity.BTN_IMAGE_ID = R.drawable.sound_mute;
            MainActivity.PLAYER_IN_PAUSE_OR_PLAY = false;
        }
        else
        {
            MainActivity.MEDIA_PLAYER.start();
            MainActivity.BTN_IMAGE_ID = R.drawable.sound_play;
            MainActivity.PLAYER_IN_PAUSE_OR_PLAY = true;
        }
        view.setBackgroundResource(MainActivity.BTN_IMAGE_ID);
    }
}
