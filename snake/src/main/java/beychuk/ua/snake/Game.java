package beychuk.ua.snake;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import beychuk.ua.snake.constructor.Map;

public class Game extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener
{
    private Bundle bundle;
    private static int CURRENT_SNAKE_HEAD_X = 1;
    private static int CURRENT_SNAKE_HEAD_Y = 10;
    private static int CURRENT_SNAKE_BODY_X = 1;
    private static int CURRENT_SNAKE_BODY_Y = 11;
    public String COUNT_RECT_HEIGHT;
    public String COUNT_RECT_WIDTH;
    public static final float MODE_LDPI = 0.75f;
    public static final float MODE_MDPI = 1.0f;
    public static final float MODE_HDPI = 1.5f;
    public static final float MODE_XHDPI = 2.0f;
    public static final float MODE_XHDPI_3 = 3.0f;
    public static final float MODE_DEFAULT = 1.0f;
    public static String TAG = "<-- DEBUG-->";
    public Activity activity;
    public TextView tvScore;
    public TextView tvApple;
    public Activity recordActivity;
    public ImageButton imgPause;
    public int SCORE = 0;
    public int APPLE = 0;
    public GameProcess GameProcess;
    public int current_width;
    public int current_height;
    public int SCALE = 19;  //Размер ячейки изначально
    public int DEFAULT_SCALE = 19;
    public static int COUNT_RECT = 20;
    public Snake snake;
    private static int SPEED = 400;
    private Apple apple;
    //Кнопки на дисплее
    public RectF rectLeft;
    public RectF rectRight;
    public RectF rectUp;
    public RectF rectDown;
    public int button_alpha = 100;
    public int rectF_Left_alpha = 50;
    public int rectF_Right_alpha = 50;
    public int rectF_Up_alpha = 50;
    public int rectF_Down_alpha = 50;
    private Bitmap wall_vertical = BitmapFactory.decodeResource(this.getResources(), R.drawable.kladka_vertical);
    private Bitmap wall_horizontal = BitmapFactory.decodeResource(this.getResources(), R.drawable.kladka_horizontal);
    private Bitmap apple_red = BitmapFactory.decodeResource(this.getResources(), R.drawable.apple);
    private Bitmap kirpich = BitmapFactory.decodeResource(this.getResources(), R.drawable.kirpich);
    private Bitmap arrow_left = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow_left);
    private Bitmap arrow_right = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow_right);
    private Bitmap arrow_up = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow_up);
    private Bitmap arrow_down = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow_down);
    private Bitmap one = BitmapFactory.decodeResource(this.getResources(), R.drawable.one);
    private Bitmap two = BitmapFactory.decodeResource(this.getResources(), R.drawable.two);
    private Bitmap three = BitmapFactory.decodeResource(this.getResources(), R.drawable.three);
    private Bitmap go = BitmapFactory.decodeResource(this.getResources(), R.drawable.go);
    private List<Bitmap> arrBitmap;
    public MediaPlayer playerGamePlay;
    public MediaPlayer playerGameOver;
    public Map map;
    public boolean gameIsStarted = false;
    public int[][] MAP_RANDOM;

    /*
        Регистрируюсб у SurfaceHolder-а на получение событий
     */
    {
        this.getHolder().addCallback(this);
    }

    public Game(Context context)
    {
        super(context);
    }

    public Game(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public Game(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        //фоновая музыка
        this.playerGamePlay = MediaPlayer.create(this.getContext(), R.raw.sound_game_play);
        this.playerGamePlay.setLooping(true);
        this.playerGamePlay.setOnCompletionListener(this);
        if(MainActivity.GAME_SOUND_IN_PAUSE_OR_PLAY)
        {
            this.playerGamePlay.start();
        }
        //музыка Game Over
        this.playerGameOver = MediaPlayer.create(this.getContext(), R.raw.sound_game_over);

        //Получаю характеристики экрана (узнаю какой режим включен)
        Display display = this.getDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Log.d(TAG, " DPI: " + metrics.densityDpi);
        Log.d(TAG, " xdpi: " + metrics.xdpi);
        Log.d(TAG, " ydpi: " + metrics.ydpi);
        Log.d(TAG, " density: " + metrics.density);
        Log.d(TAG, " densityDpi: " + metrics.densityDpi);
        Log.d(TAG, " densityDpi: " + metrics.scaledDensity);
        Log.d(TAG, " heightPixels: " + metrics.heightPixels);
        Log.d(TAG, " widthPixels: " + metrics.widthPixels);

        //проверяю если режим экрана больше дефолтного (1.0f)
        //корректирую размер ячейки в зависимости от текущего режима
        //увеличиваю размер ячейки (количество пикселей)
        if(metrics.density > MODE_DEFAULT)
        {
            //0.75
            if(metrics.density == MODE_LDPI)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_LDPI);
                Log.d(TAG, "SCALE = " + this.SCALE);
            }
            //1.5
            else if(metrics.density == MODE_HDPI)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_HDPI);
                Log.d(TAG, "SCALE = " + this.SCALE);
            }
            //2.0
            else if(metrics.density == MODE_XHDPI)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_XHDPI);
                Log.d(TAG, "SCALE = " + this.SCALE);
            }
            //3.0
            else if(metrics.density == MODE_XHDPI_3)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_XHDPI_3);
                Log.d(TAG, "SCALE = " + this.SCALE);
            }
        }

        Log.d(TAG, "SCALE (AFTER) = " + this.SCALE);

        this.snake = new Snake(CURRENT_SNAKE_HEAD_X, CURRENT_SNAKE_HEAD_Y, CURRENT_SNAKE_BODY_X, CURRENT_SNAKE_BODY_Y, this);
        this.apple = new Apple(this);
        this.wall_vertical = Bitmap.createScaledBitmap(this.wall_vertical, this.SCALE, this.SCALE * 2, true);
        this.wall_horizontal = Bitmap.createScaledBitmap(this.wall_horizontal, this.SCALE * 2, this.SCALE, true);
        this.apple_red = Bitmap.createScaledBitmap(this.apple_red, this.SCALE, this.SCALE, true);
        this.kirpich = Bitmap.createScaledBitmap(this.kirpich, this.SCALE, this.SCALE, true);
        this.arrow_left = Bitmap.createScaledBitmap(this.arrow_left, this.SCALE * 2, this.SCALE * 2, true);
        this.arrow_right = Bitmap.createScaledBitmap(this.arrow_right, this.SCALE * 2, this.SCALE * 2, true);
        this.arrow_up = Bitmap.createScaledBitmap(this.arrow_up, this.SCALE * 2, this.SCALE * 2, true);
        this.arrow_down = Bitmap.createScaledBitmap(this.arrow_down, this.SCALE * 2, this.SCALE * 2, true);
        this.one = Bitmap.createScaledBitmap(this.one, this.SCALE * 3, this.SCALE * 3, true);
        this.two = Bitmap.createScaledBitmap(this.two, this.SCALE * 3, this.SCALE * 3, true);
        this.three = Bitmap.createScaledBitmap(this.three, this.SCALE * 3, this.SCALE * 3, true);
        this.go = Bitmap.createScaledBitmap(this.go, this.SCALE * 3, this.SCALE * 3, true);

        this.arrBitmap = new ArrayList<>();
        this.arrBitmap.add(this.three);
        this.arrBitmap.add(this.two);
        this.arrBitmap.add(this.one);
        this.arrBitmap.add(this.go);

        this.GameProcess = new GameProcess();
        this.GameProcess.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d("-----", "width = " + width + " height = " + height);

        //Делаю оптимальный расчет ширины и высоты SurfaceView, подогнаный под размер SCALE
        this.current_height = (height / this.SCALE) * this.SCALE;
        this.current_width = (width / this.SCALE) * this.SCALE;

        //==== TESTING ================
        this.COUNT_RECT_HEIGHT = String.valueOf(height / this.SCALE);
        this.COUNT_RECT_WIDTH = String.valueOf(width / this.SCALE);

        //=============================
        //Нахожу текстовые поля для отображения счета игры и количество сьеденых яблок
        if(this.tvScore == null)
        {
            this.tvScore = (TextView) this.activity.findViewById(R.id.tvScore);
        }
        Game.this.tvScore.setText("SCORE: " + Game.this.SCORE);
        if(this.tvApple == null)
        {
            this.tvApple = (TextView) this.activity.findViewById(R.id.tvApple);
        }
        Game.this.tvApple.setText("APPLE: " + Game.this.APPLE);


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(this.current_width, this.current_height);
        this.setLayoutParams(layoutParams);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        try
        {
            this.playerGamePlay.stop();
            this.playerGameOver.stop();
            this.GameProcess.stopRun();
            this.GameProcess.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        this.playerGamePlay.start();
    }

    /**
     * Поток который будет рисовать графику
     */
    class GameProcess extends Thread
    {
        private SurfaceHolder holder;
        private Canvas canvas;
        private Paint paint;
        private boolean isRun = true;
        public boolean isPause = false;
        private Bitmap current_head;
        private boolean isStarted = false;


        /**
         * Метод остановки потока
         */
        public void stopRun()
        {
            this.isRun = false;
        }
        /**
         * Метод включает паузу
         */
        public synchronized void pauseOn()
        {
            if(this.isPause == false)
            {
                this.isPause = true;
            }
        }

        /**
         * Метод снимает с паузы
         */
        public synchronized void pauseOff()
        {
            if(this.isPause == true)
            {
                this.isPause = false;
//                this.notify();  //Будим вторичный поток
            }
        }

        /**
         * Метод сохранения результата в БД
         */
        public void saveScoreInDB()
        {
            SQLiteDatabase db =  MainActivity.dbHelper.getWritableDatabase();
            //--- Создаю и заполняю ContentValues
            int result = Integer.parseInt(Game.this.tvScore.getText().toString().replace("SCORE:", "").trim());
            if(result > 0)
            {
                ContentValues newScore = new ContentValues();
                newScore.put("name", MenuActivity.PLAYER_NAME);
                newScore.put("score", result);
                //--- Добавляю запись в БД
                long rowID = db.insert("Score", null, newScore);
            }
        }

        @Override
        public void run()
        {
            //Инициализирую поля
            this.holder = Game.this.getHolder();
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            int numbers = 0;
            try
            {
                while (isRun)
                {
                    if(this.isPause) continue;
                    //--- Игровой процесс
                    //=== 1. Получить канвас
                    this.canvas = this.holder.lockCanvas(null);
                    if(canvas == null) continue;
                    //=== 2. Отрисовка
                    //--- Двигаю змею
                    if(this.isStarted)
                    {
                        Game.this.snake.move();
                    }
                    //Проверяю на касание стенок
                    if(Game.this.snake.snakeX[0] == 0 || Game.this.snake.snakeX[0] == (Game.this.getWidth() / Game.this.SCALE) - 1)
                    {
                        this.pauseOn();
                        Game.this.activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                StartGameActivity.tvResult.setText(Game.this.tvScore.getText().toString());
                                StartGameActivity.dialog_game_over.show();
                                Game.this.playerGamePlay.stop();
                                Game.this.playerGameOver.start();
                                saveScoreInDB();
                            }
                        });
                    }
                    else
                    if(Game.this.snake.snakeY[0] == 0 || Game.this.snake.snakeY[0] == (Game.this.getHeight() / Game.this.SCALE) - 1)
                    {
                        this.pauseOn();
                        Game.this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StartGameActivity.tvResult.setText(Game.this.tvScore.getText().toString());
                                StartGameActivity.dialog_game_over.show();
                                Game.this.playerGamePlay.stop();
                                Game.this.playerGameOver.start();
                                saveScoreInDB();
                            }
                        });
                    }
                    //если змейка наткнулась на себя (сьедает хвост)
                    for (int k = Game.this.snake.length - 1; k > 0 ; k--)
                    {
                        if((Game.this.snake.snakeX[0] == Game.this.snake.snakeX[k]) && (Game.this.snake.snakeY[0] == Game.this.snake.snakeY[k]))
                        {
                            this.pauseOn();
                            Game.this.activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    StartGameActivity.tvResult.setText(Game.this.tvScore.getText().toString());
                                    StartGameActivity.dialog_game_over.show();
                                    Game.this.playerGamePlay.stop();
                                    Game.this.playerGameOver.start();
                                    saveScoreInDB();
                                }
                            });
                        }
                    }

                    //Проверяю сьела ли змейка яблоко
                    if((Game.this.snake.snakeX[0] == Game.this.apple.posX) &&
                            (Game.this.snake.snakeY[0] == Game.this.apple.posY))
                    {
                        //Рисую яблоко в новом месте
                        Game.this.apple.randomPosition();
                        //Увеличиваю размер змейки на 1
                        Game.this.snake.length++;
                        //Увеличиваю счет игры
                        Game.this.SCORE += 10;
                        Game.this.APPLE += 1;
                        Game.this.activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Game.this.tvScore.setText("SCORE: " + Game.this.SCORE);
                                Game.this.tvApple.setText("APPLE: " + Game.this.APPLE);
                            }
                        });
                    }
                    //--- Делаю так чтобы яблоко не появлялось на "теле" змейки
                    for (int i = 1; i < Game.this.snake.length; i++)
                    {
                        if((Game.this.snake.snakeX[i] == Game.this.apple.posX) &&
                                (Game.this.snake.snakeY[i] == Game.this.apple.posY))
                        {
                            //Рисую яблоко в новом месте
                            Game.this.apple.randomPosition();
                        }
                    }

                    //Рисую игровое поле
                    this.paint.setColor(Color.BLACK);
                    this.canvas.drawRect(new RectF(0, 0, Game.this.getWidth(), Game.this.getHeight()), this.paint);
                    //Рисую карту
                    if(!Game.this.gameIsStarted)
                    {
                        //Карта
                        MAP_RANDOM = new int[current_height / SCALE][current_width / SCALE];
                        Game.this.map = new Map(Game.this);
                        Game.this.map.map_create();
                        Game.this.gameIsStarted = true;
                    }
                    if(Game.this.map != null)
                    {
                        for (int i = 0; i < Game.this.MAP_RANDOM.length; i++)
                        {
                            for (int k = 0; k < Game.this.MAP_RANDOM[i].length; k++)
                            {
                                if(Game.this.MAP_RANDOM[i][k] == 0) continue;
                                this.canvas.drawBitmap(Game.this.kirpich, k * Game.this.SCALE, i * Game.this.SCALE, this.paint);
                            }
                        }
                    }
                    //===> Проверяю на столкновение с препятствиями
                    if(Game.this.MAP_RANDOM[Game.this.snake.snakeY[0]][Game.this.snake.snakeX[0]] == 1) {
                        this.pauseOn();
                        Game.this.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StartGameActivity.tvResult.setText(Game.this.tvScore.getText().toString());
                                StartGameActivity.dialog_game_over.show();
                                Game.this.playerGamePlay.stop();
                                Game.this.playerGameOver.start();
                                saveScoreInDB();
                            }
                        });
                    }
                    //Рисую границы
                    Paint wall = new Paint();
                    wall.setColor(Color.rgb(135, 235, 0));
                    //левая
                    int xCount = Game.this.getWidth() / Game.this.SCALE;
                    int yCount = Game.this.getHeight() / Game.this.SCALE;
                    for (int i = 0; i <= yCount ; i+=2)
                    {
                        this.canvas.drawBitmap(Game.this.wall_vertical, 0, i * Game.this.SCALE, wall);
                    }
                    //правая
                    for (int i = 0; i <= yCount ; i+=2)
                    {
                        this.canvas.drawBitmap(Game.this.wall_vertical, Game.this.getWidth() - Game.this.SCALE, i * Game.this.SCALE, wall);
                    }
                    //верхняя
                    for (int i = 0; i <= xCount ; i+=2)
                    {
                        this.canvas.drawBitmap(Game.this.wall_horizontal, i * Game.this.SCALE, 0, wall);
                    }
                    //нижняя
                    for (int i = 0; i <= xCount ; i+=2)
                    {
                        this.canvas.drawBitmap(Game.this.wall_horizontal, i * Game.this.SCALE, Game.this.getHeight() - Game.this.SCALE, wall);
                    }
                    //--- Рисую змею
                    //Выбор направления головы
                    switch (Game.this.snake.direction)
                    {
                        case 0:
                            this.current_head = BitmapFactory.decodeResource(Game.this.getResources(), R.drawable.snake_head_right);
                            this.current_head = Bitmap.createScaledBitmap(this.current_head, Game.this.SCALE, Game.this.SCALE, true);
                            break;
                        case 1:
                            this.current_head = BitmapFactory.decodeResource(Game.this.getResources(), R.drawable.snake_head_down);
                            this.current_head = Bitmap.createScaledBitmap(this.current_head, Game.this.SCALE, Game.this.SCALE, true);
                            break;
                        case 2:
                            this.current_head = BitmapFactory.decodeResource(Game.this.getResources(), R.drawable.snake_head_left);
                            this.current_head = Bitmap.createScaledBitmap(this.current_head, Game.this.SCALE, Game.this.SCALE, true);
                            break;
                        case 3:
                            this.current_head = BitmapFactory.decodeResource(Game.this.getResources(), R.drawable.snake_head_up);
                            this.current_head = Bitmap.createScaledBitmap(this.current_head, Game.this.SCALE, Game.this.SCALE, true);
                            break;
                    }
                    this.paint.setColor(Color.rgb(255, 0, 0));
                    int count = 0;
                    for (int i = 0; i < Game.this.snake.length; i++)
                    {
                        if(count > 0)
                        {
                            this.canvas.drawOval(new RectF((Game.this.snake.snakeX[i] * Game.this.SCALE) + 5,
                                        (Game.this.snake.snakeY[i] * Game.this.SCALE) + 5,
                                        (Game.this.snake.snakeX[i] * Game.this.SCALE) + Game.this.SCALE - 4,
                                        (Game.this.snake.snakeY[i] * Game.this.SCALE) + Game.this.SCALE - 4),
                                        this.paint);
                        }
                        else
                        {
                            this.canvas.drawBitmap(this.current_head,
                                    Game.this.snake.snakeX[i] * Game.this.SCALE,
                                    Game.this.snake.snakeY[i] * Game.this.SCALE, this.paint);
                        }
                        count++;
                    }
                    //--- Чтобы яблоко не появлялось на "препятствии"
                    while (Game.this.MAP_RANDOM[Game.this.apple.posY][Game.this.apple.posX] == 1)
                    {
                        Game.this.apple.randomPosition();
                        continue;
                    }
                    //--- Рисую яблоко
                    Paint appleColor = new Paint();
                    appleColor.setColor(Color.RED);
                    this.canvas.drawBitmap(Game.this.apple_red, Game.this.apple.posX * Game.this.SCALE + 1, Game.this.apple.posY * Game.this.SCALE + 1, appleColor);
                    //Рисую кнопки управления
                    //Кнопки
                    Game.this.rectLeft = new RectF(0, Game.this.SCALE * 3, Game.this.SCALE * 3, Game.this.getHeight() - (Game.this.SCALE * 3));
                    Game.this.rectRight = new RectF(Game.this.getWidth() - (Game.this.SCALE * 3), Game.this.SCALE * 3, Game.this.getWidth(), Game.this.getHeight() - (Game.this.SCALE * 3));
                    Game.this.rectUp = new RectF(Game.this.SCALE * 3, 0, Game.this.getWidth() - (Game.this.SCALE * 3), Game.this.SCALE * 3);
                    Game.this.rectDown = new RectF(Game.this.SCALE * 3, Game.this.getHeight() - (Game.this.SCALE * 3), Game.this.getWidth() - (Game.this.SCALE * 3), Game.this.getHeight());

                    this.paint.setColor(Color.WHITE);
                    Paint arrow_alpha = new Paint();
                    arrow_alpha.setAlpha(Game.this.button_alpha);
                    //Влево
                    this.paint.setAlpha(Game.this.rectF_Left_alpha);
                    this.canvas.drawRect(Game.this.rectLeft, this.paint);
                    this.canvas.drawBitmap(Game.this.arrow_left, Game.this.SCALE, (Game.this.getHeight() - Game.this.SCALE * 2) / 2, arrow_alpha);
                    //Вправо
                    this.paint.setAlpha(Game.this.rectF_Right_alpha);
                    this.canvas.drawRect(Game.this.rectRight, this.paint);
                    this.canvas.drawBitmap(Game.this.arrow_right, Game.this.getWidth() - (Game.this.SCALE * 3), (Game.this.getHeight() - Game.this.SCALE * 2) / 2, arrow_alpha);
                    //Вниз
                    this.paint.setAlpha(Game.this.rectF_Down_alpha);
                    this.canvas.drawRect(Game.this.rectDown, this.paint);
                    this.canvas.drawBitmap(Game.this.arrow_down, (Game.this.getWidth() - Game.this.SCALE * 2) / 2, Game.this.getHeight() - (Game.this.SCALE * 3), arrow_alpha);
                    //Вверх
                    this.paint.setAlpha(Game.this.rectF_Up_alpha);
                    this.canvas.drawRect(Game.this.rectUp, this.paint);
                    this.canvas.drawBitmap(Game.this.arrow_up, (Game.this.getWidth() - Game.this.SCALE * 2) / 2, Game.this.SCALE, arrow_alpha);

                    //--- Отсчет времени потом старт игры ---
                    Paint numPaint = new Paint();
                    numPaint.setColor(Color.WHITE);
                    if(numbers < Game.this.arrBitmap.size())
                    {
                        this.canvas.drawBitmap(Game.this.arrBitmap.get(numbers), (Game.this.getWidth() / 2) - Game.this.SCALE, (Game.this.getHeight() / 2) - Game.this.SCALE * 2, numPaint);
                        numbers++;
                        Thread.sleep(1000);
                    }
                    if(numbers == Game.this.arrBitmap.size() - 1) this.isStarted = true;

                    //--- скорость движения
                    Thread.sleep(Game.SPEED);
                    //=== 3. Отпустить канвас
                    this.holder.unlockCanvasAndPost(this.canvas);
                }
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
    }
}
