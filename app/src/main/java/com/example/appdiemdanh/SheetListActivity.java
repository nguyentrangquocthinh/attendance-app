package com.example.appdiemdanh;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toolbar;

import java.util.ArrayList;

public class SheetListActivity extends AppCompatActivity {
	ListView sheetList;
	ArrayAdapter adapter;
	ArrayList<String> listItems = new ArrayList();
	long cid;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sheet_list);

		cid = getIntent().getLongExtra("cid", -1);
		Log.i("1234567890", "onCreate: " + cid);
		loadListItems();
		sheetList = findViewById(R.id.sheetList);
		adapter = new ArrayAdapter(this, R.layout.sheet_list, R.id.date_list_item, listItems);
		sheetList.setAdapter(adapter);
	
		sheetList.setOnItemClickListener(( parent, view, position, id) -> openSheetActivity(position));
		ImageButton back = findViewById(R.id.back);
		back.setOnClickListener(v->onBackPressed());

	}


	private void openSheetActivity(int position) {
		long[] idArray = getIntent().getLongArrayExtra("idArray");
		int[] rollArray = getIntent().getIntArrayExtra("rollArray");
		String[] nameArray = getIntent().getStringArrayExtra("nameArray");
		Intent intent = new Intent(this, SheetActivity.class);
		intent.putExtra("idArray", idArray);
		intent.putExtra("rollArray", rollArray);
		intent.putExtra("nameArray", nameArray);
		intent.putExtra("month", listItems.get(position));


		startActivity(intent);
	}

	@SuppressLint("Range")
	private void loadListItems() {
		Cursor cursor = new DbHelper(this).getDistinctMonth(cid);

		while (cursor.moveToNext()) {
			String date = cursor.getString(cursor.getColumnIndex(DbHelper.DATE_KEY));
			listItems.add(date.substring(3));
		}
	}
}