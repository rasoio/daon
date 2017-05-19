package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer;
import daon.analysis.ko.model.Term;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class AnalyzeService {

	@Autowired
	private DaonAnalyzer analyzer;


	public List<Term> analyze(String text) throws IOException {

		List<Term> terms = analyzer.analyze(text);

		return terms;
	}

}