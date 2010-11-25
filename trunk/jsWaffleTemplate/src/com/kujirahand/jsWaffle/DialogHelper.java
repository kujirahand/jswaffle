package com.kujirahand.jsWaffle;

import java.util.Date;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class DialogHelper {
	public static Activity waffle_activity;
	public static WaffleObj waffle_obj;
	
	public static void dialogYesNo(String caption, String msg, final String callback_fn, final String tag) {
		final WaffleObj wobj = waffle_obj;
		new AlertDialog.Builder(waffle_activity)
		.setIcon(android.R.drawable.ic_menu_help)
		.setTitle(caption)
		.setMessage(msg)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	wobj.callJsEvent(String.format("%s(%s,'%s')", callback_fn, "true", tag));
		    }
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	wobj.callJsEvent(String.format("%s(%s,'%s')", callback_fn, "false", tag));
		    }
		})
		.show();
	}
	
	/**
	 * select dialog with list
	 * @param caption
	 * @param items
	 * @param callback_fn
	 * @param tag
	 */
	public static void selectList(String caption, String items, final String callback_fn, final String tag) {
		// items split
		final String[] str_items = items.split(";;;");
		//
		final WaffleObj wobj = waffle_obj;
		new AlertDialog.Builder(waffle_activity)
		.setTitle(caption)
		.setItems(str_items, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				String ans = str_items[which];
				ans = ans.replaceAll("\"", "''");
		    	wobj.callJsEvent(String.format("%s(\"%s\",'%s')", 
		    			callback_fn, 
		    			ans, 
		    			tag));
			}
		})
		.show();
	}
	
	/**
	 * select dialog with multi list
	 * @param caption
	 * @param items
	 * @param callback_fn
	 * @param tag
	 */
	public static void multiSelectList(String caption, String items, final String callback_fn, final String tag) {
		// items split
		final String[] str_items = items.split(";;;");
		final boolean[] bool_items = new boolean[str_items.length];
		//
		final WaffleObj wobj = waffle_obj;
		new AlertDialog.Builder(waffle_activity)
		.setTitle(caption)
		.setMultiChoiceItems(str_items, bool_items, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				bool_items[which] = isChecked;
			}
		})
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String ans = "";
				for (int i = 0; i < bool_items.length; i++) {
					if (bool_items[i]) {
						ans += str_items[i] + ";;;";
					}
				}
				if (ans != "") {
					ans = ans.substring(0, ans.length() - 3);
				}
				ans = ans.replaceAll("\"", "''");
		    	wobj.callJsEvent(String.format("%s(\"%s\",'%s')", 
		    			callback_fn, 
		    			ans, 
		    			tag));
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	wobj.callJsEvent(String.format("%s('','%s')", 
		    			callback_fn, 
		    			tag));
			}
		})
		.show();
	}
	
	public static void datePickerDialog(final String callback_fn, final String tag) {
		DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, 
					int year, 
					int monthOfYear,
					int dayOfMonth) {
				waffle_obj.callJsEvent(String.format("%s(%d,%d,%d,'%s')", 
						callback_fn,
						year,monthOfYear,dayOfMonth, tag));
			}
		};
		Date date = new Date();
		DatePickerDialog d = new DatePickerDialog(waffle_activity, mDateSetListener, 
				date.getYear()+1900,
				date.getMonth(),
				date.getDate());
		d.show();
	}
	
	public static void timePickerDialog(final String callback_fn, final String tag) {
		TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				waffle_obj.callJsEvent(
						String.format("%s(%d,%d,'%s')", 
								callback_fn,
								hourOfDay, minute, tag));
			}
		};
		Date date = new Date();
		TimePickerDialog d = new TimePickerDialog(waffle_activity, mTimeSetListener, date.getHours(), date.getMinutes(), true);
		d.show();
	}
}
