import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

//Properties properties = loadProperties();

public class PropertySaver extends Properties{
    private static final String PROPERTIES_FILE = "config.properties";
    private static final int DEFAULT_CPS = 5;
    private static final int DEFAULT_ACBUTTON = 3;
    private static final boolean DEFAULT_ISMOUSE = true;

    public static Properties loadProperties() {
        Properties properties = new Properties();
        File file = new File(PROPERTIES_FILE);
        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(file)) {
                properties.load(input);
            } catch (IOException e) {
                // Failed to load properties, ignore
            }
        } else {
            properties.setProperty("millis", String.valueOf(DEFAULT_CPS));
            properties.setProperty("acButton", String.valueOf(DEFAULT_ACBUTTON));
            properties.setProperty("isMouse", String.valueOf(DEFAULT_ISMOUSE));
            saveProperties(properties);
        }
        return properties;
    }

    public static void saveProperties(Properties properties) {
        try (FileOutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            // Failed to save properties, ignore
        }
    }
}
