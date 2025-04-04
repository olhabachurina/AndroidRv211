package itstep.learning.androidrv211;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import itstep.learning.orm.CurrencyRateNbu;

public class CurrencyAdapter extends ListAdapter<CurrencyRateNbu, CurrencyAdapter.ViewHolder> {

    public CurrencyAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<CurrencyRateNbu> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CurrencyRateNbu>() {

                @Override
                public boolean areItemsTheSame(@NonNull CurrencyRateNbu oldItem, @NonNull CurrencyRateNbu newItem) {
                    return oldItem.getCc().equals(newItem.getCc());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CurrencyRateNbu oldItem, @NonNull CurrencyRateNbu newItem) {
                    return oldItem.getRate() == newItem.getRate()
                            && Objects.equals(oldItem.getTxt(), newItem.getTxt())
                            && Objects.equals(oldItem.getExchangedate(), newItem.getExchangedate());
                }

                @Override
                public Object getChangePayload(@NonNull CurrencyRateNbu oldItem, @NonNull CurrencyRateNbu newItem) {
                    Bundle diff = new Bundle();

                    if (oldItem.getRate() != newItem.getRate()) {
                        diff.putDouble("rate", newItem.getRate());
                    }

                    if (!Objects.equals(oldItem.getTxt(), newItem.getTxt())) {
                        diff.putString("txt", newItem.getTxt());
                    }

                    if (!Objects.equals(oldItem.getExchangedate(), newItem.getExchangedate())) {
                        diff.putString("date", newItem.getExchangedate());
                    }

                    return diff.size() == 0 ? null : diff;
                }
            };

    @NonNull
    @Override
    public CurrencyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_currency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyAdapter.ViewHolder holder, int position) {
        bindData(holder, getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Bundle diff = (Bundle) payloads.get(0);
            CurrencyRateNbu rate = getItem(position);

            if (diff.containsKey("rate")) {
                double newRate = rate.getRate();
                double reverseRate = newRate == 0 ? 0 : 1.0 / newRate;

                String lineDirect = "1 " + rate.getCc() + " = " + String.format("%.4f", newRate) + " HRN";
                String lineReverse = "1 HRN = " + String.format("%.4f", reverseRate) + " " + rate.getCc();
                holder.tvRates.setText(lineDirect + "\n" + lineReverse);

                // 🔄 Анимация обновления
                holder.tvRates.setAlpha(0f);
                holder.tvRates.animate().alpha(1f).setDuration(300).start();
            }

            if (diff.containsKey("txt")) {
                String title = rate.getTxt() + " (" + rate.getCc() + ")";
                holder.tvTitle.setText(title);
            }
        }
    }

    private void bindData(@NonNull ViewHolder holder, CurrencyRateNbu rate) {
        String title = rate.getTxt() + " (" + rate.getCc() + ")";
        holder.tvTitle.setText(title);

        double directRate = rate.getRate();
        double reverseRate = directRate == 0 ? 0 : 1.0 / directRate;

        String lineDirect = "1 " + rate.getCc() + " = " + String.format("%.4f", directRate) + " HRN";
        String lineReverse = "1 HRN = " + String.format("%.4f", reverseRate) + " " + rate.getCc();
        holder.tvRates.setText(lineDirect + "\n" + lineReverse);
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