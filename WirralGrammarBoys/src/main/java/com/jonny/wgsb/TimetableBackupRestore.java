package com.jonny.wgsb;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TimetableBackupRestore {

	static void backup(Context mContext) {
		ContentResolver cr = mContext.getContentResolver();
		Cursor p = cr.query(TimetableProvider.PERIODS_URI, 
				new String[] {TimetableProvider.ID, TimetableProvider.NAME, TimetableProvider.DAY,
					TimetableProvider.START, TimetableProvider.END, TimetableProvider.ACTIVITY, TimetableProvider.TEACHER,
					TimetableProvider.ROOM, TimetableProvider.BREAK}, null, null, null);
		Cursor w = cr.query(TimetableProvider.WEEK_URI, 
				new String[] {TimetableProvider.ID, TimetableProvider.KEY, TimetableProvider.NUM}, null, null, null);
		String text = "";
		text += "#![ACTIVITIES]\n";
		p.moveToFirst();
		text += "#![PERIODS]\n";
		for (int i = 0; i < p.getCount(); i++) {
			String name = null;
			if (!"".equals(p.getString(1))) {
				name = p.getString(1);
			}
			String day = null;
			if (!"".equals(p.getString(2))) {
				day = p.getString(2);
			}
			int start = p.getInt(3);
			int end = p.getInt(4);
			String activity = null;
			if (!"".equals(p.getString(5))) {
				activity = p.getString(5);
			}
			String teacher = null;
			if (!"".equals(p.getString(6))) {
				teacher = p.getString(6);
			}
			String room = null;
			if (!"".equals(p.getString(7))) {
				room = p.getString(7);
			}
			int isBreak = p.getInt(8);
			text += name + "|" + day + "|" + start + "|" + end + "|" +
					activity + "|" + teacher + "|" + room +  "|" + isBreak + "\n";
			p.moveToNext();
		}
		w.moveToFirst();
		text += "#![WEEK]\n";
		for (int i = 0; i < w.getCount(); i++) {
			String key = null;
			if (!"".equals(w.getString(1))) {
				key = w.getString(1);
			}
			text += key + "|" + w.getInt(2) + "\n";
			w.moveToNext();
		}
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
		        File root = new File(Environment.getExternalStorageDirectory(), "WGSB/backup");
		        if (! root.exists()) {
		            boolean check = root.mkdirs();
		            if (! check) {
		            	Toast.makeText(mContext, root.toString(), Toast.LENGTH_SHORT).show();
		            	throw new IOException();
		            }
		        }
		        File backupFile = new File(root, "backup.txt");
		        if (!backupFile.exists()) backupFile.createNewFile();
		        FileWriter writer = new FileWriter(backupFile);
		        writer.append(text);
		        writer.flush();
		        writer.close();
		        Toast.makeText(mContext, "Backup successful!", Toast.LENGTH_SHORT).show();
		    } catch(IOException e) {
		         e.printStackTrace();
		         Toast.makeText(mContext, "Backup failed - If you can see this, report it as a bug!", Toast.LENGTH_SHORT).show();
		    }
		} else {
			Toast.makeText(mContext, "Backup failed - Your SD card couldn't be written to", Toast.LENGTH_SHORT).show();
		}
	}
	
	static void restore(Context mContext) {
		ProgressDialog pd = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
		pd.setMessage("Restoring...");
		pd.show();
		String currentTable = "";
		try {
			File dir = new File(Environment.getExternalStorageDirectory(), "WGSB/backup");
			if (! dir.exists()) {
				Toast.makeText(mContext, "Restore failed - Directory not found (/sdcard/WGSB/backup/", Toast.LENGTH_SHORT).show();
			}
			File file = new File(dir, "backup.txt");
			if (! file.exists()) {
				Toast.makeText(mContext, "Restore failed - Backup file not found (/sdcard/WGSB/backup/backup.txt)", Toast.LENGTH_SHORT).show();
			}
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    ContentResolver cr = mContext.getContentResolver();
		    while ((line = br.readLine()) != null) {
		        if (line.startsWith("#![") && line.endsWith("]")) {
		        	currentTable = line.substring(3, line.length()-1);
		        } else if (currentTable.equals("PERIODS")) {
		        	String[] periodValues = line.split("\\|");
		        	for (int i = 0; i < periodValues.length; i++) {
		        		if (periodValues[i].equals("null")) {
		        			periodValues[i] = "";
		        		}
		        	}
		        	ContentValues values  = new ContentValues();
		        	values.put(TimetableProvider.NAME, periodValues[0]);
		        	values.put(TimetableProvider.DAY, periodValues[1]);
		        	values.put(TimetableProvider.START, Integer.parseInt(periodValues[2]));
		        	values.put(TimetableProvider.END, Integer.parseInt(periodValues[3]));
		        	values.put(TimetableProvider.ACTIVITY, periodValues[4]);
		        	values.put(TimetableProvider.TEACHER, periodValues[5]);
		        	values.put(TimetableProvider.ROOM, periodValues[6]);
		        	values.put(TimetableProvider.BREAK, Integer.parseInt(periodValues[7]));
		        	cr.insert(TimetableProvider.PERIODS_URI, values);
		        } else if (currentTable.equals("WEEK")) {
		        	String[] weekValues = line.split("\\|");
		        	for (int i = 0; i < weekValues.length; i++) {
		        		if (weekValues[i].equals("null")) {
		        			weekValues[i] = "";
		        		}
		        	}
		        	ContentValues values = new ContentValues();
		        	values.put(TimetableProvider.KEY, weekValues[0]);
		        	values.put(TimetableProvider.NUM, Integer.parseInt(weekValues[1]));
		        	cr.insert(TimetableProvider.WEEK_URI, values);
		        } else {
		        	Toast.makeText(mContext, "Restore failed - Invalid backup file", Toast.LENGTH_SHORT).show();
		        }
		        Toast.makeText(mContext, "Restore successful!", Toast.LENGTH_SHORT);
		    }
		    br.close();
		} catch (IOException e) {
		    e.printStackTrace();
		    Toast.makeText(mContext, "Restore failed - Your SD card couldn't be read from", Toast.LENGTH_LONG).show();
		}
		pd.dismiss();
	}
}