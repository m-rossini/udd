package br.com.auster.udd.node;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class UDDSimpleAttributeFormatter implements UDDFormatter {

	public static final String TYPE_ATTR = "type";
	public static final String FORMAT_ATTR = "format";
	public static final String PARSE_ATTR = "parse";
	public static final String DATE_TYPE = "date";
	public static final String NUMBER_TYPE = "number";
	
	private static final Logger log = Logger.getLogger(UDDSimpleAttributeFormatter.class);

	protected String type = null;
	protected String name;
  
	private DecimalFormat numFormat = null;
  private DecimalFormat numParser = null;
  
  private SimpleDateFormat datFormat = null;
  private SimpleDateFormat datParser = null;
	
	public CharSequence format(CharSequence data) {
		if (this.type.equals(DATE_TYPE)) {
			return formatDate(data);
		} else if (this.type.equals(NUMBER_TYPE)) {
			return formatNumber(data);
		} else {
			return data;
		}		
	}

	public void setFormatOptions(Map options) {
	  this.type = (String) options.get(TYPE_ATTR);
	  if (this.type == null || this.type.length() == 0) {
	    throw new IllegalArgumentException("'type' attribute cannot be null/empty.");
	  }
	  final String format = (String) options.get(FORMAT_ATTR);
	  if (format != null && format.length() > 0) {
	    if (this.type.equals(DATE_TYPE)) {
	      this.datFormat = new SimpleDateFormat(format);
	    } else if (this.type.equals(NUMBER_TYPE)) {
	      this.numFormat = new DecimalFormat(format);
	    }
	  } else if (this.type.equals(DATE_TYPE)) {
	    this.datFormat = new SimpleDateFormat();
	  }
	  final String parse = (String) options.get(PARSE_ATTR);
	  if (parse != null && parse.length() > 0) {
	    if (this.type.equals(DATE_TYPE)) {
	      this.datParser = new SimpleDateFormat(parse);
	    } else if (this.type.equals(NUMBER_TYPE)) {
	      this.numParser = new DecimalFormat(parse);
	    }
	  } else if (this.type.equals(DATE_TYPE)) {
	    this.datParser = new SimpleDateFormat();
	  }
	}

	public void setName(String name) {
		this.name = name;
	}

	public CharSequence formatNumber(CharSequence data) {
		if (data.length() == 0) {
			return data;
		}
		try {
      Number number;
      if (this.numParser == null) {
        number = Double.valueOf(data.toString());
      } else {
        number = this.numParser.parse(data.toString());
      }
      if (this.numFormat == null) {
        data = String.valueOf(number);
      } else {
        data = this.numFormat.format(number);
      }
		} catch (Exception e) {
			log.error("Error while parsing/formatting number: [" + data + "]", e);
    }
		return data;
	}
  
	public CharSequence formatDate(CharSequence data) {
		if (data.length() == 0) {
			return data;
		}
		try {
			data = this.datFormat.format(this.datParser.parse(data.toString()));
		} catch (Exception e) {
			log.error("Error while parsing/formatting date: [" + data + "]", e);
    }
		return data;
	}	
}
