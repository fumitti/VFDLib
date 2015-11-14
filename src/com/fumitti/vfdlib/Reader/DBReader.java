package com.fumitti.vfdlib.Reader;

import com.fumitti.vfdlib.VFDNewsPanel;

import java.sql.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fumitti on 2015/07/24.
 */
public class DBReader extends TimerTask implements ReaderInterface {
    private final Connection conn;
    public DBReader(){
        Connection conn1;
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            Properties props = new Properties();
            props.put("user",       "");                 // 任意
            props.put("password",   "");                  // 任意
            conn1 = DriverManager.getConnection("jdbc:mysql://192.168.1.0:3306/DBReader",props);
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
            Timer timer = new Timer("DBReader");
            timer.schedule(task, 1000, 1000);
        }
    }

    @Override
    public void getinfo() {
        try {
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery("SELECT * FROM DBReader");
            if(rs.next()) {
                String desc = rs.getString(1);
                boolean isFirst = rs.getBoolean(2);
                boolean isCrit = rs.getBoolean(3);
                rs.deleteRow();
                rs.close();
                if (!isCrit){
                    if (isFirst) {
                        VFDNewsPanel.newsQue.add(0, desc);
                    } else {
                        VFDNewsPanel.newsQue.add(desc);
                    }
                }else{
                    if (isFirst) {
                        VFDNewsPanel.prioQue.add(0, desc);
                    } else {
                        VFDNewsPanel.prioQue.add(desc);
                    }
                }
            }else{
                rs.close();
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
