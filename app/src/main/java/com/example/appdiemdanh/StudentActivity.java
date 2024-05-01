package com.example.appdiemdanh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class StudentActivity extends AppCompatActivity {
	Toolbar toolbar;
	String className;
	String subjectName;
	int position;
	RecyclerView recyclerView;
	StudentAdapter adapter;
	RecyclerView.LayoutManager layoutManager;
	ArrayList<StudentItem> studentItems = new ArrayList<>();
	DbHelper dbHelper;
	long cid;
	MyCalendar calendar;
	TextView subtitle;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student);

		calendar = new MyCalendar();
		dbHelper = new DbHelper(this);

		Intent intent = getIntent();
		className = intent.getStringExtra("className");
		subjectName = intent.getStringExtra("subjectName");
		position = intent.getIntExtra("position", -1);
		cid = intent.getLongExtra("cid", -1);

		setToolbar();
		loadData();
		recyclerView = findViewById(R.id.student_recycler);
		recyclerView.setHasFixedSize(true);
		layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		adapter = new StudentAdapter(this, studentItems);
		recyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener(position->changeStatus(position));
		loadStatusData();

	}

	@SuppressLint("Range")
	private void loadData() {
		Cursor cursor = dbHelper.getStudentTable(cid);
		Log.i("1234567890", "loadData: " + cid);
		studentItems.clear();
		while (cursor.moveToNext()){
			long sid = Long.parseLong(cursor.getString(cursor.getColumnIndex(DbHelper.S_ID)));
			int roll = cursor.getInt(cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY));
			String name = cursor.getString(cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY));
			studentItems.add(new StudentItem(sid, roll, name));
		}
		cursor.close();

	}

	private void changeStatus(int position) {
		String status = studentItems.get(position).getStatus();

		if(status.equals("Present")){
			status = "Absent";
		}else{
			status = "Present";
		}
		studentItems.get(position).setStatus(status);
		adapter.notifyItemChanged(position);
	}

	private void setToolbar() {
		toolbar = findViewById(R.id.toolbar);

		TextView title = toolbar.findViewById(R.id.title_toolbar);
		subtitle = toolbar.findViewById(R.id.subtitle_toolbar);

		ImageButton back = toolbar.findViewById(R.id.back);
		ImageButton save = toolbar.findViewById(R.id.save);
		save.setOnClickListener(v->saveStatus());

		title.setText(className);
		subtitle.setText(subjectName +"  |  "+ calendar.getDate());

		back.setOnClickListener(v->onBackPressed());
		toolbar.inflateMenu(R.menu.student_menu);
		toolbar.setOnMenuItemClickListener(menuItem->onMenuItemClick(menuItem));
	}

	private void saveStatus() {
		for(StudentItem studentItem : studentItems){
			String status = studentItem.getStatus();
			if(status != "Present") status = "Absent";
			long value = dbHelper.addStatus(studentItem.getSid(), cid, calendar.getDate(), status);

			if(value==-1) dbHelper.updateStatus(studentItem.getSid(), calendar.getDate(), status);
		}
		Toast.makeText(this, "Save successfully", Toast.LENGTH_SHORT).show();
	}

	private void loadStatusData(){
		for(StudentItem studentItem : studentItems){
			String status = dbHelper.getStatus(studentItem.getSid(), calendar.getDate());
			if(status != null) studentItem.setStatus(status);
			else studentItem.setStatus("");
		}
		adapter.notifyDataSetChanged();
	}

	private boolean onMenuItemClick(MenuItem menuItem) {
		if(menuItem.getItemId() == R.id.add_student){
			showAddStudentDialog();
		} else if(menuItem.getItemId() == R.id.show_calendar){
			showCalendarDialog();
		} else if(menuItem.getItemId() == R.id.show_attendance_sheet){
			openSheetList();
		}
		return true;
	}

	private void openSheetList() {
		long[] idArray = new long[studentItems.size()];
		String[] nameArray = new String[studentItems.size()];
		int[] rollArray = new int[studentItems.size()];

		for (int i = 0; i < idArray.length; i++) {
			idArray[i] = studentItems.get(i).getSid();
		}

		for (int i = 0; i < rollArray.length; i++) {
			rollArray[i] = studentItems.get(i).getRoll();
		}

		for (int i = 0; i < nameArray.length; i++) {
			nameArray[i] = studentItems.get(i).getName();
		}


		Intent intent = new Intent(this, SheetListActivity.class);
		intent.putExtra("cid", cid);
		intent.putExtra("idArray", idArray);
		intent.putExtra("rollArray", rollArray);
		intent.putExtra("nameArray", nameArray);
		startActivity(intent);

	}

	private void showCalendarDialog() {
		MyCalendar calendar = new MyCalendar();
		calendar.show(getSupportFragmentManager(), "");
		calendar.setOnCalendarOkClickListener(this::onCalendarOkClicked);
	}

	private void onCalendarOkClicked(int year, int month, int day) {
		calendar.setDate(year, month, day);
		subtitle.setText(subjectName + "  |  " + calendar.getDate());
		loadStatusData();
	}

	private void showAddStudentDialog() {
		MyDialog dialog = new MyDialog();
		dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_ADD_DIALOG);
		dialog.setListener((roll, name)->addStudent(roll, name));
	}

//	them hoc sinh
	private void addStudent(String roll_string, String name) {
		int roll = Integer.parseInt(roll_string);
		long sid = dbHelper.addStudent(cid, roll, name);
		StudentItem studentItem = new StudentItem(sid, roll, name);
		studentItems.add(studentItem);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onContextItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){
			case 0:
				showUpdateStudentDialog(item.getGroupId());
				break;
			case 1:
				deleteStudent(item.getGroupId());
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void showUpdateStudentDialog(int position) {
		MyDialog dialog = new MyDialog(studentItems.get(position).getRoll(), studentItems.get(position).getName());
		dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_UPDATE_DIALOG);
		dialog.setListener((roll_string, name)->updateStudent(position, name));
	}

	// cap nhat sinh vien
	private void updateStudent(int position, String name) {
		dbHelper.updateStudent(studentItems.get(position).getSid(), name);
		studentItems.get(position).setName(name);
		adapter.notifyItemChanged(position);
		Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show();
	}

//	xoa sinh vien
	private void deleteStudent(int position) {
		dbHelper.deleteStudent(studentItems.get(position).getSid());
		studentItems.remove(position);
		adapter.notifyItemRemoved(position);
		Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show();

	}
}