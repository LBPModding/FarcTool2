package com.philosophofee.farctool2.utilities;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Aidan
 */
public class Entry 
{
    public String Path;
    public String SHA1;
    public String Size;
    public String GUID;
    public File FileToAdd;
    public Boolean AddToFARC;
    
    public Entry(String Path, String GUID, File FileToAdd, Boolean AddToFARC)
    {
        this.AddToFARC = AddToFARC;
        this.Path = Path;
        this.FileToAdd = FileToAdd;
        
        try {
            this.SHA1 = MiscUtils.byteArrayToHexString(MiscUtils.getSHA1(FileToAdd));
        } catch (Exception ex) {
            Logger.getLogger(Entry.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.Size = Integer.toHexString((int) FileToAdd.length());
        this.GUID = GUID;
    }
    
    public Entry (String Path, String SHA1, String Size, String GUID)
    {
        this.Path = Path;
        this.SHA1 = SHA1;
        this.Size = Size;
        this.GUID = GUID;
        this.AddToFARC = false;
    }
}
