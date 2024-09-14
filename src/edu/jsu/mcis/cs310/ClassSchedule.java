package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        // CSV Iterator
        Iterator<String[]> csvIterator = csv.iterator();

        if (csvIterator.hasNext()) {
            // Headers
            String[] headers = csvIterator.next();
            // Map to store Headers
            HashMap<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i], i);
            }

            // Initialize JSON objects
            JsonObject scheduleTypeMap = new JsonObject();
            JsonObject subjectMap = new JsonObject();
            JsonObject courseMap = new JsonObject();
            JsonArray sectionArray = new JsonArray();

            while (csvIterator.hasNext()) {
                String[] csvRecord = csvIterator.next();

                // Schedule Type
                String type = csvRecord[headerIndex.get(TYPE_COL_HEADER)];
                String schedule = csvRecord[headerIndex.get(SCHEDULE_COL_HEADER)];
                if (scheduleTypeMap.get(type) == null) {
                    scheduleTypeMap.put(type, schedule);
                }

                // Subject
                String subjectId = csvRecord[headerIndex.get(NUM_COL_HEADER)].replaceAll("\\d", "").replaceAll("\\s", "");
                if (subjectMap.get(subjectId) == null) {
                    String subjectHeader = csvRecord[headerIndex.get(SUBJECT_COL_HEADER)];
                    subjectMap.put(subjectId, subjectHeader);
                }

                // Course
                String num = csvRecord[headerIndex.get(NUM_COL_HEADER)];
                String numNoLetters = num.replaceAll("[A-Z]", "").replaceAll("\\s", "");
                if (courseMap.get(num) == null) {
                    String description = csvRecord[headerIndex.get(DESCRIPTION_COL_HEADER)];
                    int credits = Integer.parseInt(csvRecord[headerIndex.get(CREDITS_COL_HEADER)]);
                    JsonObject course = new JsonObject();
                    course.put(SUBJECTID_COL_HEADER, subjectId);
                    course.put(NUM_COL_HEADER, numNoLetters);
                    course.put(DESCRIPTION_COL_HEADER, description);
                    course.put(CREDITS_COL_HEADER, credits);
                    courseMap.put(num, course);
                }

                // Extract Section-specific data
                int crn = Integer.parseInt(csvRecord[headerIndex.get(CRN_COL_HEADER)]);
                String sectionHeader = csvRecord[headerIndex.get(SECTION_COL_HEADER)];
                String start = csvRecord[headerIndex.get(START_COL_HEADER)];
                String end = csvRecord[headerIndex.get(END_COL_HEADER)];
                String days = csvRecord[headerIndex.get(DAYS_COL_HEADER)];
                String where = csvRecord[headerIndex.get(WHERE_COL_HEADER)];
                String allInstructors = csvRecord[headerIndex.get(INSTRUCTOR_COL_HEADER)];
                // Split Instructor list into individuals
                List<String> instructors = Arrays.asList(allInstructors.split(", "));
                JsonArray instructorArray = new JsonArray();
                for (String instructor : instructors) {
                    instructorArray.add(instructor);
                }

                // JSON object to store section data
                JsonObject section = new JsonObject();
                section.put(CRN_COL_HEADER, crn);
                section.put(SECTION_COL_HEADER, sectionHeader);
                section.put(START_COL_HEADER, start);
                section.put(END_COL_HEADER, end);
                section.put(DAYS_COL_HEADER, days);
                section.put(WHERE_COL_HEADER, where);
                section.put(INSTRUCTOR_COL_HEADER, instructorArray);
                section.put(NUM_COL_HEADER, numNoLetters);
                section.put(TYPE_COL_HEADER, type); 
                section.put(SUBJECTID_COL_HEADER, subjectId); 

                // Add section object to section array
                sectionArray.add(section);
            }


            // Final JSON object to store all course data
            JsonObject courseListMap = new JsonObject();
            courseListMap.put("scheduletype", scheduleTypeMap);
            courseListMap.put("subject", subjectMap);
            courseListMap.put("course", courseMap);
            courseListMap.put("section", sectionArray);

            // Serialize JSON object to string
            return Jsoner.serialize(courseListMap);
        }
        return "{}"; // Return empty JSON if no data is present
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        
        
        
        
        
        
        
        return ""; // remove this!
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}