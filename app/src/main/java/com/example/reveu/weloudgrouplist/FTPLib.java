package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

class FTPLib
{
    private Context context;
    private static String serverIP;
    private static String user = "";
    private static String password = "";
    private static final int port = 21;
    private static String defaultPath;
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

        user = context.getText(R.string.FTP_ID).toString();
        password = context.getText(R.string.FTP_PASSWORD).toString();

        new FTPTask().execute("*Connect");
    }

    boolean isConnect()
    {
        try
        {
            return new FTPTask().execute("*isConnect").get().equals("true");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

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

    void rename(String targetFileName, String changeFileName)
    {
        try
        {
            ftpClient.rename(targetFileName, changeFileName);
        }
        catch(IOException ioe)
        {
            System.out.println("FTP_RENAME_ERR : 이름 변경 실패");
        }
    }

    void makeDirectory(String folderName)
    {
        new FTPTask().execute("*MakeDirectory", folderName);
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

    public boolean isDirectoryDefault(String path)
    {
        return path.equals(defaultPath);
    }

    private String fileExistEvent(final String targetFile, final String downloadFileName, final String downloadPath)
    {
        final CharSequence[] items = {context.getText(R.string.text_overwrite).toString(), context.getText(R.string.text_rename).toString(), context.getText(R.string.text_cancel).toString()};

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle(context.getText(R.string.text_fileisexists))
        .setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int index)
            {
                // OverWrite Event
                if(items[index].equals(context.getText(R.string.text_overwrite).toString()))
                {
                    downloadFile(true, targetFile, "", downloadPath);
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
                                downloadFile(false, targetFile, fileName, downloadPath);
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
        });

        alert.show();

        return "";
    }

    private class FTPTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            switch(params[0])
            {
                case "*Connect" :
                    FTPConnect();
                    break;

                case "*Disconnect" :
                    FTPDisconnect();
                    break;

                case "*MakeDirectory" :
                    FTPMakeDirectory(params[1]);
                    break;

                case "*GetDirectory" :
                    return FTPgetDirectory();

                case "*GetFileList" :
                    FTPgetFileList();
                    return "*GetFileListComplete";

                case "*GetFileModificationTime" :
                    return FTPgetFileModificationTime(params[1]);

                case "*ChangeWorkingDirectory" :
                    FTPchangeWorkingDirectory(params[1]);
                    break;

                case "*Delete" :
                    FTPdelete(params[1]);
                    break;

                case "*RemoveDir" :
                    FTPRemoveDirectory(params[1]);
                    break;

                case "*isConnect" :
                    return (FTPIsConnect() ? "true" : "false");

                case "*DownloadFile" :
                    return FTPDownloadFile(params[1], params[2], params[3]);
            }
            return params[0];
        }

        private boolean FTPConnect()
        {
            boolean loginResult = false;

            try
            {
                ftpClient = new FTPClient();
                ftpClient.setControlEncoding("UTF-8");

                ftpClient.connect(serverIP, port);
                loginResult = ftpClient.login(user, password);
                ftpClient.changeWorkingDirectory(defaultPath);
            }
            catch(IOException e)
            {
                System.out.println("FTP_LOGIN_ERR : " + e.getMessage());
            }

            if(!loginResult)
            {
                System.out.println("FTP_LOGIN_ERR : 로그인 실패");
            }
            else
            {
                System.out.println("FTP_LOGIN_SUCCESS");
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

        private boolean FTPIsConnect()
        {
            return ftpClient.isConnected();
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

        private void FTPgetFileList()
        {
            FTPFile[] files;
            try
            {
                files = ftpClient.listFiles();
                System.out.println("FTP_FILELIST_SUCCESS");
                ftpFile = files;
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_FILELIST_ERR : 리스트 가져오기 실패");
            }
        }

        private String FTPgetDirectory()
        {
            String result;
            try
            {
                result = ftpClient.printWorkingDirectory();
                return result;
            }
            catch(IOException ioe)
            {
                System.out.println("FTP_GETDIRECTORY_ERR : 작업 디렉토리 가져오기 실패");
            }
            return "";
        }

        private String FTPgetFileModificationTime(String fileName)
        {
            String dir = FTPgetDirectory();

            try
            {
                return ftpClient.getModificationTime(dir + "/" + fileName);
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
                return null;
            }
        }

        private void FTPchangeWorkingDirectory(String path)
        {
            try
            {
                if(path.equals("..") && isDirectoryDefault(FTPgetDirectory()))
                    System.out.println("FTP_CHANGEWORKINGDIRECTORY_ERR : 권한이 거부되었습니다.");
                else
                    ftpClient.changeWorkingDirectory(path);
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

        private String FTPDownloadFile(String type, String targetFile, String downloadPath)
        {
            boolean isOverWrite = (type.equals("OverWrite") ? true : false);

            try
            {
                File downloadFile = new File(downloadPath);
                if(downloadFile.exists() && !isOverWrite)
                {
                    return "Exists";
                }
                else
                {
                    OutputStream outputStream = new FileOutputStream(downloadFile);
                    ftpClient.retrieveFile(targetFile, outputStream);
                    return "Success";
                }
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
    }
}
