package com.kidscademy.dynamicmedia;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This interface implementations are reused and should hold not state, that is,
 * be stateless.
 * 
 * @author Iulian Rotaru
 */
public interface Transformer {
    Pattern pattern();

    void exec(File source, File target, Matcher matcher) throws IOException;
}
