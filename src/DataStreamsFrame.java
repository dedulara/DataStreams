import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static java.awt.Font.*;

public class DataStreamsFrame extends JFrame
{
    JPanel wholePanel, buttonPanel, taPanel, searchPanel;

    JButton loadFileButton, searchFileButton, quitButton;

    JTextArea wholeTextTA, searchTA;
    JScrollPane scrollPaneOne, scrollPaneTwo;

    JTextField searchTF;

    JFileChooser chooser = new JFileChooser();
    File selectedFile;
    File workingDirectory = new File(System.getProperty("user.dir"));

    public static Set<String> noiseWords = new TreeSet<>();
    public static Set<String> keySet = new TreeSet<>();
    Map<String, List<Integer>> lineMap = new TreeMap<>();
    Map<Integer, String> wholeLine = new TreeMap<>();

    int lineNumber = 0;

    public DataStreamsFrame()
    {
        wholePanel = new JPanel();
        wholePanel.setLayout(new BorderLayout());
        createButtonPanel();
        wholePanel.add(buttonPanel, BorderLayout.NORTH);
        createTAPanel();
        wholePanel.add(taPanel, BorderLayout.CENTER);
        createSearchPanel();
        wholePanel.add(searchPanel, BorderLayout.SOUTH);

        add(wholePanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000,700);
    }

    public void createButtonPanel()
    {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,2));
        loadFileButton = new JButton("Pick File");
        quitButton = new JButton("Quit");

        loadFileButton.addActionListener((ActionEvent ae) ->
        {
            wholeTextTA.setText("");
            chooser.setCurrentDirectory(workingDirectory);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                lineNumber = 0;
                try(Stream<String> lines = Files.lines(file))
                {
                    lines.forEach(l ->
                    {
                        wholeTextTA.append(l + "\n");
                        lineNumber++;
                        wholeLine.put(lineNumber, l);
                        String[] words = l.split("[^\\w']+");
                        String w;
                        for (String x : words)
                        {
                            w = x.toLowerCase().trim();
                            w = w.replaceAll("_", " ").trim();
                            if(!isNoiseWord(w))
                            {
                                if(lineMap.containsKey(w)) {lineMap.get(w).add(lineNumber);}
                                else
                                {
                                    List<Integer> lnList = new LinkedList<>();
                                    lnList.add(lineNumber);
                                    lineMap.put(w, lnList);
                                }
                            }
                        }
                    });
                }
                catch(IOException e) {e.printStackTrace();}
                //keySet.clear();
                keySet = lineMap.keySet();
                for(String k:keySet) {Stream <Integer> words = lineMap.get(k).stream();}
            }
            else {System.out.println("Must choose a file to process!");}
            searchFileButton.setEnabled(true);
            searchTF.setEditable(true);
        });

        quitButton.addActionListener((ActionEvent ae) -> System.exit(0));

        buttonPanel.add(loadFileButton);
        buttonPanel.add(quitButton);
    }

    public void createTAPanel()
    {
        taPanel = new JPanel();
        taPanel.setLayout(new GridLayout(1,2));
        wholeTextTA = new JTextArea();
        wholeTextTA.setEditable(false);
        scrollPaneOne = new JScrollPane(wholeTextTA);
        searchTA = new JTextArea();
        searchTA.setEditable(false);
        scrollPaneTwo = new JScrollPane(searchTA);

        taPanel.add(scrollPaneOne);
        taPanel.add(scrollPaneTwo);
    }

    public void createSearchPanel()
    {
        searchPanel = new JPanel();
        searchPanel.setLayout(new GridLayout(1,2));
        searchTF = new JTextField(15);
        searchPanel.add(searchTF);
        searchFileButton = new JButton("Search File");
        searchFileButton.addActionListener((ActionEvent ae) ->
        {
            searchTA.setText("");
            String searchWord = searchTF.getText();
            List<Integer> lineList = new LinkedList();
            lineList = lineMap.get(searchWord);
            if (lineMap.get(searchWord) == null){searchTA.setText("Word not found in file.");}
            else
            {
                int listLength = lineList.size();
                for(int i = 0; i < (listLength); i++)
                {
                    String lineString = wholeLine.get(lineList.get(i));
                    searchTA.append("Line " + lineList.get(i) + " - " + lineString + "\n");
                }
            }
        });
        searchPanel.add(searchFileButton);
        searchFileButton.setEnabled(false);
        searchTF.setEditable(false);
    }

    public boolean isNoiseWord(String word)
    {
        if(noiseWords.isEmpty()) {loadNoiseWords();}
        if(noiseWords.contains(word)) {return true;}
        else {return false;}
    }

    public void loadNoiseWords()
    {
        try(Stream<String> lines = Files.lines(Paths.get("\\src\\EnglishStopWords.txt")))
        {
            lines.forEach(l ->
            {
                String[] words = l.split("\\R");
                String w;
                for (String x : words)
                {
                    w = x.toLowerCase().trim();
                    noiseWords.add(w);
                }
            });
        }
        catch(IOException e) {e.printStackTrace();}
    }
}