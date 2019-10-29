package com.ztf.nineimg;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 检测sd卡
 */
public class SdCardTools {
    /**
     * 检查是否存在SDCard
     *
     * @return
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getCachePath(Context context) {
        String pkgPath = context.getApplicationContext().getPackageCodePath();

        String absPath = context.getApplicationContext().getFilesDir().getAbsolutePath();

        return pkgPath;
    }

    /**
     * 获取sdcard路径
     *
     * @return
     */
    static List<String> paths = null;

    public static String getSDcardPath(int index) {
        if (paths == null) {
            paths = getOutSDPaths();
        }
        if (paths.size() == 0) {
            return "";
        }
        if (paths.size() == 1) {
            return paths.get(0);
        }
        if (index >= 2) {
            return paths.get(paths.size() - 1);
        } else {
            return paths.get(index);
        }
//		String path = Environment.getExternalStorageDirectory().getPath();
//		return path;
    }

    /**
     * 获取所有路径
     *
     * @return
     */
    public static List<String> getOutSDPaths() {
        if (paths == null) {
            paths = new ArrayList<>();
        } else {
            return paths;
        }
        try {
            String path = Environment.getExternalStorageDirectory().getPath();
            paths.add(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;

            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;

                if (line.contains("storage")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        String path = columns[1];
                        String[] as = path.split("/");
                        if (path.contains("sdcard") && !paths.contains(path)) {
                            paths.add(path);
                        }
                        if ((path.contains("0") && path.split("/").length == 4) && !paths.contains(path)) {
                            paths.add(0, path);
                        }
//                        mount = mount.concat("*" + columns[1] + "\n");
                    }
                }
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return paths;
    }

    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }
}
