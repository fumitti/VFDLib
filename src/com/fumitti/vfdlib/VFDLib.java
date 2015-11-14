package com.fumitti.vfdlib;

import gnu.io.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class VFDLib {
    private SerialPort port;
    private BufferedReader reader;
    private final OutputStream write;
    private int buf = 0;
    private int nowx = 0;

    public VFDLib(String portname, int bitrate) throws IOException {
        // Serial port initialize
        CommPortIdentifier portId = null;
        try {
            portId = CommPortIdentifier.getPortIdentifier(portname);
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
        try {
            port = (SerialPort) portId.open("serial", 2000);
        } catch (PortInUseException e) {
            e.printStackTrace();
        }

        try {
            port.setSerialPortParams(
                    bitrate,                   // 通信速度[bps]
                    SerialPort.DATABITS_8,   // データビット数
                    SerialPort.STOPBITS_1,   // ストップビット
                    SerialPort.PARITY_NONE   // パリティ
            );
            port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT | SerialPort.FLOWCONTROL_RTSCTS_IN);

        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }

        reader = new BufferedReader(
                new InputStreamReader(port.getInputStream()));

        try {
            //port.addEventListener(new SerialEventListener());
            port.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        write = port.getOutputStream();
    }

    public void init() throws IOException {
        write(0x1B);
        write(0x40);
    }

    public void clear() throws IOException {
        write(0x0C);
    }

    public void cursor_home() throws IOException {
        write(0x0B);
    }

    public void cursor_left() throws IOException {
        write(0x08);
    }

    public void cursor_right() throws IOException {
        write(0x09);
    }

    public void cursor_return() throws IOException {
        write(0x0A);
    }

    public void cursor_carriagereturn() throws IOException {
        write(0x0D);
    }

    public void cursor_set(int x, int y) throws IOException {
        write(0x1f);
        write(0x24);
        if (x < 256)
            write(x);
        else
            write(x - 256);
        write(x / 256);
        write(y);//yL
        write(0x00);//yH
    }

    public void setModeOverWrite() throws IOException {
        write(0x1F);
        write(0x01);
    }

    public void setModeHorScroll() throws IOException {
        write(0x1F);
        write(0x02);
    }

    public void setModeVerScroll() throws IOException {
        write(0x1F);
        write(0x03);
    }

    public void setVerScrollSpeed(int speed) throws IOException {
        write(0x1F);
        write(0x73);
        write(speed);
    }

    public void setReverseMode(boolean set) throws IOException {
        write(0x1F);
        write(0x72);
        if (set)
            write(0x01);
        else
            write(0x00);
    }

    public void setWriteMixMode(int mode) throws IOException {
        write(0x1F);
        write(0x77);
        write(mode);
    }

    public void wait(int time) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x61);
        write(0x01);
        write(time);
    }

    public void scroll(int shiftbyte, int repeat, int speed) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x61);
        write(0x10);
        int a = shiftbyte / 256;//(255)
        int b = shiftbyte - (256 * a);
        write(b);
        write(a);
        a = repeat / 256;//(255)
        b = repeat - (256 * a);
        write(b);
        write(a);
        write(speed);
    }

    public void brink(int type, int time1, int time2, int repeat) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x61);
        write(0x11);
        write(type);
        write(time1);
        write(time2);
        write(repeat);
    }

    public void screenSaver(int type) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x61);
        write(0x40);
        write(type);
    }

    public void fontSize(int x, int y) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x67);
        write(0x40);
        write(x);
        write(y);
    }

    public void selectCurrentWindow(int window) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x77);
        write(0x01);
        write(window);
    }

    public void setWriteWindowMode(boolean fullscreem) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x77);
        write(0x10);
        if (fullscreem)
            write(0x01);
        else
            write(0x00);
    }


    public void defineUserWindow(int window, int x, int y, int xsize, int ysize) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x77);
        write(0x02);
        write(window);
        write(0x01);
        int a = x / 256;//(255)
        int b = x - (256 * a);
        write(b);
        write(a);
        write(y);
        write(0x00);
        a = xsize / 256;//(255)
        b = xsize - (256 * a);
        write(b);
        write(a);
        write(ysize);
        write(0x00);
    }

    public void deleteUserWindow(int window) throws IOException {
        write(0x1F);
        write(0x28);
        write(0x77);
        write(0x02);
        write(window);
        write(0x00);
    }

    public void setBrightness(int Brightness) throws IOException {
        write(0x1F);
        write(0x58);
        write(Brightness);
    }

    public void fonttype(int type) throws IOException {
        write(0x1f);
        write(0x28);
        write(0x67);
        write(0x03);
        write(type);
    }

    public void bitmapdraw(BitmapChar b) throws IOException {
        int xsize = b.getBitmapData().get(0).size() * 4;
        int ysize = b.getBitmapData().size();
        write(0x1F);
        write(0x28);
        write(0x66);
        write(0x11);
        if (xsize < 256) {
            write(xsize);//xL
            write(0x00);//xH
        } else if (xsize < 512) {
            write(xsize - 256);//xL
            write(0x01);//xH
        } else if (xsize == 512) {
            write(xsize - 512);//xL
            write(0x02);//xH
        }
        if (ysize > 8) {
            write(0x02);//yL
        } else {
            write(0x01);//yL
        }
        write(0x00);//yH
        write(0x01);//g
        List<String> intl = new ArrayList<>();
        for (List<Integer> list : b.getBitmapData()) {
            String line = "";
            for (Integer data : list) {
                line += String.format("%04d", Integer.parseInt(Integer.toBinaryString(data)));
            }
            intl.add(line);
        }

        for (int i = 0; i < xsize; i++) {
            String yline = "";
            for (String line : intl) {
                char c = line.charAt(i);
                if (c == '0') {
                    yline += "0";
                } else {
                    yline += "1";
                }
            }
            int v = ysize / 8;
            int n = ysize % 8;
            if (n != 0) {
                v++;
            }
            for (int j = 0; j < v; j++) {
                int l = (j + 1) * 8;
                if (j == v) {
                    if (n == 0)
                        continue;
                    l = j * 8 + n;
                }
                int st = j * 8;
                if (st != 0) {
                    st--;
                }
                String s = yline.substring(st, l);
                write(Integer.parseInt(s, 2));
            }
        }
    }

    public void cursor_view(boolean view) throws IOException {
        write(0x1F);
        write(0x43);
        if (view)
            write(0x01);
        else
            write(0x00);
    }

    public void set_use_ext_char(boolean set) throws IOException {
        write(0x1B);
        write(0x23);
        if (set)
            write(0x01);
        else
            write(0x00);
    }

    public void deleteExtChar(char c) throws IOException {
        write(0x1B);
        write(0x3F);
        write(0x01);
        write((int) c);
    }

    public void setInternationalCharset(int set) throws IOException {
        write(0x1B);
        write(0x52);
        write(set);
    }

    public void setCodePages(int set) throws IOException {
        write(0x1B);
        write(0x74);
        write(set);
    }

    public void write(int i) throws IOException {
        synchronized (write) {
            write.write(i);
        }
    }

    public void print(String s) throws IOException {
        for (byte b : s.getBytes()) {
            write(b);
        }
    }
}
