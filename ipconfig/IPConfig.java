package ipconfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class IPConfig implements Serializable{
	
/** The Constant CONFIG. */
private final static String CONFIG = "src/ipconfig/ipconfig.properties";


/**
 * Gets the property.
 *
 * @param key the key
 * @return the property
 * @throws IOException Signals that an I/O exception has occurred.
 */
public static String getProperty(String key) throws IOException {
	String property = null;
	
	FileInputStream file = new FileInputStream(CONFIG);
	Properties prop = new Properties();
	
	prop.load(file);
	
	property = prop.getProperty(key);
	
	return property;
}
}
