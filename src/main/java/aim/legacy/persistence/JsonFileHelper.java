package aim.legacy.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * JsonFileHelper - utility for reading/writing JSON files.
 * Legacy pattern: Static helper methods.
 */
public class JsonFileHelper {
    
    private static final ObjectMapper mapper = createMapper();
    
    private static ObjectMapper createMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        return om;
    }
    
    /**
     * Read list of objects from JSON file.
     */
    public static <T> List<T> readList(File file, Class<T> clazz) throws IOException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }
        return mapper.readValue(file, 
            mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
    
    /**
     * Write list of objects to JSON file.
     */
    public static <T> void writeList(File file, List<T> list) throws IOException {
        // Create parent directories if needed
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        mapper.writeValue(file, list);
    }
    
    /**
     * Get ObjectMapper instance for custom serialization.
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}
