package daon.manager.service;

import daon.analysis.ko.DaonAnalyzer2;
import daon.analysis.ko.model.CandidateTerm;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 2017. 3. 9..
 */
@Slf4j
@Service
public class AnalyzeService {

	@Autowired
	private TransportClient esClient;

	@Autowired
	private DaonAnalyzer2 analyzer;


	public List<CandidateTerm> analyze(String text) throws IOException {
//		List<CandidateTerm> terms = new ArrayList<>();

		List<CandidateTerm> terms = analyzer.analyze(text);

		return terms;
	}

}