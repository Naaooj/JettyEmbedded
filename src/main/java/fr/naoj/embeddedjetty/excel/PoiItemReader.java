package fr.naoj.embeddedjetty.excel;

import java.io.Closeable;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import fr.naoj.embeddedjetty.excel.impl.RowSetFactoryImpl;

public class PoiItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements ResourceAwareItemReaderItemStream<T>, InitializingBean {

	private static final Logger LOG = Logger.getLogger(PoiItemReader.class.getName());
	private final RowSetFactory rowSetFactory = new RowSetFactoryImpl();
	
	private int currentSheet = 0;
	
	private Resource resource;
	private RowMapper<T> rowMapper;
	private Workbook workbook;
    private InputStream workbookStream;
    private Iterator<List<String>> it;
    
    public PoiItemReader(Resource resource) {
    	super();
    	setName(ClassUtils.getShortName(getClass()));
    	setResource(resource);
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public PoiItemReader<T> setRowMapper(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
		return this;
	}

	@Override
	protected void doClose() throws Exception {
		// As of Apache POI 3.11 there is a close method on the Workbook, prior version
        // lack this method.
        if (workbook instanceof Closeable) {
            this.workbook.close();
        }
        
        if (workbookStream instanceof PushbackInputStream) {
//        	((PushbackInputStream) workbookStream).
        }

        if (workbookStream != null) {
            workbookStream.close();
        }
        this.workbook=null;
        this.workbookStream=null;
	}

	@Override
	protected void doOpen() throws Exception {
		if (!this.resource.exists()) {
            LOG.warning("Input resource does not exist '" + this.resource.getDescription() + "'.");
            return;
        }
		if (!this.resource.isReadable()) {
			LOG.warning("Input resource is not readable '" + this.resource.getDescription() + "'.");
            return;
		}
		openExcelFile();
	}

	@Override
	protected T doRead() throws Exception {
		if (this.workbook == null || this.it == null) {
            return null;
        }

        if (it.hasNext()) {
            try {
                return this.rowMapper.mapRow(it.next());
            } catch (final Exception e) {
                throw new Exception("Exception parsing Excel file.", e);
            }
        } else {
            this.currentSheet++;
            if (this.currentSheet >= this.workbook.getNumberOfSheets()) {
                if (LOG.isLoggable(Level.FINE)) {
                	LOG.fine("No more sheets in '" + this.resource.getDescription() + "'.");
                }
                return null;
            } else {
                this.openExcelSheet();
                return this.doRead();
            }
        }	
	}

	private void openExcelFile() throws Exception {
		workbookStream = resource.getInputStream();
        if (!workbookStream.markSupported() && !(workbookStream instanceof PushbackInputStream)) {
            throw new IllegalStateException("InputStream MUST either support mark/reset, or be wrapped as a PushbackInputStream");
        }
        this.workbook = WorkbookFactory.create(workbookStream);
        this.workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
	}
	
	private void openExcelSheet() {
        final Sheet sheet = this.workbook.getSheetAt(this.currentSheet);
        this.it = rowSetFactory.create(sheet).iterator();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Opening sheet " + sheet.getSheetName() + ".");
        }
    }
}
