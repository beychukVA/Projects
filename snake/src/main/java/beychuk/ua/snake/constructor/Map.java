package beychuk.ua.snake.constructor;

import beychuk.ua.snake.Game;

/**
 * Карта
 */
public class Map
{
    private Game game;

    public Map(Game game)
    {
        this.game = game;
    }

    public void map_create()
    {
//        int[][] map_random = new int[game.current_height / game.SCALE][game.current_width / game.SCALE];
//        int half_x = (game.current_width / game.SCALE / 2);
//        int half_y = (game.current_height / game.SCALE / 2);

        for (int y = 2; y < game.MAP_RANDOM.length - 2; y+=2)
        {
            for (int x = 2; x < game.MAP_RANDOM[y].length - 2; x+=2)
            {
//                if(x != half_x)
//                {
//                    if(x == 3 || x == (half_x * 2) - 3||
//                            y == 3 && x > 2 && x < (half_x * 2) - 3||
//                            y == map_level_1.length - 4  && x > 2 && x < (half_x * 2) - 3||
//                            (y > 3 && y < map_level_1.length - 4) && x == 5||
//                            (y > 3 && y < map_level_1.length - 4) && x == map_level_1[y].length - 6||
//                            (y > 3 && y < map_level_1.length - 4) && x == half_x - 2||
//                            (y > 3 && y < map_level_1.length - 4) && x == half_x + 2||
//                            y > 4 && y < map_level_1.length - 5 && (x > 5 && x < half_x - 2)||
//                            y > 4 && y < map_level_1.length - 5 && (x > half_x + 2 && x < map_level_1[y].length - 6)) continue;
//                    map_level_1[y][x] = 1;
//                }
                int value = (int) (Math.random() + 0.3);
                game.MAP_RANDOM[y][x] = value; //0 или 1
//                //проверяю на тупики и избавляюсь от них
//                if(game.MAP_RANDOM[y][x+1] == 1 && game.MAP_RANDOM[y-1][x] == 1 && game.MAP_RANDOM[y+1][x] == 1 &&
//                        game.MAP_RANDOM[y][x-1] == 1 && game.MAP_RANDOM[y-1][x] == 1 && game.MAP_RANDOM[y+1][x] == 1 &&
//                        game.MAP_RANDOM[y-1][x] == 1 && game.MAP_RANDOM[y+1][x] == 1 && game.MAP_RANDOM[y][x-1] == 1 &&
//                        game.MAP_RANDOM[y-1][x] == 1 && game.MAP_RANDOM[y+1][x] == 1 && game.MAP_RANDOM[y][x+1] == 1)
//                {
//                    game.MAP_RANDOM[y][x] = 1;
//                }
            }
        }
    }

}
