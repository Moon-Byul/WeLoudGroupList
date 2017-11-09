package com.example.reveu.weloudgrouplist;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;

import static android.R.attr.type;
import static android.content.ContentValues.TAG;

class FTPLib
{
    private Context context;
    private static String serverIP;
    private static String user = "";
    private static String password = "";
    private static final int port = 21;
    private static String defaultPath;
    private static String workingPath;
    private static FTPClient ftpClient;

    private FTPFile[] ftpFile;

    FTPLib()
    {
    }

    FTPLib(String serverIPInp, String defaultPathInp, Context context)
    {
        this.context = context;
        serverIP = serverIPInp;
        defaultPath = defaultPathInp;
        workingPath = "";

        user = context.getText(R.string.FTP_ID).toString();
        password = context.getText(R.string.FTP_PASSWORD).toString();
    }

    void execute(FTPCMD type, String... ftpCommand)
    {
        String[] temp = new String[]{type.getName()};
        String[] result = new String[ftpCommand.length + 1];
        System.arraycopy(temp, 0, result, 0, 1);
        System.arraycopy(ftpCommand, 0, result, 1, ftpCommand.length);

        new FTPTask().execute(result);
    }

    void execute(Activity activity, String methodName, FTPCMD type, String... ftpCommand)
    {
        String[] temp = new String[]{type.getName()};
        String[] result = new String[ftpCommand.length + 1];
        System.arraycopy(temp, 0, result, 0, 1);
        System.arraycopy(ftpCommand, 0, result, 1, ftpCommand.length);

        new FTPTask(activity, methodName).execute(result);
    }

    void execute(android.support.v4.app.Fragment fragment, String methodName, FTPCMD type, String... ftpCommand)
    {
        String[] temp = new String[]{type.getName()};
        String[] result = new String[ftpCommand.length + 1];
        System.arraycopy(temp, 0, result, 0, 1);
        System.arraycopy(ftpCommand, 0, result, 1, ftpCommand.length);

        new FTPTask(fragment, methodName).execute(result);
    }

    void downloadFile(final String targetFile, final String downloadFileName, final String downloadPath)
    {
        final String resultPath;
        if(downloadFileName.equals(""))
            resultPath = downloadPath + "/" + targetFile;
        else
            resultPath = downloadPath + "/" + downloadFileName;

        File downloadFile = new File(resultPath);
        if(downloadFile.exists())
        {
            final CharSequence[] items = {context.getText(R.string.text_overwrite).toString(), context.getText(R.string.text_rename).toString(), context.getText(R.string.text_cancel).toString()};

            TextView tvFileName = new TextView(context);

            if(downloadFileName.equals(""))
                tvFileName.setText(context.getText(R.string.text_file) + " : " + targetFile);
            else
                tvFileName.setText(context.getText(R.string.text_file) + " : " + downloadFileName);

            tvFileName.setTextSize(12.0f);
            tvFileName.setGravity(Gravity.CENTER);

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setView(tvFileName)
            .setTitle(context.getText(R.string.text_fileisexists))
            .setItems(items, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int index)
                {
                    // OverWrite Event
                    if(items[index].equals(context.getText(R.string.text_overwrite).toString()))
                    {
                        execute(FTPCMD.DownloadFile, targetFile, resultPath);
                    }
                    // Rename Event
                    else if(items[index].equals(context.getText(R.string.text_rename).toString()))
                    {
                        final EditText name = new EditText(context);

                        // Set Default Text to EditText
                        if(downloadFileName.equals(""))
                            name.setText(targetFile);
                        else
                            name.setText(downloadFileName);

                        // Create Rename AlertDialog
                        AlertDialog.Builder alertRename = new AlertDialog.Builder(context);

                        alertRename.setMessage(context.getText(R.string.text_rename).toString())
                        .setView(name)
                        .setPositiveButton(context.getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String fileName = name.getText().toString();

                                if(!fileName.equals(""))
                                {
                                    downloadFile(targetFile, fileName, downloadPath);
                                }
                                else
                                {
                                    Toast.makeText(context, context.getText(R.string.text_file).toString() + context.getText(R.string.text_nameisempty).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })

                        .setNegativeButton(context.getText(R.string.text_cancel).toString(), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });

                        alertRename.show();
                    }
                }
            })
            .show();
        }
        else
            execute(FTPCMD.DownloadFile, targetFile, resultPath);
    }

    void delete(FTPFile targetFile)
    {
        try
        {
            if(targetFile.isDirectory())
                execute(FTPCMD.RemoveDir, targetFile.getName());
            else
                execute(FTPCMD.Delete, targetFile.getName());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    boolean isDirectoryDefault(String path)
    {
        Log.d("Twily", path + "::" + defaultPath);
        return path.equals(defaultPath);
    }

    String getWorkingPath()
    {
        return workingPath;
    }

    FTPFile[] getFTPFile()
    {
        return ftpFile;
    }

    String getExt(FTPFile file)
    {
        if(file.isDirectory())
            return "folder";
        if(file.isFile())
        {
            return new StringLib().fileExt(file.getName());
        }
        return null;
    }

    int getExtDrawable(FTPFile file)
    {
        if(file.isDirectory())
            return R.drawable.folder;
        if(file.isFile())
        {
            return new ExtLib().getDrawableExt(new StringLib().fileExt(file.getName()));
        }
        return R.drawable.blank_file;
    }

    /*
    void disconnect()
    {
        new FTPTask().execute("*Disconnect");
    }

    FTPFile[] getFileList()
    {
        try
        {
            if (new FTPTask().execute("*GetFileList").get().equals("*GetFileListComplete"))
            {
                return ftpFile;
            }

            return null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    FTPFile[] getFileList(Activity activity, String methodName)
    {
        try
        {
            if (new FTPTask(activity, methodName).execute("*GetFileList").get().equals("*GetFileListComplete"))
            {
                return ftpFile;
            }

            return null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    String getFileModificationTime(String fileName)
    {
        try
        {
            return new FTPTask().execute("*GetFileModificationTime", fileName).get();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    String getDirectory()
    {
        try
        {
            return new FTPTask().execute("*GetDirectory").get();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    void changeWorkingDirectory(String path)
    {
        new FTPTask().execute("*ChangeWorkingDirectory", path);
    }

    boolean rename(String targetFileName, String changeFileName)
    {
        try
        {
            String result = new FTPTask().execute("*Rename", targetFileName, changeFileName).get();

            if(result.equals("")) // 에러
                return false;
            else
                return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    boolean makeDirectory(String folderName)
    {
        try
        {
            String result = new FTPTask().execute("*MakeDirectory", folderName).get();

            if(result.equals("")) // 에러
                return false;
            else
                return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    void delete(FTPFile targetFile)
    {
        try
        {
            if(targetFile.isDirectory())
                new FTPTask().execute("*RemoveDir", targetFile.getName());
            else
                new FTPTask().execute("*Delete", targetFile.getName());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    void uploadFile(String filePath)
    {
        try
        {
            File uploadFile = new File(filePath);
            InputStream inputStream = new FileInputStream(uploadFile);
            ftpClient.storeFile(uploadFile.getName(), inputStream);
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println("FTP_UPLOADFILE_ERR : 파일이 존재하지 않습니다.");
        }
        catch(IOException ioe)
        {
            System.out.println("FTP_UPLOADFILE_ERR : 파일 업로드 실패");
        }
    }

    // isOverWrite = 덮어쓰기를 할 것인지.
    // downloadFileName = 공백이면 targetFile의 이름을 그대로, 아닐 경우 다른 이름으로 저장 (rename)
    void downloadFile(boolean isOverWrite, String targetFile, String downloadFileName, String downloadPath)
    {
        String resultPath;
        if(downloadFileName.equals(""))
            resultPath = downloadPath + "/" + targetFile;
        else
            resultPath = downloadPath + "/" + downloadFileName;

        try
        {
            String strOverWrite = "";
            if(isOverWrite)
                strOverWrite = "OverWrite";

            String result = new FTPTask().execute("*DownloadFile", strOverWrite, targetFile, resultPath).get();

            if(result.equals("Exists"))
            {
                fileExistEvent(targetFile, downloadFileName, downloadPath);
            }
            else
            {
                if(result.equals("Success"))
                    Toast.makeText(context, context.getText(R.string.text_downloadsuccess), Toast.LENGTH_SHORT).show();
                else if(result.equals("Error_fnfe"))
                    Toast.makeText(context, context.getText(R.string.text_fileisntexists), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getText(R.string.text_downloadfail), Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    */

    private class FTPTask extends AsyncTask<String, Integer, String>
    {
        private Activity activity = null;
        private android.support.v4.app.Fragment fragment = null;
        private String methodCall = "";
        private String saveParam = "";

        FTPTask()
        {
        }

        FTPTask(android.support.v4.app.Fragment fragmentInput, String methodName)
        {
            fragment = fragmentInput;
            methodCall = methodName;
        }

        FTPTask(Activity activityInput, String methodName)
        {
            activity = activityInput;
            methodCall = methodName;
        }

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(context, context.getText(R.string.text_loading).toString(), null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if(progressDialog != null)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            switch(saveParam)
            {
                case "RN":
                    Toast.makeText(context, context.getText(R.string.text_nameisexists), Toast.LENGTH_SHORT).show();
                    break;

                case "DOWN":
                {
                    if(result.equals(""))
                    {
                        Toast.makeText(context, context.getText(R.string.text_downloadsuccess), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else if(result.equals("Error_fnfe"))
                        Toast.makeText(context, context.getText(R.string.text_fileisntexists), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, context.getText(R.string.text_downloadfail), Toast.LENGTH_SHORT).show();

                    break;
                }
            }

            if(activity != null || fragment != null)
            {
                try
                {
                    Method method;
                    if(activity != null)
                    {
                        method = activity.getClass().getMethod(methodCall);
                        method.invoke(activity);
                    }
                    else
                    {
                        method = fragment.getClass().getMethod(methodCall);
                        method.invoke(fragment);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(String... params)
        {
            String result = saveParam = params[0];
            try
            {
                if(FTPConnect())
                {
                    switch(params[0])
                    {
                        case "MD":
                            FTPMakeDirectory(params[1]);
                            break;

                        case "GFL":
                            FTPgetFileList();
                            break;

                        case "CWD":
                            FTPchangeWorkingDirectory(params[1]);
                            break;

                        case "DEL":
                            FTPdelete(params[1]);
                            break;

                        case "RN":
                            result = FTPRename(params[1], params[2]);
                            break;

                        case "RDIR":
                            FTPRemoveDirectory(params[1]);
                            break;

                        case "DOWN":
                            result = FTPDownloadFile(params[1], params[2]);
                            break;
                    }
                    FTPDisconnect();
                }
            }
            catch (ConnectException ce)
            {
                ce.printStackTrace();
                Toast.makeText(context, context.getText(R.string.error_connection), Toast.LENGTH_SHORT).show();
            }
            finally
            {
                return result;
            }
        }

        private boolean FTPConnect() throws ConnectException
        {
            boolean loginResult = false;

            try
            {
                ftpClient = new FTPClient();
                ftpClient.setControlEncoding("UTF-8");

                ftpClient.connect(serverIP, port);
                loginResult = ftpClient.login(user, password);

                ftpClient.enterLocalPassiveMode();

                if(workingPath.equals(""))
                    FTPchangeWorkingDirectory(defaultPath);
                else
                    FTPchangeWorkingDirectory(workingPath);
            }
            catch(IOException e)
            {
                System.out.println("FTP_LOGIN_ERR : " + e.getMessage());
                throw new ConnectException();
            }

            if(!loginResult)
            {
                System.out.println("FTP_LOGIN_ERR : 로그인 실패");
                throw new ConnectException();
            }

            return loginResult;
        }

        private void FTPDisconnect()
        {
            try
            {
                ftpClient.disconnect();
            }
            catch (IOException ignored){}
        }

        private void FTPMakeDirectory(String folderName)
        {
            try
            {
                ftpClient.makeDirectory(folderName);
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_MAKEDIRECTORY_ERR : 폴더 생성 실패");
            }
        }

        private String FTPRename(String targetFileName, String changeFileName)
        {
            try
            {
                if(ftpClient.rename(targetFileName, changeFileName))
                    return "Success";
                else
                    return "";
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_RENAME_ERR : 이름 변경 실패");
                return "";
            }
        }

        private void FTPgetFileList()
        {
            FTPFile[] files;
            try
            {
                files = ftpClient.listFiles();

                for(FTPFile file : files)
                {
                    file.setTimestamp(FTPgetFileModificationTime(file.getName()));
                }

                ftpFile = files;
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_FILELIST_ERR : 리스트 가져오기 실패");
            }
        }

        private Calendar FTPgetFileModificationTime(String fileName)
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat ftpFormat = new SimpleDateFormat("yyyyMMddHHmmss");

            try
            {
                cal.setTime(ftpFormat.parse(ftpClient.getModificationTime(workingPath + "/" + fileName)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return cal;
        }

        private void FTPchangeWorkingDirectory(String path)
        {
            try
            {
                if(path.equals("..") && isDirectoryDefault(workingPath))
                    System.out.println("FTP_CHANGEWORKINGDIRECTORY_ERR : 권한이 거부되었습니다.");
                else
                {
                    ftpClient.changeWorkingDirectory(path);
                    workingPath = ftpClient.printWorkingDirectory();
                }
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_CHANGEWORKINGDIRECTORY_ERR : 디렉토리 변경 실패");
            }
        }

        private void FTPdelete(String targetFile)
        {
            try
            {
                ftpClient.deleteFile(targetFile);
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_DELETE_ERR : 파일 삭제 실패");
            }
        }

        private String FTPDownloadFile(final String targetFile, String downloadPath)
        {
            try
            {
                /*
                 * progressDialog는 다운로드 할 때 필요없으니까 비활성화.
                 */
                if(progressDialog != null)
                {
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                }

                final NotificationManager nmDownload = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                final NotificationCompat.Builder ncBuilder = new NotificationCompat.Builder(context);

                ncBuilder.setContentTitle("Weloud")
                .setContentText(context.getText(R.string.text_downloadinprogress) + "(0%) - " + targetFile)
                .setOngoing(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.download); // 요거 안넣어주면 notification 안생김.

                File downloadFile = new File(downloadPath);
                OutputStream outputStream = new FileOutputStream(downloadFile);

                final long size = getFileSize(targetFile);

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setCopyStreamListener(new CopyStreamAdapter()
                {
                    int percentPrev = 0;

                    @Override
                    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize)
                    {
                        /*
                         * 바이트가 전송될 때 마다 실행되는 메소드.
                         */
                        super.bytesTransferred(totalBytesTransferred, bytesTransferred, streamSize);

                        int percent = (int) (totalBytesTransferred * 100 / size);

                        /*
                         * Percent 단위로 갱신해줘야함. 안그러면 위에서 말했듯이 바이트 전송될때마다 UI 갱신해서 핸드폰 배터리 광탈남
                         * NotificationCombat을 Update 한 뒤에, NotificationManager에 Notify를 하는 순서로 Update를 해줘야함.
                         */
                        if(percentPrev < percent)
                        {
                            percentPrev = percent;
                            ncBuilder.setProgress(100, percent, false)
                            .setContentText(context.getText(R.string.text_downloadinprogress) + "(" + percent + "%) - " + targetFile);
                            nmDownload.notify(1, ncBuilder.build());
                        }
                        if (totalBytesTransferred == size) // 다운 다 됨 ㅎ
                        {
                            removeCopyStreamListener(this);
                            ncBuilder.setContentText(context.getText(R.string.text_downloadcomplete) + " : " + targetFile)
                            .setProgress(0, 0, false)
                            .setOngoing(false)
                            .setAutoCancel(false);
                            nmDownload.notify(1, ncBuilder.build());
                        }
                    }
                });

                ftpClient.retrieveFile(targetFile, outputStream);
                return "";
            }
            catch(FileNotFoundException fnfe)
            {
                System.out.println("FTP_DOWNLOADFILE_ERR : 파일이 존재하지 않습니다.");
                fnfe.printStackTrace();
                return "Error_fnfe";
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_DOWNLOADFILE_ERR : 파일 다운로드 실패");
                ioe.printStackTrace();
                return "Error_ioe";
            }
        }

        private void FTPRemoveDirectory(String path)
        {
            /*
                폴더를 완전히 삭제하는 함수 (안에 있는 파일,폴더 까지 모두)
                재귀 함수를 이용하였다.
             */
            try
            {
                FTPFile[] files = ftpClient.listFiles(path);
                if(files.length > 0)
                {
                    for(FTPFile ftpFile : files)
                    {
                        if (ftpFile.isDirectory())
                        {
                            FTPRemoveDirectory(path + "/" + ftpFile.getName());
                        }
                        else
                        {
                            String filePath = path + "/" + ftpFile.getName();
                            ftpClient.deleteFile(filePath);
                        }
                    }
                }
                ftpClient.removeDirectory(path);
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_REMOVEDIR_ERR : 폴더 삭제 실패");
            }
        }

        private long getFileSize(String filePath)
        {
            long fileSize = 0;

            try
            {
                FTPFile[] files = ftpClient.listFiles(filePath);

                if (files.length == 1 && files[0].isFile())
                    fileSize = files[0].getSize();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return fileSize;
        }
    }
}