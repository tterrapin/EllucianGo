/*
* Copyright 2015 Ellucian Company L.P. and its affiliates.
*/

package com.ellucian.mobile.android.finances;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.client.finances.Transaction;
import com.ellucian.mobile.android.util.CalendarUtils;
import com.ellucian.mobile.android.util.CurrencyUtils;

import java.util.Currency;

class TransactionsAdapter extends ArrayAdapter<Transaction> {

    private final Context context;
    private final Currency currency;

    public TransactionsAdapter(Context context, Currency currency, Transaction[] transactions) {
        super(context, 0, transactions);
        this.context = context;
        this.currency = currency;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Transaction transaction = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.finances_transaction_row, parent, false);
        }
        TextView transactionDescription = (TextView) convertView.findViewById(R.id.transaction_description);
        TextView transactionAmount = (TextView) convertView.findViewById(R.id.transaction_amount);
        TextView transactionDate = (TextView) convertView.findViewById(R.id.transaction_date);

        String displayDate = CalendarUtils.getDefaultDateString(context, transaction.entryDate);

        transactionDescription.setText(transaction.description);
        transactionAmount.setText(CurrencyUtils.getCurrencyString(transaction.amount, currency));
        transactionDate.setText(displayDate);

        return convertView;
    }
}
