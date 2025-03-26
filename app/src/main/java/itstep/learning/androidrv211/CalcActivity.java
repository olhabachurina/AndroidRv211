package itstep.learning.androidrv211;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class CalcActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        // Получаем ссылки на все TextView
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvExpression = findViewById(R.id.tvExpression);
        TextView tvDisplay = findViewById(R.id.tvDisplay);

        // Устанавливаем тексты
        tvTitle.setText("Калькулятор");
        tvExpression.setText("3 × 9");
        tvDisplay.setText("27");
    }
}