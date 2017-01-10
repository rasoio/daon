package daon.analysis.ko.dict.connect;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import daon.analysis.ko.dict.config.Config;
import daon.analysis.ko.dict.config.Config.POSTag;
import daon.analysis.ko.dict.reader.Reader;
import daon.analysis.ko.model.TagConnection;
import daon.analysis.ko.model.TagInfo;

public class ConnectMatrixBuilder {

	private Logger logger = LoggerFactory.getLogger(ConnectMatrixBuilder.class);

	private Config config = new Config();
	private Reader<TagConnection> reader;
	
	public static ConnectMatrixBuilder create() {
		return new ConnectMatrixBuilder();
	}

	private ConnectMatrixBuilder() {}

	public final ConnectMatrixBuilder setFileName(final String fileName) {
		this.config.define(Config.FILE_NAME, fileName);
		return this;
	}
	
	public final ConnectMatrixBuilder setReader(final Reader<TagConnection> reader) {
		this.reader = reader;
		return this;
	}
	
	public final ConnectMatrixBuilder setValueType(final Class<TagConnection> valueType) {
		this.config.define(Config.VALUE_TYPE, valueType);
		return this;
	}
	
	public ConnectMatrix build() throws IOException{
		
		if(reader == null){
			//TODO throw exception 
		}
		
		try{
			reader.read(config);

			//최대 사이즈
			int size = Config.POSTag.fin.getIdx() + 1;

			float connProb[][] = new float[size][size];
			float rootProb[] = new float[size];

			//초기화
            for(int i=0; i< size;i++){
                for(int j=0; j< size;j++){
                    connProb[i][j] = Config.MISS_PENALTY_SCORE;
                }

                rootProb[i] = Config.MISS_PENALTY_SCORE;
            }

			while (reader.hasNext()) {
				TagConnection tag = reader.next();

				String mainTagName = tag.getTag();

				for(TagInfo subTag : tag.getTags()){
					
					POSTag subPosTag = subTag.getTag();

					if("Root".equals(mainTagName)){
						rootProb[subPosTag.getIdx()] = subTag.getProb();

					}else{
						POSTag mainPosTag = POSTag.valueOf(mainTagName);
						connProb[mainPosTag.getIdx()][subPosTag.getIdx()] = subTag.getProb();
					}
				}
			}

			return new ConnectMatrix(rootProb, connProb);
		} finally {
			reader.close();
		}
	}
}
