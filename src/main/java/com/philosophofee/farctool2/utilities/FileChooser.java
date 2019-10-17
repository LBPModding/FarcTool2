package com.philosophofee.farctool2.utilities;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileChooser {
    
    public JFileChooser fileDialogue;
    FileFilter filter;
    Component frame;
    
    public FileChooser(Component frame) {
        this.fileDialogue = new JFileChooser();
        this.fileDialogue.setCurrentDirectory(new java.io.File(System.getProperty("user.home") + "/Desktop"));
        this.frame = frame;
    }
    
    public File openFile(String name, String ext, String desc, boolean saveFile) {
        if (setupFilter(name, ext, desc, false, false))
        {
            if (((saveFile) ? fileDialogue.showSaveDialog(frame) : fileDialogue.showOpenDialog(frame)) == JFileChooser.APPROVE_OPTION) 
                return fileDialogue.getSelectedFile();
        }
        return null;
    }
    
    public File[] openFiles(String ext, String desc) {
        if (setupFilter("", ext, desc, true, false))
        {
            if (fileDialogue.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
                return fileDialogue.getSelectedFiles();
        }
        return null;
    }
    
    public String openDirectory() {
        if (setupFilter("", "", "", false, true))
        {
            if (fileDialogue.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                return fileDialogue.getSelectedFile().getAbsolutePath().toString() + "\\";
        }
        return null;
    }
    
    public boolean setupFilter(String name, String ext, String desc, boolean mult, boolean dirs) {
        if (dirs) fileDialogue.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        else fileDialogue.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileDialogue.setMultiSelectionEnabled(mult);
        fileDialogue.removeChoosableFileFilter(filter);
        if (name != "") fileDialogue.setSelectedFile(new java.io.File(name));
        if (ext != "" && desc != "")
        {
            filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                else if (f.getName().toLowerCase().endsWith(ext.toLowerCase())) return true;
                else return false;
            }
            @Override
            public String getDescription() { return desc; }
        };
        fileDialogue.setFileFilter(filter);   
        }
        return true;
    }
    
}
