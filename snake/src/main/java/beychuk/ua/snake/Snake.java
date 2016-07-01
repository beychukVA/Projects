package beychuk.ua.snake;

/**
 * Змейка
 */
public class Snake
{
    private Game game;
    public static int MOVE_STOP = -1;
    public static int MOVE_RIGHT = 0;
    public static int MOVE_DOWN = 1;
    public static int MOVE_LEFT = 2;
    public static int MOVE_UP = 3;
    public int direction = MOVE_UP;
    public int length = 2;
    public int snakeX[] = new int[100];
    public int snakeY[] = new int[100];

    public Snake(int x0, int y0, int x1, int y1, Game game)
    {
        this.snakeX[0] = x0;
        this.snakeY[0] = y0;
        this.snakeX[1] = x1;
        this.snakeY[1] = y1;
        this.game = game;
    }

    /**
     * Метод перемещения змейки
     */
    public void move()
    {
        //Перебераем массив и смещаем елементы
        for (int i = this.length; i > 0 ; i--)
        {
            this.snakeX[i] = this.snakeX[i - 1];
            this.snakeY[i] = this.snakeY[i - 1];
        }
        //направление движения + назначение картинки для "головы"
        if(this.direction == 0) this.snakeX[0]++;
        if(this.direction == 1) this.snakeY[0]++;
        if(this.direction == 2) this.snakeX[0]--;
        if(this.direction == 3) this.snakeY[0]--;
//        //если змейка наткнулась на себя (сьедает хвост)
//        for (int k = this.length - 1; k > 0 ; k--)
//        {
//            if((this.snakeX[0] == this.snakeX[k]) && (this.snakeY[0] == this.snakeY[k])) this.length = k;
//        }
//        //Если змейка зашла за "край экрана" она появляется с другой стороны
//        //по "х"
//        if(this.snakeX[0] > this.game.getWidth() / this.game.SCALE) this.snakeX[0] = 0;
//        if(this.snakeX[0] < 0) this.snakeX[0] = this.game.getWidth() / this.game.SCALE;
//        //по "у"
//        if(this.snakeY[0] > this.game.getHeight() / this.game.SCALE) this.snakeY[0] = 0;
//        if(this.snakeY[0] < 0) this.snakeY[0] = this.game.getHeight() / this.game.SCALE;
    }
}
