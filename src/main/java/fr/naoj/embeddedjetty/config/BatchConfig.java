package fr.naoj.embeddedjetty.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;

import fr.naoj.embeddedjetty.dto.XlsDto;
import fr.naoj.embeddedjetty.excel.PoiItemReader;
import fr.naoj.embeddedjetty.excel.impl.RowMapperImpl;

@Configuration
public class BatchConfig {

	@Bean
	ItemReader<XlsDto> reader() {
		ItemReader<XlsDto> reader;
		try {
			reader = new PoiItemReader<XlsDto>(new InputStreamResource(new FileInputStream("poi_test.xlsx"))).setRowMapper(new RowMapperImpl<XlsDto>());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			reader = null;
		}
		return reader;
	}
	
	@Bean
    ItemProcessor<XlsDto, XlsDto> excelProcessor() {
        return new ItemProcessor<XlsDto, XlsDto>() {
			@Override
			public XlsDto process(XlsDto arg0) throws Exception {
				return arg0;
			}
		};
    }

    @Bean
    ItemWriter<XlsDto> excelWriter() {
        return new ItemWriter<XlsDto>() {
			@Override
			public void write(List<? extends XlsDto> arg0) throws Exception {
				
			}
		};
    }
    
    @Bean
    Step excelFileToXmlStep(ItemReader<XlsDto> excelReader,
                         ItemProcessor<XlsDto, XlsDto> excelProcessor,
                         ItemWriter<XlsDto> excelWriter,
                         StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("excelFileToXmlStep")
                .<XlsDto, XlsDto>chunk(1)
                .reader(excelReader)
                .processor(excelProcessor)
                .writer(excelWriter)
                .build();
    }
    
    @Bean
    Job excelFileToXmlJob(JobBuilderFactory jobBuilderFactory, @Qualifier("excelFileToXmlStep") Step excelStep) {
        return jobBuilderFactory.get("excelFileToXmlJob")
                .incrementer(new RunIdIncrementer())
                .flow(excelStep)
                .end()
                .build();
    }
}
