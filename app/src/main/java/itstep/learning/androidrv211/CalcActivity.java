package itstep.learning.androidrv211;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;



public class CalcActivity extends AppCompatActivity {

    private TextView tvExpression, tvDisplay;
    private StringBuilder expression = new StringBuilder();
    private double memoryValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvExpression = findViewById(R.id.tvExpression);
        tvDisplay = findViewById(R.id.tvDisplay);

        // Відновлення збереженого стану, якщо він існує
        if (savedInstanceState != null) {
            String savedExpression = savedInstanceState.getString("expression", "");
            String savedDisplay = savedInstanceState.getString("display", "0");
            expression = new StringBuilder(savedExpression);
            tvExpression.setText(savedExpression);
            tvDisplay.setText(savedDisplay);
        }

        int[] numberIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int id : numberIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> appendToExpression(btn.getText().toString()));
        }

        setOperator(R.id.btn_add, "+");
        setOperator(R.id.btn_subtract, "-");
        setOperator(R.id.btn_multiply, "*");
        setOperator(R.id.btn_divide, "/");
        setOperator(R.id.btn_dot, ".");

        findViewById(R.id.btn_ac).setOnClickListener(v -> clear());

        findViewById(R.id.btn_equals).setOnClickListener(v -> calculate());

        // Допоміжні функції
        findViewById(R.id.btn_sign).setOnClickListener(v -> toggleSign());
        findViewById(R.id.btn_percent).setOnClickListener(v -> applyPercent());
        findViewById(R.id.btn_backspace).setOnClickListener(v -> backspace());
        findViewById(R.id.btn_open_parenthesis).setOnClickListener(v -> appendToExpression("("));
        findViewById(R.id.btn_close_parenthesis).setOnClickListener(v -> appendToExpression(")"));
        findViewById(R.id.btn_sqrt).setOnClickListener(v -> squareRoot());
        findViewById(R.id.btn_memory_clear).setOnClickListener(v -> memoryClear());
        findViewById(R.id.btn_memory_recall).setOnClickListener(v -> memoryRecall());
        findViewById(R.id.btn_memory_add).setOnClickListener(v -> memoryAdd());
        findViewById(R.id.btn_memory_subtract).setOnClickListener(v -> memorySubtract());
    }
    // onCreate(): Ініціалізує активність, встановлює розмітку, відновлює збережений стан та прив’язує обробники подій до кнопок.

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("expression", expression.toString());
        outState.putString("display", tvDisplay.getText().toString());
    }
    // onSaveInstanceState(): Зберігає поточний стан (вираз та відображення результату) перед пересозданням активності.

    private void appendToExpression(String value) {
        expression.append(value);
        tvExpression.setText(expression.toString());
    }
    // appendToExpression(): Додає задане значення до виразу та оновлює відображення TextView.

    private void setOperator(int id, String op) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> appendToExpression(op));
    }
    // setOperator(): Призначає обробник події для кнопки оператора, що додає символ оператора до виразу.

    private void clear() {
        expression.setLength(0);
        tvExpression.setText("");
        tvDisplay.setText("0");
    }
    // clear(): Очищає вираз і скидає відображення до початкових значень.

    private void calculate() {
        try {
            String expr = expression.toString().replace(",", ".");
            Expression parsed = new ExpressionBuilder(expr).build();
            double result = parsed.evaluate();
            tvDisplay.setText(String.valueOf(result));
        } catch (Exception e) {
            tvDisplay.setText("Помилка");
        }
    }
    // calculate(): Обчислює математичний вираз за допомогою ExpressionBuilder та відображає результат.

    private void toggleSign() {
        try {
            String expr = expression.toString();
            if (!expr.isEmpty()) {
                double value = new ExpressionBuilder(expr).build().evaluate();
                value = -value;
                expression = new StringBuilder(String.valueOf(value));
                tvExpression.setText(expression.toString());
            }
        } catch (Exception e) {
            tvDisplay.setText("Помилка");
        }
    }
    // toggleSign(): Змінює знак поточного виразу (додає або віднімає мінус).

    private void applyPercent() {
        try {
            String expr = expression.toString();
            if (!expr.isEmpty()) {
                double value = new ExpressionBuilder(expr).build().evaluate();
                value = value / 100;
                expression = new StringBuilder(String.valueOf(value));
                tvExpression.setText(expression.toString());
            }
        } catch (Exception e) {
            tvDisplay.setText("Помилка");
        }
    }
    // applyPercent(): Обчислює відсоткове значення поточного виразу та оновлює його.

    private void backspace() {
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
            tvExpression.setText(expression.toString());
        }
    }
    // backspace(): Видаляє останній символ з виразу і оновлює відображення.

    private void squareRoot() {
        try {
            String expr = expression.toString();
            if (!expr.isEmpty()) {
                double value = new ExpressionBuilder(expr).build().evaluate();
                if (value < 0) {
                    tvDisplay.setText("Помилка");
                    return;
                }
                value = Math.sqrt(value);
                expression = new StringBuilder(String.valueOf(value));
                tvExpression.setText(expression.toString());
            }
        } catch (Exception e) {
            tvDisplay.setText("Помилка");
        }
    }
    // squareRoot(): Обчислює квадратний корінь поточного виразу, якщо значення не є від'ємним.

    // Функції пам'яті
    private void memoryClear() {
        memoryValue = 0;
    }
    // memoryClear(): Очищає значення пам'яті.

    private void memoryRecall() {
        appendToExpression(String.valueOf(memoryValue));
    }
    // memoryRecall(): Додає значення з пам'яті до поточного виразу.

    private void memoryAdd() {
        try {
            String expr = expression.toString();
            if (!expr.isEmpty()) {
                double value = new ExpressionBuilder(expr).build().evaluate();
                memoryValue += value;
            }
        } catch (Exception e) {
            tvDisplay.setText("Помилка");
        }
    }
    // memoryAdd(): Додає поточне обчислене значення до пам'яті.

    private void memorySubtract() {
        try {
            String expr = expression.toString();
            if (!expr.isEmpty()) {
                double value = new ExpressionBuilder(expr).build().evaluate();
                memoryValue -= value;
            }
        } catch (Exception e) {
            tvDisplay.setText("Помилка");
        }
    }
    // memorySubtract(): Віднімає поточне обчислене значення від пам'яті.
}

/*

CalcActivity є основною активністю калькулятора, яка дозволяє користувачу вводити математичні вирази,
 обчислювати їх, змінювати знак, розраховувати відсотки, застосовувати функцію квадратного кореня,
 редагувати введення (backspace), а також виконувати операції з пам'яттю (збереження, додавання,
 віднімання та виклик значення пам'яті). Стан активності зберігається при зміні орієнтації,
 що забезпечує безперервність роботи калькулятора.
*/

