package com.example.reveu.weloudgrouplist;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPLib
{
    private Context context;
    private static String serverIP;
    private static String user = "";
    private static String password = "";
    private static final int port = 21;
    private static String defaultPath;
    private static FTPClient ftpClient;
    private FTPFile[] ftpFile;

    public FTPLib()
    {

    }

    public FTPLib(String serverIPInp, String defaultPathInp, Context context)
    {
        this.context = context;
        serverIP = serverIPInp;
        defaultPath = defaultPathInp;

        user = context.getText(R.string.FTP_ID).toString();
        password = context.getText(R.string.FTP_PASSWORD).toString();

        new FTPTask().execute("*Connect");
    }

    public boolean isConnect()
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

    public void disconnect()
    {
        new FTPTask().execute("*Disconnect");
    }

    public FTPFile[] getFileList()
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

    public String getFileModificationTime(String fileName)
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

    public String getDirectory()
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

    public void changeWorkingDirectory(String path)
    {
        new FTPTask().execute("*ChangeWorkingDirectory", path);
    }

    public String getExt(FTPFile file)
    {
        if(file.isDirectory())
            return "folder";
        if(file.isFile())
        {
            return new StringLib().fileExt(file.getName());
        }
        return null;
    }

    public int getExtDrawable(FTPFile file)
    {
        if(file.isDirectory())
            return R.drawable.folder;
        if(file.isFile())
        {
            return new ExtLib().getDrawableExt(new StringLib().fileExt(file.getName()));
        }
        return R.drawable.blank_file;
    }

    public void rename(String targetFileName, String changeFileName)
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

    public void makeDirectory(String folderName)
    {
        new FTPTask().execute("*MakeDirectory", folderName);
    }

    public void delete(FTPFile targetFile)
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

    public void uploadFile(String filePath)
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

    public void downloadFile(String targetFile, String downloadPath)
    {
        try
        {
            File downloadFile = new File(downloadPath + "\\" + targetFile);
            OutputStream outputStream = new FileOutputStream(downloadFile);
            ftpClient.retrieveFile(targetFile, outputStream);
        }
        catch(FileNotFoundException fnfe)
        {
            System.out.println("FTP_DOWNLOADFILE_ERR : 파일이 존재하지 않습니다.");
        }
        catch(IOException ioe)
        {
            System.out.println("FTP_DOWNLOADFILE_ERR : 파일 다운로드 실패");
        }
    }

    public boolean isDirectoryDefault(String path)
    {
        return path.equals(defaultPath);
    }

    private class FTPTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            if(params[0].equals("*Connect"))
            {
                FTPConnect();
            }
            else if(params[0].equals("*Disconnect"))
            {
                FTPDisconnect();
            }
            else if(params[0].equals("*MakeDirectory"))
            {
                FTPMakeDirectory(params[1]);
            }
            else if(params[0].equals("*GetFileList"))
            {
                FTPgetFileList();
                return "*GetFileListComplete";
            }
            else if(params[0].equals("*GetDirectory"))
            {
                return FTPgetDirectory();
            }
            else if(params[0].equals("*GetFileModificationTime"))
            {
                return FTPgetFileModificationTime(params[1]);
            }
            else if(params[0].equals("*ChangeWorkingDirectory"))
            {
                FTPchangeWorkingDirectory(params[1]);
            }
            else if(params[0].equals("*Delete"))
            {
                FTPdelete(params[1]);
            }
            else if(params[0].equals("*RemoveDir"))
            {
                FTPRemoveDirectory(params[1]);
            }
            else if(params[0].equals("*isConnect"))
            {
                return (FTPIsConnect() ? "true" : "false");
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

        public void FTPgetFileList()
        {
            FTPFile[] files = null;
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

        public String FTPgetDirectory()
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

        public String FTPgetFileModificationTime(String fileName)
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

        public void FTPchangeWorkingDirectory(String path)
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

        public void FTPdelete(String targetFile)
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

        public void FTPRemoveDirectory(String path)
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
