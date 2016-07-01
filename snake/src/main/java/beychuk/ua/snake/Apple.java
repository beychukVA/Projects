package beychuk.ua.snake;

/**
 * Класс "Яблоко"
 */
public class Apple
{
    private Game game;
    private int randomX;
    private int randomY;
    public int posX, posY;

    public Apple(Game game)
    {
        this.game = game;
        this.randomX = (this.game.getWidth() / this.game.SCALE) - 1;
        this.randomY = (this.game.getHeight() / this.game.SCALE) - 1;
        randomPosition();
    }

    public void randomPosition()
    {
        this.posX = (int)(Math.random() * this.randomX);
        if(this.posX == 0) this.posX = 1;
        this.posY = (int)(Math.random() * this.randomY);
        if(this.posY == 0) this.posY = 1;
    }
}
