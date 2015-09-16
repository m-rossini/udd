package br.com.auster.udd.node;

import java.util.Map;

/***
 * This class defines the interfaces for XML UDD Formatting.
 * Remember that all UDD attributes are awlways String on entry.
 * @author mtengelm
 *
 */
public interface UDDFormatter {

	/***
	 * Sets the name of the formatter. Can be whatever you want for reference.
	 * @param name
	 */
	public void setName(String name);
	
	/***
	 * Do the formatting itself.
	 * 
	 * @param data The data to be formatted
	 * @return The formatted data
	 */
	public CharSequence format(CharSequence data);
	
	/***
	 * This method receives a Map with formatting options.
	 * It is implementation dependent, but the general rule is:
	 * The Key is the parameter name and the Value is its value.
	 * This method should be called BEFORE format() method.
	 * @param options
	 */
	public void setFormatOptions(Map options);
}
