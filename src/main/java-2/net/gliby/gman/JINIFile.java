/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.gman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

public class JINIFile
extends ArrayList {
    private final File userFileName;

    public JINIFile(File file) throws IOException {
        this.clear();
        this.userFileName = file;
        if (this.userFileName.exists()) {
            String s;
            BufferedReader inbuf = new BufferedReader(new FileReader(this.userFileName));
            while ((s = inbuf.readLine()) != null) {
                if (s.startsWith(";")) continue;
                this.add(s);
            }
            inbuf.close();
        } else {
            file.createNewFile();
            BufferedReader inbuf = new BufferedReader(new FileReader(this.userFileName));
            inbuf.close();
        }
    }

    private void addToList(String Section, String key, String value) {
        if (this.SectionExist(Section)) {
            if (this.ValueExist(Section, key)) {
                int pos = this.ValuePosition(Section, key);
                this.remove(pos);
                this.add(pos, value);
            } else {
                this.add(this.SectionPosition(Section) + 1, value);
            }
        } else {
            this.add("[" + Section + "]");
            this.add(value);
        }
    }

    public void DeleteKey(String Section, String key) {
        if (this.ValuePosition(Section, key) > 0) {
            this.remove(this.ValuePosition(Section, key));
        }
    }

    public void EraseSection(String Section) {
        int start = this.SectionPosition(Section) + 1;
        if (this.SectionPosition(Section) > -1) {
            String s;
            for (int i = start; !(i >= this.size() || (s = this.get(i).toString()).startsWith("[") && s.endsWith("]")); ++i) {
                this.remove(i);
                --i;
            }
            this.remove(this.SectionPosition(Section));
        }
    }

    public boolean ReadBool(String Section, String key, boolean defaultValue) throws JINIReadException {
        String s = this.get(this.ValuePosition(Section, key)).toString().substring(key.length() + 1, this.get(this.ValuePosition(Section, key)).toString().length());
        boolean value = defaultValue;
        if (this.ValuePosition(Section, key) > 0) {
            value = Boolean.parseBoolean(s);
            return value;
        }
        throw new JINIReadException("ReadBool operation failed: " + s);
    }

    public Float ReadFloat(String Section, String key, Float defaultValue) throws JINIReadException {
        Float value = new Float(0.0f);
        value = defaultValue;
        if (this.ValuePosition(Section, key) > 0) {
            int strLen = key.length() + 1;
            value = Float.valueOf(this.get(this.ValuePosition(Section, key)).toString().substring(strLen, this.get(this.ValuePosition(Section, key)).toString().length()));
            return value;
        }
        throw new JINIReadException("ReadFloat operation failed.");
    }

    public int ReadInteger(String Section, String key, int defaultValue) throws JINIReadException {
        int value = defaultValue;
        if (this.ValuePosition(Section, key) > 0) {
            int strLen = key.length() + 1;
            value = Integer.parseInt(this.get(this.ValuePosition(Section, key)).toString().substring(strLen, this.get(this.ValuePosition(Section, key)).toString().length()));
            return value;
        }
        throw new JINIReadException("ReadInteger operation failed.");
    }

    public ArrayList ReadSection(String Section) {
        ArrayList<String> myList = new ArrayList<String>();
        int start = this.SectionPosition(Section) + 1;
        if (this.SectionPosition(Section) > -1) {
            String s;
            for (int i = start; !(i >= this.size() || (s = this.get(i).toString()).startsWith("[") && s.endsWith("]")); ++i) {
                myList.add(s.substring(0, s.indexOf("=")));
            }
        }
        return myList;
    }

    public ArrayList ReadSections() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < this.size(); ++i) {
            String s = this.get(i).toString();
            if (!s.startsWith("[") || !s.endsWith("]")) continue;
            list.add(s.substring(1, s.length() - 1));
        }
        return list;
    }

    public ArrayList ReadSectionValues(String Section) {
        ArrayList<String> myList = new ArrayList<String>();
        int start = this.SectionPosition(Section) + 1;
        if (this.SectionPosition(Section) > -1) {
            String s;
            for (int i = start; !(i >= this.size() || (s = this.get(i).toString().substring(this.get(i).toString().indexOf("=") + 1, this.get(i).toString().length())).startsWith("[") && s.endsWith("]")); ++i) {
                myList.add(s);
            }
        }
        return myList;
    }

    public String ReadString(String Section, String key, String defaultValue) {
        String value = defaultValue;
        if (this.ValuePosition(Section, key) > 0) {
            int strLen = key.length() + 1;
            value = this.get(this.ValuePosition(Section, key)).toString().substring(strLen, this.get(this.ValuePosition(Section, key)).toString().length());
        } else {
            try {
                throw new Exception("Failed to parse");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public boolean SectionExist(String Section) {
        boolean val = false;
        for (int i = 0; i < this.size(); ++i) {
            String s = this.get(i).toString();
            if (!s.equals("[" + Section + "]")) continue;
            val = true;
            break;
        }
        return val;
    }

    private int SectionPosition(String Section) {
        int pos = -1;
        for (int i = 0; i < this.size(); ++i) {
            String s = this.get(i).toString();
            if (!s.equals("[" + Section + "]")) continue;
            pos = i;
            break;
        }
        return pos;
    }

    public boolean UpdateFile() {
        try {
            String s;
            BufferedWriter outbuf = new BufferedWriter(new FileWriter(this.userFileName, false));
            for (int i = 0; i < this.size() && (s = this.get(i).toString()) != null; ++i) {
                outbuf.write(s);
                outbuf.newLine();
            }
            outbuf.close();
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }

    public boolean ValueExist(String Section, String key) {
        int start = this.SectionPosition(Section);
        boolean val = false;
        key.length();
        for (int i = start + 1; i < this.size(); ++i) {
            String s = this.get(i).toString();
            if (s.startsWith(key + "=")) {
                val = true;
                break;
            }
            if (s.startsWith("[") && s.endsWith("]")) break;
        }
        return val;
    }

    private int ValuePosition(String Section, String key) {
        int start = this.SectionPosition(Section);
        int pos = -1;
        key.length();
        for (int i = start + 1; i < this.size(); ++i) {
            String s = this.get(i).toString();
            if (s.startsWith(key + "=")) {
                pos = i;
                break;
            }
            if (s.startsWith("[") && s.endsWith("]")) break;
        }
        return pos;
    }

    public void WriteBool(String Section, String key, boolean value) {
        String s = key + "=" + Boolean.toString(value);
        this.addToList(Section, key, s);
    }

    public void WriteComment(String Section, String comment) {
        if (this.SectionExist(Section)) {
            this.add(this.SectionPosition(Section) + 1, "; " + comment);
        }
    }

    public void WriteFloat(String Section, String key, float value) {
        String s = key + "=" + Float.toString(value);
        this.addToList(Section, key, s);
    }

    public void WriteInteger(String Section, String key, int value) {
        String s = key + "=" + Integer.toString(value);
        this.addToList(Section, key, s);
    }

    public void WriteString(String Section, String key, String value) {
        String s = key + "=" + value;
        this.addToList(Section, key, s);
    }

    public static class JINIReadException
    extends Exception {
        public JINIReadException(String string) {
            super(string);
        }
    }

}

