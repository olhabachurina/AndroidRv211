package itstep.learning.androidrv211;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import itstep.learning.orm.CurrencyRateNbu;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    private List<CurrencyRateNbu> rates;

    public CurrencyAdapter(List<CurrencyRateNbu> rates) {
        this.rates = rates;
    }

    @NonNull
    @Override
    public CurrencyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CurrencyRateNbu rate = rates.get(position);

        String title = rate.getTxt() + " (" + rate.getCc() + ")";
        holder.tvTitle.setText(title);

        double directRate = rate.getRate();
        double reverseRate = (directRate == 0) ? 0 : 1.0 / directRate;

        String lineDirect = "1 " + rate.getCc() + " = " + String.format("%.4f", directRate) + " HRN";
        String lineReverse = "1 HRN = " + String.format("%.4f", reverseRate) + " " + rate.getCc();
        holder.tvRates.setText(lineDirect + "\n" + lineReverse);
    }

    @Override
    public int getItemCount() {
        return rates != null ? rates.size() : 0;
    }

    public void updateData(List<CurrencyRateNbu> newRates) {
        this.rates = newRates;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvRates;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRates = itemView.findViewById(R.id.tvRates);
        }
    }
}
/*
 *  Робота RecyclerView у Android
 *
 * 1. Activity:
 * - Містить список даних: List<Rates> rates
 * - Передає дані в Adapter через setAdapter()

 * 2. Adapter:
 * - onCreateViewHolder(): створює ViewHolder, інфлює layout (XML)
 * - onBindViewHolder(): прив'язує об'єкт Rate до ViewHolder

 * 3. ViewHolder:
 * - Зберігає посилання на елементи View (наприклад, TextView)
 * - Отримує доступ до них через findViewById()
 * - Може містити метод bind(Rate), що оновлює вміст UI

 * 4. Layout (item.xml):
 * - XML-шаблон одного елементу списку
 * - Містить View-компоненти з id (наприклад, R.id.nbu_rate_txt)

 * Потік роботи:
 * Activity -> Adapter.setData(List<Rates>) ->
 * Adapter.onCreateViewHolder() -> ViewHolder(itemView) ->
 * ViewHolder.findViewById() -> onBindViewHolder() -> ViewHolder.bind(rate)
 */