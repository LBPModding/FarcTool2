package com.philosophofee.farctool2.utilities;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class FileChooser {

    private JFileChooser fileDialogue;
    private FileFilter filter;
    private Component frame;

    public FileChooser(Component frame) {
        this.fileDialogue = new JFileChooser();
        this.fileDialogue.setCurrentDirectory(new java.io.File(System.getProperty("user.home") + "/Desktop"));
        this.frame = frame;
    }

    public File openFile(String name, String ext, String desc, boolean saveFile) {
        if (setupFilter(name, ext, desc, false, false)) {
            if (((saveFile) ? fileDialogue.showSaveDialog(frame) : fileDialogue.showOpenDialog(frame)) == JFileChooser.APPROVE_OPTION)
                return fileDialogue.getSelectedFile();
        }
        return null;
    }

    public File[] openFiles(String ext, String desc) {
        if (setupFilter("", ext, desc, true, false)) {
            if (fileDialogue.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                return fileDialogue.getSelectedFiles();
        }
        return null;
    }

    public String openDirectory() {
        if (setupFilter("", "", "", false, true)) {
            if (fileDialogue.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                return fileDialogue.getSelectedFile().getAbsolutePath() + "\\";
        }
        return null;
    }

    private boolean setupFilter(String name, String ext, String desc, boolean mult, boolean dirs) {
        if (dirs) {
            fileDialogue.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            fileDialogue.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        fileDialogue.setMultiSelectionEnabled(mult);
        fileDialogue.removeChoosableFileFilter(filter);
        if (!name.equals("")) {
            fileDialogue.setSelectedFile(new File(name));
        }
        if (ext.equals("") || desc.equals("")) {
            return true;
        }
        filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().toLowerCase().endsWith(ext.toLowerCase());
            }

            @Override
            public String getDescription() {
                return desc;
            }
        };
        fileDialogue.setFileFilter(filter);
        return true;
    }

}
