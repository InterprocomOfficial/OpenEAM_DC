package com.ipc.openeam.desktop.util;

import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.dom4j.Node;
import org.dom4j.XPath;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
public class Utils {
	public static final String datepickerConverterPatern = "dd.MM.yyyy";
	public static final DateTimeFormatter uiDateFormat =
			DateTimeFormatter.ofPattern(datepickerConverterPatern);
	public static final String xsdSchemaPath = "/exchange/openEAM-exchange-schema.xsd";
	public static final String openeamNamespace = "http://www.interprocom.ru/openEAM-client-exchange";
	public static final DecimalFormat decimalFormat = new DecimalFormat("#.######",
			DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	public static final DecimalFormat integerFormat = new DecimalFormat("#",
			DecimalFormatSymbols.getInstance(Locale.ENGLISH));



	private static Logger log = Logger.getLogger(Utils.class);

	/**
	 * ^                 # start-of-string
	 * (?=.*[0-9])       # a digit must occur at least once
	 * (?=.*[a-z])       # a lower case letter must occur at least once
	 * (?=.*[A-Z])       # an upper case letter must occur at least once
	 * (?=.*[@#$%^&+=])  # a special character must occur at least once
	 * (?=\S+$)          # no whitespace allowed in the entire string
	 * .{6,}             # anything, at least six places though
	 * $                 # end-of-string
	 */



	@SuppressWarnings("unchecked")
	public static List<Node> getNodeListByPath(Node root, String path) {
		Map<String, String> uris = new HashMap<>();
		uris.put("openeam", openeamNamespace);

		XPath xpath = root.createXPath(path);
		xpath.setNamespaceURIs(uris);
		return xpath.selectNodes(root);
	}

	public static StringConverter<LocalDate> getDatepickerConverter() {
		return new StringConverter<LocalDate>() {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datepickerConverterPatern);

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return dateFormatter.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, dateFormatter);
				} else {
					return null;
				}
			}
		};
	}

}
