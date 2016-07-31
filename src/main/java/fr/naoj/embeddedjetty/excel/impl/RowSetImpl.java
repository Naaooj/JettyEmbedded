package fr.naoj.embeddedjetty.excel.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import fr.naoj.embeddedjetty.excel.RowSet;

public class RowSetImpl implements RowSet {

	private final Sheet sheet;
	private int nbrOfCOlumns = -1;
	private int currentRowIndex = -1;
	
	public RowSetImpl(Sheet sheet) {
		this.sheet = sheet;
	}
	
	@Override
	public Iterator<List<String>> iterator() {
		return new Itr();
	}
	
	private int getNbrOfColumns() {
		if (this.nbrOfCOlumns < 0) {
			this.nbrOfCOlumns = this.sheet.getRow(0).getLastCellNum();
		}
		return this.nbrOfCOlumns;
	}
	
	private class Itr implements Iterator<List<String>> {

		@Override
		public boolean hasNext() {
	        if (currentRowIndex < sheet.getLastRowNum() + 1) {
	            return true;
	        }
	        return false;
		}

		@Override
		public List<String> next() {
			currentRowIndex++;
			return getCellsOfRow(sheet.getRow(currentRowIndex));
		}
		
		private List<String> getCellsOfRow(Row row) {
			if (row == null) {
				return Collections.emptyList();
			}
			
			List<String> cells = new ArrayList<>();
			
			Cell cell;
			for (int i = 0; i < getNbrOfColumns(); ++i) {
				cell = row.getCell(i);
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
				case Cell.CELL_TYPE_BLANK:
					cells.add(cell.getStringCellValue());
					break;
				default:
					break;
				}
			}
			
			return cells;
		}
	}
}
