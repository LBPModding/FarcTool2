package com.philosophofee.farctool2.streams;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class TextAreaOutputStream extends OutputStream {
    private JTextArea textControl;

    public TextAreaOutputStream(JTextArea control) {
        textControl = control;
    }

    @Override
    public void write(int b) throws IOException {
        textControl.append(String.valueOf((char) b));
    }

}