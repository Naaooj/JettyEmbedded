package fr.naoj.embeddedjetty.excel;

import java.util.List;

public interface RowMapper<T> {

	T mapRow(List<String> rowSet) throws Exception;
}
