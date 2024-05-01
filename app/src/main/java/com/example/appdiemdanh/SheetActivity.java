package com.example.appdiemdanh;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.Calendar;

public class SheetActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sheet);

		showTable();
		ImageButton back = findViewById(R.id.back);
		back.setOnClickListener(v->onBackPressed());
	}



	private void showTable() {
		DbHelper dbHelper = new DbHelper(this);
		TableLayout tableLayout = findViewById(R.id.tableLayout);
		long[] idArray = getIntent().getLongArrayExtra("idArray");
		int[] rollArray = getIntent().getIntArrayExtra("rollArray");
		String[] nameArray = getIntent().getStringArrayExtra("nameArray");
		String month = getIntent().getStringExtra("month");

		int DAY_IN_MONTH = getDayInMonth(month);

		// create table row

		int rowSize = idArray.length + 1;

		TableRow[] rows= new TableRow[rowSize];
		TextView[] rolls_tvs = new TextView[rowSize];
		TextView[] names_tvs = new TextView[rowSize];
		TextView[][] status_tvs = new TextView[rowSize][DAY_IN_MONTH + 1];

		for (int i = 0; i < rowSize; i++) {
			rolls_tvs[i] = new TextView(this);
			names_tvs[i] = new TextView(this);
			for (int j = 1; j <= DAY_IN_MONTH; j++) {
				status_tvs[i][j] = new TextView(this);
			}
		}

		// create table header
		rolls_tvs[0].setText("Roll");
		rolls_tvs[0].setTypeface(rolls_tvs[0].getTypeface(), Typeface.BOLD);

		names_tvs[0].setText("Name");
		names_tvs[0].setTypeface(names_tvs[0].getTypeface(), Typeface.BOLD);
		for (int i = 1; i <= DAY_IN_MONTH; i++) {
			status_tvs[0][i].setText(String.valueOf(i));
			status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface(), Typeface.BOLD);
		}

		for (int i = 1; i < rowSize; i++) {
			rolls_tvs[i].setText(String.valueOf(rollArray[i-1]));
			names_tvs[i].setText(nameArray[i-1]);

			for (int j = 1; j <= DAY_IN_MONTH; j++) {
				String day = String.valueOf(j);
				if (day.length() == 1) day = "0" + day;

				String date = day + "." + month;
				String status = dbHelper.getStatus(idArray[i-1], date);
				status_tvs[i][j].setText(status);
			}
		}

		for (int i = 0; i < rowSize; i++) {
			rows[i] = new TableRow(this);

			if(i % 2 == 0)
				rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
			else rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));

			rolls_tvs[i].setPadding(20, 20, 20, 20);
			names_tvs[i].setPadding(20, 20, 20, 20);

			rows[i].addView(rolls_tvs[i]);
			rows[i].addView(names_tvs[i]);

			for (int j = 1; j <= DAY_IN_MONTH; j++) {
				status_tvs[i][j].setPadding(20, 20, 20, 20);

				rows[i].addView(status_tvs[i][j]);
			}

			tableLayout.addView(rows[i]);
		}
		tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

	}

	private int getDayInMonth(String month) {
		int monthIndex = Integer.parseInt(month.substring(0, 2)) - 1;
		int year = Integer.parseInt(month.substring(3));

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, monthIndex);
		calendar.set(Calendar.YEAR, year);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
}