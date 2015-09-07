package com.hello2morrow.sonargraph.jenkinsplugin.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hudson.FilePath;

public class TextFileReader
{
    public String readLargeTextFile(FilePath largeTextFilePath) throws IOException
    {
        BufferedReader bfReader = null;
        StringBuilder completeTextFile = new StringBuilder();
        bfReader = new BufferedReader(new InputStreamReader(largeTextFilePath.read()));
        String currentLine;
        while ((currentLine = bfReader.readLine()) != null)
        {
            completeTextFile.append(currentLine);
        }
        bfReader.close();
        return completeTextFile.toString();
    }
}