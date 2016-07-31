package fr.naoj.embeddedjetty.excel.impl;

import org.apache.poi.ss.usermodel.Sheet;

import fr.naoj.embeddedjetty.excel.RowSet;
import fr.naoj.embeddedjetty.excel.RowSetFactory;

public class RowSetFactoryImpl implements RowSetFactory {

	@Override
	public RowSet create(Sheet sheet) {
		return new RowSetImpl(sheet);
	}

}
