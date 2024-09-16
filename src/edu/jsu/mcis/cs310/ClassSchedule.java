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
            HashMap<String, Integer> headerIndexMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndexMap.put(headers[i], i);
            }

            // Initialize JSON objects
            JsonObject typeToScheduleMap = new JsonObject();
            JsonObject idToScheduleMap = new JsonObject();
            JsonObject courseNameToDetailsMap = new JsonObject();
            JsonArray sectionArray = new JsonArray();

            while (csvIterator.hasNext()) {
                String[] rowData = csvIterator.next();

                // Schedule Type
                String type = rowData[headerIndexMap.get(TYPE_COL_HEADER)];
                String schedule = rowData[headerIndexMap.get(SCHEDULE_COL_HEADER)];
                if (typeToScheduleMap.get(type) == null) {
                    typeToScheduleMap.put(type, schedule);
                }

                // Subject
                String subjectId = rowData[headerIndexMap.get(NUM_COL_HEADER)].replaceAll("\\d", "").replaceAll("\\s", "");
                if (idToScheduleMap.get(subjectId) == null) {
                    String subjectHeader = rowData[headerIndexMap.get(SUBJECT_COL_HEADER)];
                    idToScheduleMap.put(subjectId, subjectHeader);
                }

                // Course
                String courseNum = rowData[headerIndexMap.get(NUM_COL_HEADER)];
                String courseNumNoLetters = courseNum.replaceAll("[A-Z]", "").replaceAll("\\s", "");
                if (courseNameToDetailsMap.get(courseNum) == null) {
                    String description = rowData[headerIndexMap.get(DESCRIPTION_COL_HEADER)];
                    int credits = Integer.parseInt(rowData[headerIndexMap.get(CREDITS_COL_HEADER)]);
                    JsonObject course = new JsonObject();
                    course.put(SUBJECTID_COL_HEADER, subjectId);
                    course.put(NUM_COL_HEADER, courseNumNoLetters);
                    course.put(DESCRIPTION_COL_HEADER, description);
                    course.put(CREDITS_COL_HEADER, credits);
                    courseNameToDetailsMap.put(courseNum, course);
                }

                // Extract Section-specific data
                int crn = Integer.parseInt(rowData[headerIndexMap.get(CRN_COL_HEADER)]);
                String sectionHeader = rowData[headerIndexMap.get(SECTION_COL_HEADER)];
                String start = rowData[headerIndexMap.get(START_COL_HEADER)];
                String end = rowData[headerIndexMap.get(END_COL_HEADER)];
                String days = rowData[headerIndexMap.get(DAYS_COL_HEADER)];
                String where = rowData[headerIndexMap.get(WHERE_COL_HEADER)];
                String allInstructors = rowData[headerIndexMap.get(INSTRUCTOR_COL_HEADER)];
                // Split Instructor list into individuals
                List<String> instructors = Arrays.asList(allInstructors.split(", "));
                JsonArray instructorArray = new JsonArray();
                for (String instructor : instructors) {
                    instructorArray.add(instructor);
                }

                // JSON object to store sectionDetails data
                JsonObject sectionDetails = new JsonObject();
                sectionDetails.put(CRN_COL_HEADER, crn);
                sectionDetails.put(SECTION_COL_HEADER, sectionHeader);
                sectionDetails.put(START_COL_HEADER, start);
                sectionDetails.put(END_COL_HEADER, end);
                sectionDetails.put(DAYS_COL_HEADER, days);
                sectionDetails.put(WHERE_COL_HEADER, where);
                sectionDetails.put(INSTRUCTOR_COL_HEADER, instructorArray);
                sectionDetails.put(NUM_COL_HEADER, courseNumNoLetters);
                sectionDetails.put(TYPE_COL_HEADER, type); 
                sectionDetails.put(SUBJECTID_COL_HEADER, subjectId); 

                // Add sectionDetails object to sectionDetails array
                sectionArray.add(sectionDetails);
            }


            // Final JSON object to store all course data
            JsonObject classListMap = new JsonObject();
            classListMap.put("scheduletype", typeToScheduleMap);
            classListMap.put("subject", idToScheduleMap);
            classListMap.put("course", courseNameToDetailsMap);
            classListMap.put("section", sectionArray);

            // Serialize JSON object to string
            return Jsoner.serialize(classListMap);
        }
        return "{}"; // Return empty JSON if no data is present
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        // StringWriter 
        StringWriter writer = new StringWriter();

        // CSV writer
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");

        // CSV headers
        String[] header = {
            CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER, DESCRIPTION_COL_HEADER, 
            SECTION_COL_HEADER, TYPE_COL_HEADER, CREDITS_COL_HEADER, START_COL_HEADER, 
            END_COL_HEADER, DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER, 
            INSTRUCTOR_COL_HEADER
        };
        csvWriter.writeNext(header);

        // Extract JSON objects
        JsonObject scheduleTypeMap = (JsonObject)json.get("scheduletype");
        JsonObject subjectMap = (JsonObject)json.get("subject");
        JsonObject courseMap = (JsonObject)json.get("course");
        JsonArray sectionArray = (JsonArray)json.get("section");

        // Iterate over each section
        for (int i = 0; i < sectionArray.size(); i++) {
            // Extract section details
            JsonObject sectionDetails = sectionArray.getMap(i);

            // Extract section data
            String crn = String.valueOf(sectionDetails.get(CRN_COL_HEADER));
            String subjectId = (String)sectionDetails.get(SUBJECTID_COL_HEADER);
            String num = subjectId + " " + sectionDetails.get(NUM_COL_HEADER);
            String section = (String)sectionDetails.get(SECTION_COL_HEADER);
            String type = (String)sectionDetails.get(TYPE_COL_HEADER);
            String start = (String)sectionDetails.get(START_COL_HEADER);
            String end = (String)sectionDetails.get(END_COL_HEADER);
            String days = (String)sectionDetails.get(DAYS_COL_HEADER);
            String where = (String)sectionDetails.get(WHERE_COL_HEADER);

            // Extract instructors
            JsonArray instructorArray = (JsonArray)sectionDetails.get(INSTRUCTOR_COL_HEADER);
            StringBuilder instructorBuilder = new StringBuilder();
            for (int j = 0; j < instructorArray.size(); j++) {
                instructorBuilder.append(instructorArray.getString(j));
                if (j < instructorArray.size() - 1) {
                    instructorBuilder.append(", ");
                }
            }
            String instructor = instructorBuilder.toString();


            // Retrieve schedule type from the scheduleTypeMap
            String schedule = (String)scheduleTypeMap.get(type);

            // Extract course details using the course number
            JsonObject courseDetails = (JsonObject)courseMap.get(num);
            String description = (String)courseDetails.get(DESCRIPTION_COL_HEADER);
            String credits = String.valueOf(courseDetails.get(CREDITS_COL_HEADER));

            // Retrieve subject name from the subjectMap
            String subjectName = (String)subjectMap.get(subjectId);

            // Combine all the fields
            String[] record = {
                crn, subjectName, num, description, section, type, credits, 
                start, end, days, where, schedule, instructor
            };

            // Write the record to the CSV file
            csvWriter.writeNext(record);
        }

        // Convert CSV data to a string
        return writer.toString();
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