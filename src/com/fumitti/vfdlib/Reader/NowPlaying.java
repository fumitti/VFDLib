package com.fumitti.vfdlib.Reader;

import com.fumitti.vfdlib.VFDNewsPanel;

import java.sql.*;
import java.util.*;

/**
 * Created by fumitti on 2015/07/24.
 */
public class NowPlaying extends TimerTask implements ReaderInterface {
    private final Connection conn;
    public NowPlaying(){
        Connection conn1;
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            Properties props = new Properties();
            props.put("user",       "");                 // 任意
            props.put("password",   "");                  // 任意
            conn1 = DriverManager.getConnection("jdbc:mysql://192.168.1.0:3306/jukebox",props);
        } catch (ClassNotFoundException e) {
            conn1 = null;
            e.printStackTrace();
        } catch (SQLException e) {
            conn1 = null;
            e.printStackTrace();
        }
        conn = conn1;
        getinfo();
        if (conn != null) {
            TimerTask task = this;
            Timer timer = new Timer("JukeBoxFetcher");
            timer.schedule(task, 1000, 1000);
        }
    }

    @Override
    public void getinfo() {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT songID,isRequest,isPlaying,isShowed,Requester FROM playQue");
            if(rs.next()){
                int songID = rs.getInt(1);
                boolean isRequest = rs.getBoolean(2);
                boolean isPlaying = rs.getBoolean(3);
                boolean isShowed = rs.getBoolean(4);
                String requester = rs.getString(5);
                if(isPlaying&&!isShowed) {
                    st = conn.createStatement();
                    String sql = "SELECT songTitle,songArtist FROM songDB WHERE songDB.songID = " + songID;
                    rs = st.executeQuery(sql);
                    if (rs.next()) {
                        String title = rs.getString(1);
                        String artist = rs.getString(2);
                        if (isRequest) {
                            System.out.println("NowPlaying:" + title + " - " + artist + " by" + requester);
                            VFDNewsPanel.newsQue.add(0, "NowPlaying:" + title + " - " + artist + " by" + requester);
                        } else {
                            VFDNewsPanel.newsQue.add(0, "NowPlaying:" + title + " - " + artist);
                        }
                    }
                    st.executeUpdate("UPDATE playQue SET isShowed = 1 WHERE songID = "+songID);
                }
            }else{
                //VFDNewsPanel.newsQue.add("No Playing...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        getinfo();
    }
}
