package itstep.learning.androidrv211;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Калькулятор
        Button btnCalc = findViewById(R.id.main_btn_calc);
        btnCalc.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CalcActivity.class);
            startActivity(intent);
        });

        // Курси валют
        Button btnCurrency = findViewById(R.id.main_btn_currency);
        btnCurrency.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CurrencyActivity.class);
            startActivity(intent);
        });

        // ЄВсьо
        Button btnFinal = findViewById(R.id.main_btn_final);
        btnFinal.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FinalActivity.class);
            startActivity(intent);
        });

        // Контейнер для дополнительных кнопок
        LinearLayout mainLlContainer = findViewById(R.id.main_ll_container);

        // Кнопка "АНІМАЦІЇ"
        Button animButton = new Button(this);
        styleButton(animButton, getString(R.string.anim_title), R.drawable.ic_anim);
        animButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AnimActivity.class);
            startActivity(intent);
        });
        mainLlContainer.addView(animButton);

        // Кнопка "Чат"
        Button chatButton = new Button(this);
        styleButton(chatButton, "Чат", R.drawable.baseline_chat_24);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });
        mainLlContainer.addView(chatButton);
    }

    // Универсальный метод стилизации кнопок
    private void styleButton(Button button, String text, int iconRes) {
        button.setText(text);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_btn_purple);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setTextSize(16);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);

        // Иконка и отступ между иконкой и текстом
        button.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, iconRes), null, null, null
        );
        button.setCompoundDrawablePadding(
                (int) (8 * getResources().getDisplayMetrics().density)
        );

        // Отступ слева
        button.setPadding(
                (int) (24 * getResources().getDisplayMetrics().density),
                button.getPaddingTop(),
                button.getPaddingRight(),
                button.getPaddingBottom()
        );

        // Параметры layout с отступом сверху
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
        button.setLayoutParams(params);
    }
}