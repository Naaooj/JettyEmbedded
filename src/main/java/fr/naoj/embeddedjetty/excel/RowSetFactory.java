package fr.naoj.embeddedjetty.excel;

import org.apache.poi.ss.usermodel.Sheet;

public interface RowSetFactory {

	RowSet create(Sheet sheet);
}
