package com.fumitti.vfdlib.Writer;

import com.fumitti.vfdlib.VFDNewsPanel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fumitti on 2015/05/20.
 */
public class scrollWriter implements WriterInterface{

    private final List<String> ybuf;
    private final boolean blink;

    public scrollWriter(List<String> ybuf) {
        this.ybuf = ybuf;
        this.blink=false;
    }

    public scrollWriter(List<String> ybuf,boolean blink) {
        this.ybuf = ybuf;
        this.blink = blink;
    }

    @Override
    public void run() {
        try {
            int wait = 12;
            int ybufpos = 0;
            int skip = 0;
            if(blink){
                VFDNewsPanel.vfd.brink(2,125,125,0);
            }
            while (true) {
                long now = System.currentTimeMillis();
                String yline;
                try {
                    yline = ybuf.get(ybufpos);
                } catch (IndexOutOfBoundsException e) {
                    yline = null;
                }
                ybufpos++;
                if (yline == null) {
                    yline = "00000000000000000";
                    ybuf.clear();
                    ybufpos = 0;
                    skip++;
                }
                if (skip >= 280) {
                    skip = 0;
                    break;
                }
                //----Image Block----//
                VFDNewsPanel.vfd.write(0x1F);
                VFDNewsPanel.vfd.write(0x28);
                VFDNewsPanel.vfd.write(0x66);
                VFDNewsPanel.vfd.write(0x11);
                VFDNewsPanel.vfd.write(0x01);//xL
                VFDNewsPanel.vfd.write(0x00);//xH
                VFDNewsPanel.vfd.write(0x02);//yL
                VFDNewsPanel.vfd.write(0x00);//yH
                VFDNewsPanel.vfd.write(0x01);//g
                String s = yline.substring(0, 8);
                VFDNewsPanel.vfd.write(Integer.parseInt(s, 2));
                s = yline.substring(8, 17);
                VFDNewsPanel.vfd.write(Integer.parseInt(s, 2));
                //----Image Block----//
                VFDNewsPanel.vfd.scroll(2, 1, 0);//1pxスクロール
                int nowx = 280;
                nowx++;
                if (nowx >= 512) {
                    nowx = 0;
                }
                while (now+wait > System.currentTimeMillis());{

                }
                VFDNewsPanel.vfd.cursor_set(nowx, 0);//1pxカーソルずらし
            }
            VFDNewsPanel.vfd.brink(0,255,255,0);
        }catch (Exception e){
        }
        VFDNewsPanel.writing=null;
    }
}
