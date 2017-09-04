package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.os.AsyncTask;

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

    public void disconnect()
    {
        new FTPTask().execute("*Disconnect");
    }

    public FTPFile[] getFileList()
    {
        FTPFile[] files = null;
        try
        {
            files = this.ftpClient.listFiles();
            System.out.println("FTP_FILELIST_SUCCESS");
            return files;
        }
        catch(IOException ioe)
        {
            System.out.println("FTP_FILELIST_ERR : 리스트 가져오기 실패");
        }
        return null;
    }

    public String getDirectory()
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

    public void changeWorkingDirectory(String path)
    {
        try
        {
            if(path.equals("..") && getDirectory().equals(defaultPath))
                System.out.println("FTP_CHANGEWORKINGDIRECTORY_ERR : 권한이 거부되었습니다.");
            else
                ftpClient.changeWorkingDirectory(path);
        }
        catch(IOException ioe)
        {
            System.out.println("FTP_CHANGEWORKINGDIRECTORY_ERR : 디렉토리 변경 실패");
        }
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

    public void delete(String targetFile)
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
            return params[0];
        }

        private void FTPConnect()
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
    }
}
