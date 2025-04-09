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

        // Создание и добавление кнопки "АНІМАЦІЇ"
        Button animButton = new Button(this);
        styleAnimationButton(animButton);
        LinearLayout mainLlContainer = findViewById(R.id.main_ll_container);
        mainLlContainer.addView(animButton);
// Кнопка "Чат"
        Button chatButton = new Button(this);
        chatButton.setText("Чат");
        chatButton.setAllCaps(false);
        chatButton.setBackgroundResource(R.drawable.bg_btn_purple);
        chatButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        chatButton.setTextSize(16);
        chatButton.setTypeface(chatButton.getTypeface(), Typeface.BOLD);

// Отступы и параметры
        LinearLayout.LayoutParams chatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        chatParams.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
        chatButton.setLayoutParams(chatParams);

// Обработчик перехода
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

// Добавление в контейнер
        mainLlContainer.addView(chatButton);

    }

    private void styleAnimationButton(Button animButton) {
        animButton.setText(getString(R.string.anim_title));
        animButton.setAllCaps(false);
        animButton.setBackgroundResource(R.drawable.bg_btn_purple);
        animButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        animButton.setTextSize(16);
        animButton.setTypeface(animButton.getTypeface(), Typeface.BOLD);

        // Иконка + отступ между иконкой и текстом
        animButton.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.ic_anim), null, null, null
        );
        animButton.setCompoundDrawablePadding(
                (int) (8 * getResources().getDisplayMetrics().density)
        );

        // Отступ СЛЕВА для выравнивания текста и иконки
        animButton.setPadding(
                (int) (24 * getResources().getDisplayMetrics().density), // left
                animButton.getPaddingTop(),
                animButton.getPaddingRight(),
                animButton.getPaddingBottom()
        );

        // Layout-параметры: отступ сверху
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
        animButton.setLayoutParams(params);

        // Переход в AnimActivity
        animButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AnimActivity.class);
            startActivity(intent);
        });
    }
}
