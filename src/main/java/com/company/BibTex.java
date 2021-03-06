package com.company;

import com.company.model.Entry;
import com.company.model.EntryType;
import com.company.model.FieldType;
import com.company.parserUtil.Parser;
import com.company.parserUtil.StringVariableUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main singleton class that contain a map of entries that is being created
 * from the file that user provides. Makes available to filter map
 * of entries by categories (Entry's types) and authors
 */
public class BibTex {

    private static BibTex instance;
    private StringBuilder fileInString;
    private Parser parser;
    private Map<String, Entry> entries;

    private BibTex() {
    }

    /**
     * Gets instance of BibTex class
     * @return Instance of BibTex class
     */
    public static BibTex getInstance() {
        return instance;
    }

    /**
     * Static factory for creating bibTex class from given file
     * @param filePath path of the file with .bib extension
     */
    public static BibTex getInstance(String filePath) {
        if(instance == null) {
            instance = new BibTex();
            try {
                instance.setFileInString(instance.readFile(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringVariableUtil.createMapOfStrings(instance.getFileInString());
            instance.parser = new Parser(instance.getFileInString());
            instance.entries = instance.getParser().parse();
        }
        return instance;
    }

    /**
     * Reads from given file to string
     * @param filePath path of the file with .bib extension
     * @return provided file in string
     * @throws IOException Throws exception when reading from file is failed
     */
    public StringBuilder readFile(String filePath) throws IOException {
        String file;
        try {
            file = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new IOException("Error while reading from file");
        }
        return new StringBuilder(file);
    }

    /**
     * Prints every entry from map
     */
    public void display() {
        entries.values().forEach(entry -> System.out.println(entry.toString()));
    }

    /**
     * Filters map of entries by categories
     * @param categories List of categories
     * @return map of filtered entries
     */
    public Map<String, Entry> filterByCategories(List<EntryType> categories) {
        return entries.entrySet()
                .stream()
                .filter(mapEntry -> categories.contains(mapEntry.getValue().getType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Filters map of entries by authors
     * @param authors List of authors
     * @return map of filtered entries
     */
    public Map<String, Entry> filterByAuthors(List<String> authors) {
        return entries.entrySet()
                .stream()
                .filter(mapEntry -> containsInAuthors(authors, mapEntry.getValue().getAllFields().get(FieldType.AUTHOR)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Check whether particular author is in the filtering list
     * @param authors List of authors
     * @param authorsInString author for checking
     * @return true if author matches, false if not
     */
    private boolean containsInAuthors(List<String> authors, String authorsInString) {
        if (authorsInString != null) {
            String[] splitValues = authorsInString.split("and");
            for (String s : splitValues) {
                for (String author : authors) {
                    if(s.toUpperCase().contains(author.toUpperCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Filters map of entries by categories and authors
     * @param categories List of categories
     * @param authors List of authors
     * @return map of filtered entries
     */
    public Map<String, Entry> filterByAuthorsAndCategories(List<EntryType> categories, List<String> authors) {
        return entries.entrySet()
                .stream()
                .filter(mapEntry ->
                        categories.contains(mapEntry.getValue().getType()) &&
                            containsInAuthors(authors, mapEntry.getValue().getAllFields().get(FieldType.AUTHOR))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void setFileInString(StringBuilder fileInString) {
        this.fileInString = fileInString;
    }

    public StringBuilder getFileInString() {
        return fileInString;
    }

    public Parser getParser() {
        return parser;
    }

    public Map<String, Entry> getEntriesMap() {
        return entries;
    }
}