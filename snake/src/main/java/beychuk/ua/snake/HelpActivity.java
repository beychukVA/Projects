package beychuk.ua.snake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Окно справки
 */
public class HelpActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Button btn_home = (Button) findViewById(R.id.btn_help_home);
        btn_home.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent("ua.beychuk.snake.MENU_GAME"));
            }
        });
    }

    @Override
    public void onBackPressed() {}
}
