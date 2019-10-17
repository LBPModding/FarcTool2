package com.philosophofee.farctool2.data;

import com.philosophofee.farctool2.utilities.MiscUtils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entry {
    public String path;
    public String sha1;
    public String size;
    public String guid;
    public File fileToAdd;
    public Boolean addToFARC;

    public Entry(String path, String guid, File fileToAdd, Boolean addToFARC) {
        this.addToFARC = addToFARC;
        this.path = path;
        this.fileToAdd = fileToAdd;

        try {
            this.sha1 = MiscUtils.byteArrayToHexString(MiscUtils.getSHA1(fileToAdd));
        } catch (Exception ex) {
            Logger.getLogger(Entry.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.size = Integer.toHexString((int) fileToAdd.length());
        this.guid = guid;
    }

    public Entry(String path, String sha1, String size, String guid) {
        this.path = path;
        this.sha1 = sha1;
        this.size = size;
        this.guid = guid;
        this.addToFARC = false;
    }
}
