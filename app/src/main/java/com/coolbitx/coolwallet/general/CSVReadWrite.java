package com.coolbitx.coolwallet.general;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.coolbitx.coolwallet.bean.User;
import com.snscity.egdwlib.utils.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CSVReadWrite {

    private final static String DIR = "/CoolWallet";
    private FileWriter writer;
    private BufferedReader reader = null;
    private File saveFile;
    private SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private String mFileName = "";
    private String mFilePath = "";
    private String readerFileName = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
//    BC1DB bc1db;

    public CSVReadWrite(Context context) {
        this.mContext = context;
        sharedPreferences = mContext.getSharedPreferences("card", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        makeDocument();
    }

    public String getSaveFileName() {
        return mFileName;
    }

    public String getSaveFileDir() {
        return mFilePath;
    }

    public void setSaveFileName(String filename, boolean append) {
        try {
            mFileName = filename;
            mFilePath = DIR;
            saveFile = new File(getSDcardDir().getPath() + DIR, filename);
            writer = new FileWriter(saveFile, append);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeFile(String filename) throws IOException {

        File dir = Environment.getExternalStorageDirectory();
        String path = dir.getPath() + DIR+"/"+filename;

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

    }

    public boolean setReadFileName(String filename) throws IOException {
        try {
            reader = new BufferedReader(new FileReader(getSDcardDir().getPath() + DIR + "/" + filename));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
//        readerFileName = filename.substring(filename.indexOf("-") + 1, filename.indexOf("-", filename.indexOf("-") + 1)).replace(".csv", "");
        readerFileName =  mFileName;
        Log.e("setReadFileName", "read csv file and insert into table : " + readerFileName);
        if (reader == null)
            return false;
        else
            return true;
    }

    public void closeSaveFile() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void closeReader() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public void saveLoginToCSV(User user) throws IOException {

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(sdfDateTime.format(new Date()) + ",");

            sb.append(PublicPun.user.getMacID() + ",");
            sb.append(PublicPun.user.getUuid() + ",");
            sb.append(PublicPun.user.getOtpCode() + ",");

            sb.append("\n");
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
           closeSaveFile();
        }
    }



    public void saveToCSVwithIrrIndex(float[] data0, float[] data1) throws IOException {

        try {
            for (int i = 0; i < data1.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(Float.toString(data0[i]) + ",");
                sb.append(Float.toString(data1[i]) + ",");
                sb.append("\n");
                writer.write(sb.toString());
                writer.flush();
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveToCSVwithMultiple(float[] data) throws IOException {

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(sdfDateTime.format(new Date()) + ",");
            for (int i = 0; i < data.length; i++) {

                sb.append(Float.toString(data[i]) + ",");
            }
            sb.append("\n");
            writer.write(sb.toString());
            writer.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveToCSVwithMultiple(int[] data) throws IOException {

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(sdfDateTime.format(new Date()) + ",");
            for (int i = 0; i < data.length; i++) {

                sb.append(Integer.toString(data[i]) + ",");
            }
            sb.append("\n");
            writer.write(sb.toString());
            writer.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveToRRIsingle(float period, float amp) throws IOException {

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(sdfDateTime.format(new Date()) + ",");
            sb.append(Float.toString(period) + ",");
            sb.append(Float.toString(amp) + ",");
            sb.append("\n");

            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void saveHistoryToCSV(ArrayList<String> arraylist) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arraylist.size(); i++) {
                sb.append(arraylist.get(i) + ",");
            }
            sb.append("\n");

            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    public void readHistoryFromCSV(Context context) throws IOException {
//        String line = "";
//        ArrayList<String> arraylist = new ArrayList<String>();
//        while ((line = reader.readLine()) != null) {
//            String[] buffer = line.split(",");
//            arraylist = new ArrayList<String>();
//            for (int i = 0; i < buffer.length; i++) {
//                arraylist.add(buffer[i]);
//            }
//            try {
//                bc1db = new BC1DB(context);
//                bc1db.open(context);
//                bc1db.readFromCSV(readerFileName, arraylist);
//
//            } catch (Exception e) {
//                Log.e("readHistoryFromCSV", "insert to sqlite error : " + e.getMessage());
//                e.printStackTrace();
//            } finally {
//                bc1db.close();
//            }
//        }
//    }

//    public void readSleepTempFromCSV(Context context) throws IOException {
//        String line = "";
//        ArrayList<String> arraylist = new ArrayList<String>();
//        while ((line = reader.readLine()) != null) {
//            String[] buffer = line.split(",");
//            arraylist = new ArrayList<String>();
//            for (int i = 0; i < buffer.length; i++) {
//                arraylist.add(buffer[i]);
//            }
//            try {
//                bc1db = new BC1DB(context);
//                bc1db.open(context);
//                bc1db.readFromCSV(readerFileName, arraylist);
//
//                if (readerFileName.equals("Snf_history")) {
//                    bc1db.readFromCSV("db_BC1_SNF_HISTORY", arraylist);
//                }
////				else if(readerFileName.equals("Ma_history")){
////					bc1db.readFromCSV("db_BC1_MA_HISTORY", arraylist);
////				}
////				else if(readerFileName.equals("Enh_history")){
////					bc1db.readFromCSV("db_BC1_ENH_HISTORY", arraylist);
////				}
////				else if(readerFileName.equals("At_history")){
////					bc1db.readFromCSV("db_BC1_AT_HISTORY", arraylist);
////				}
////				else if(readerFileName.equals("Sa_history")){
////					bc1db.readFromCSV("db_BC1_SA_HISTORY", arraylist);
////				}
//            } catch (Exception e) {
//                Log.e("readHistoryFromCSV", "insert to sqlite error : " + e.getMessage());
//                e.printStackTrace();
//            } finally {
//                bc1db.close();
//            }
//        }
//    }

    public Boolean readFromCSV(String carId) throws IOException {
        String line = "";

        while ((line = reader.readLine()) != null) {
            LogUtil.i("CSV line="+line);
            String[] buffer = line.split(",");
            LogUtil.i("CSV data="+buffer[0]);
            LogUtil.i("CSV data=" + buffer[1]);
            LogUtil.i("CSV data=" + buffer[2]);
            LogUtil.i("CSV data=" + buffer[3]);

            if(carId.equals(buffer[1])){
                LogUtil.i("找到 readFromCSV! 已驗證過");
                editor.putString("uuid", buffer[2]);
                editor.putString("optCode", buffer[3]);
                editor.commit();
                return false;
            }

        }
        return true;
    }

    public ArrayList<Float> readFromCSV(int colume) throws IOException {
        String line = "";
        ArrayList<Float> value = new ArrayList<Float>();

        while ((line = reader.readLine()) != null) {
            String[] buffer = line.split(",");
            value.add(Float.parseFloat(buffer[colume]));
        }
        return value;
    }

    public ArrayList<Double> readFromCSVDouble(int colume) throws IOException {
        String line = "";
        ArrayList<Double> value = new ArrayList<Double>();

        while ((line = reader.readLine()) != null) {
            String[] buffer = line.split(",");
            value.add(Double.parseDouble(buffer[colume]));
        }
        return value;
    }

    public ArrayList<Float> readFromCSV_Column0() throws IOException {
        String line = "";
        ArrayList<Float> value = new ArrayList<Float>();
        while ((line = reader.readLine()) != null) {
            String[] buffer = line.split(",");
            value.add(Float.parseFloat(buffer[0]));
        }
        return value;
    }

    //create of CSV header
    public void writeCSVHeader(String header0, String header1, String header2) throws IOException {
        String line = String.format("%s,%s,%s\n", header0, header1, header2);
        writer.write(line);
    }

    public void makeDocument() {
        File dir = Environment.getExternalStorageDirectory();
        String path = dir.getPath() + DIR;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public File getSDcardDir() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        File sdCardDir = null;
        if (sdCardExist) {
            sdCardDir = Environment.getExternalStorageDirectory();
        }
        return sdCardDir;
    }


}
