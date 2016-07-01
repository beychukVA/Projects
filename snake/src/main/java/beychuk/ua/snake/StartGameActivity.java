package beychuk.ua.snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Victor on 09.06.2016.
 */
public class StartGameActivity extends Activity
{
    private Game game;
    private float xDown = 0, yDown = 0, xMove, yMove;
    private ImageButton imgPause;
    private AlertDialog dialog_pause;
    private AlertDialog.Builder builder_pause;
    public static AlertDialog dialog_game_over;
    private AlertDialog.Builder builder_game_over;
    public static TextView tvResult;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--- Кнопка Play/Pause sound menu
        Button btn_pay_pause = (Button) this.findViewById(R.id.btn_game_play_or_mute);
        btn_pay_pause.setBackgroundResource(MainActivity.GAME_BTN_IMAGE);

        //--- Диалоговое окно pause
        this.builder_pause = new AlertDialog.Builder(this);
        View viewPause = getLayoutInflater().inflate(R.layout.dialog_pause, null);
        Button btn_pause_play = (Button) viewPause.findViewById(R.id.btn_dialog_pause_play);
        Button btn_pause_restart = (Button) viewPause.findViewById(R.id.btn_dialog_pause_restart);
        Button btn_pause_home = (Button) viewPause.findViewById(R.id.btn_dialog_pause_home);
        btn_pause_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartGameActivity.this.dialog_pause.hide();
                game.GameProcess.pauseOff();
            }
        });
        btn_pause_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("ua.beychuk.snake.START_GAME"));
            }
        });
        btn_pause_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("ua.beychuk.snake.MENU_GAME"));
                if(MainActivity.PLAYER_IN_PAUSE_OR_PLAY)
                {
                    MainActivity.MEDIA_PLAYER.start();
                }
            }
        });
        this.builder_pause.setView(viewPause);
        this.builder_pause.setCancelable(false);
        this.dialog_pause = this.builder_pause.create();

        game = (Game) this.findViewById(R.id.vGame);
        game.activity = this;

        //--- Нахожу кнопку "пауза" и назначаю ей слушателя
        this.imgPause = (ImageButton)this.findViewById(R.id.btnPause);
        this.imgPause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (game.GameProcess.isPause == false)
                {
                    game.GameProcess.pauseOn();
                    StartGameActivity.this.dialog_pause.show();
                }
            }
        });

//       //--- Диалоговое окно game over
        this.builder_game_over = new AlertDialog.Builder(this);
        View viewGameOver = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        Button btn_game_over_restart = (Button) viewGameOver.findViewById(R.id.btn_dialog_game_over_restart);
        Button btn_game_over_home = (Button) viewGameOver.findViewById(R.id.btn_dialog_game_over_home);
        this.tvResult = (TextView) viewGameOver.findViewById(R.id.tvResult);
        btn_game_over_restart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent("ua.beychuk.snake.START_GAME"));
            }
        });
        btn_game_over_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("ua.beychuk.snake.MENU_GAME"));
                if(MainActivity.PLAYER_IN_PAUSE_OR_PLAY)
                {
                    MainActivity.MEDIA_PLAYER.start();
                }
            }
        });
        this.builder_game_over.setView(viewGameOver);
        this.builder_game_over.setCancelable(false);
        this.dialog_game_over = this.builder_game_over.create();

        //УПРАВЛЕНИЕ КНОПКАМИ
        game.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //получаю координаты касания
                        xDown = event.getX();
                        yDown = event.getY();
                        Log.d("-----", "ACTION_DOWN");
                        Log.d("-----", "xDown = " + xDown);
                        Log.d("-----", "yDown = " + yDown);
                        if (xDown > game.rectLeft.left && xDown < game.rectLeft.right &&
                                yDown > game.rectLeft.top && yDown < game.rectLeft.bottom) {
                            game.rectF_Left_alpha += 50;
                        }
                        //Вправо
                        else if (xDown > game.rectRight.left && xDown < game.rectRight.right &&
                                yDown > game.rectRight.top && yDown < game.rectRight.bottom) {
                            game.rectF_Right_alpha += 50;
                        }
                        //Вниз
                        else if (xDown > game.rectDown.left && xDown < game.rectDown.right &&
                                yDown > game.rectDown.top && yDown < game.rectDown.bottom) {
                            game.rectF_Down_alpha += 50;
                        }
                        //Вверх
                        else if (xDown > game.rectUp.left && xDown < game.rectUp.right &&
                                yDown > game.rectUp.top && yDown < game.rectUp.bottom) {
                            game.rectF_Up_alpha += 50;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //Влево
                        if (xDown > game.rectLeft.left && xDown < game.rectLeft.right &&
                                yDown > game.rectLeft.top && yDown < game.rectLeft.bottom) {
                            if (game.snake.direction != Snake.MOVE_RIGHT)
                                game.snake.direction = Snake.MOVE_LEFT;
                            game.rectF_Left_alpha -= 50;
                        }
                        //Вправо
                        else if (xDown > game.rectRight.left && xDown < game.rectRight.right &&
                                yDown > game.rectRight.top && yDown < game.rectRight.bottom) {
                            if (game.snake.direction != Snake.MOVE_LEFT)
                                game.snake.direction = Snake.MOVE_RIGHT;
                            game.rectF_Right_alpha -= 50;
                        }
                        //Вниз
                        else if (xDown > game.rectDown.left && xDown < game.rectDown.right &&
                                yDown > game.rectDown.top && yDown < game.rectDown.bottom) {
                            if (game.snake.direction != Snake.MOVE_UP)
                                game.snake.direction = Snake.MOVE_DOWN;
                            game.rectF_Down_alpha -= 50;
                        }
                        //Вверх
                        else if (xDown > game.rectUp.left && xDown < game.rectUp.right &&
                                yDown > game.rectUp.top && yDown < game.rectUp.bottom) {
                            if (game.snake.direction != Snake.MOVE_DOWN)
                                game.snake.direction = Snake.MOVE_UP;
                            game.rectF_Up_alpha -= 50;
                        }
                        break;
                }
                return true;
            }
        });


        //УПРАВЛЕНИЕ ЖЕСТАМИ
//        game.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        //получаю координаты касания
//                        xDown = event.getX();
//                        yDown = event.getY();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        //координаты в момент движения
//                        xMove = event.getX();
//                        yMove = event.getY();
//                        //Движение по Х или У
//                        if (Math.abs((xMove - xDown)) > Math.abs((yMove - yDown))) {
//                            //влево
//                            if (xMove < xDown) {
//                                if (MainActivity.this.game.snake.direction != Snake.MOVE_RIGHT)
//                                    MainActivity.this.game.snake.direction = Snake.MOVE_LEFT;
//                            }
//                            //вправо
//                            else {
//                                if (MainActivity.this.game.snake.direction != Snake.MOVE_LEFT)
//                                    MainActivity.this.game.snake.direction = Snake.MOVE_RIGHT;
//                            }
//                        }
//                        //Движение вверх/вниз
//                        else {
//                            //Вверх
//                            if (yMove < yDown) {
//                                if (MainActivity.this.game.snake.direction != Snake.MOVE_DOWN)
//                                    MainActivity.this.game.snake.direction = Snake.MOVE_UP;
//                            }
//                            //Вниз
//                            else {
//                                if (MainActivity.this.game.snake.direction != Snake.MOVE_UP)
//                                    MainActivity.this.game.snake.direction = Snake.MOVE_DOWN;
//                            }
//                        }
//                }
//                return true;
//            }
//        });
    }

    @Override
    public void onBackPressed() {}

    public void btnGameMuteOrPlay(View view)
    {
        if(this.game.playerGamePlay.isPlaying())
        {
            this.game.playerGamePlay.pause();
            MainActivity.GAME_BTN_IMAGE = R.drawable.sound_mute;
            MainActivity.GAME_SOUND_IN_PAUSE_OR_PLAY = false;
        }
        else
        {
            this.game.playerGamePlay.start();
            MainActivity.GAME_BTN_IMAGE = R.drawable.sound_play;
            MainActivity.GAME_SOUND_IN_PAUSE_OR_PLAY = true;
        }
        view.setBackgroundResource(MainActivity.GAME_BTN_IMAGE);
    }
}
