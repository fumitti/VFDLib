package com.fumitti.vfdlib.Writer;

import com.fumitti.vfdlib.VFDNewsPanel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by fumitti on 2015/06/23.
 */
public class OneWindowWriter implements WriterInterface {

    private final List<String> ybuf;
    private final boolean blink;

    public OneWindowWriter(List<String> ybuf) {
        this.ybuf = ybuf;
        this.blink=false;
    }

    public OneWindowWriter(List<String> ybuf,boolean blink) {
        this.ybuf = ybuf;
        this.blink = blink;
    }

    @Override
    public void run() {
        try {
            int ybufpos = 0;
            int skip = 0;
            int xsize = ybuf.size();
            VFDNewsPanel.vfd.cursor_home();
            VFDNewsPanel.vfd.write(0x1F);
            VFDNewsPanel.vfd.write(0x28);
            VFDNewsPanel.vfd.write(0x66);
            VFDNewsPanel.vfd.write(0x11);
            if (xsize < 256) {
                VFDNewsPanel.vfd.write(xsize);//xL
                VFDNewsPanel.vfd.write(0x00);//xH
            } else if (xsize < 512) {
                VFDNewsPanel.vfd.write(xsize - 256);//xL
                VFDNewsPanel.vfd.write(0x01);//xH
            } else if (xsize == 512) {
                VFDNewsPanel.vfd.write(xsize - 512);//xL
                VFDNewsPanel.vfd.write(0x02);//xH
            }
            VFDNewsPanel.vfd.write(0x02);//yL
            VFDNewsPanel.vfd.write(0x00);//yH
            VFDNewsPanel.vfd.write(0x01);//g
            while (true) {
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
                String s = yline.substring(0, 8);
                VFDNewsPanel.vfd.write(Integer.parseInt(s, 2));
                s = yline.substring(8, 17);
                VFDNewsPanel.vfd.write(Integer.parseInt(s, 2));
                //----Image Block----//
            }
        }catch (Exception e){
        }
        VFDNewsPanel.writing=null;
    }
}
