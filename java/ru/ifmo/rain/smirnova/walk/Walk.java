package ru.ifmo.rain.smirnova.walk;

import java.io.*;

public class Walk {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect input format.\nUsage:\nWalk <input_file> <output_file>");
        } else {
            //buffer
            try ( LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
                  Writer writer = new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8") ) {
                String curLine = null;
                byte[] buf = new byte[1024];
                try {
                    while ((curLine = reader.readLine()) != null) {
                        processFile(curLine, writer, buf);
                    }
                } catch(IOException e) {
                    System.out.println("Read/write error");//separite exeptions
                }
            } catch (FileNotFoundException e) {
                System.out.println("Input/Output file doesn't exist");
            } catch (UnsupportedEncodingException e) {
                System.out.println("UTF-8 encoding is unsupported");
            } catch (IOException e) {
                System.out.println("Close error");
            }
        }
    }

    public static void processFile(String filePath, Writer out, byte[] buf) throws IOException {
        int hashValue = 0x811c9dc5;
        try (InputStream in = new FileInputStream(filePath)) {
            int t;
            while ((t = in.read(buf)) >= 0) {
                hashValue = hash(hashValue, buf, t);
            }
        } catch(FileNotFoundException e) {
            hashValue = 0;
        } catch(IOException e) {
            System.out.println("Read error");
        } finally {
            out.write(String.format("%08x", hashValue) + " " + filePath + System.lineSeparator());
        }
    }

    private static int hash(int h, final byte[] buf, int t) {
        for (int i = 0; i < t; ++i) {
            h = (h * 0x01000193) ^ (buf[i] & 0xff);
        }
        return h;
    }

}
