package com.fumitti.vfdlib.Reader;

import com.fumitti.vfdlib.VFDNewsPanel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fumitti on 2015/06/11.
 */
public class timeSignal extends TimerTask implements ReaderInterface {

    public timeSignal(){
        TimerTask task = this;
        Timer timer = new Timer("時報");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.MILLISECOND, 0);
        timer.scheduleAtFixedRate(task, new Date(calendar.getTimeInMillis()+3601000), 3600000);
    }

    @Override
    public void getinfo() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm");
        VFDNewsPanel.newsQue.add("現在時刻:"+df.format(date));
    }

    @Override
    public void run() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm");
        VFDNewsPanel.newsQue.add(0,"現在時刻:"+df.format(date));
    }
}
