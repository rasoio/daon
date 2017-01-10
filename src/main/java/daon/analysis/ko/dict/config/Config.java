package daon.analysis.ko.dict.config;

import java.util.HashMap;
import java.util.Map;

import daon.analysis.ko.dict.config.Config.POSTag;

public class Config {

	/**
	 * 설정 parameter key 값 : 사전 타입 구분 값
	 */
	public static final String DICTIONARY_TYPE = "dicType";
	
	/**
	 * 설정 parameter key 값 : 파일명
	 */
	public static final String FILE_NAME = "fileName";
	
	/**
	 * 설정 parameter key 값 : jdbc 정보
	 */
	public static final String JDBC_INFO = "jdbcInfo";
	
	public static final String VALUE_TYPE = "valueType";
	
	private final Map<String, Object> configValues = new HashMap<>();
	
	public static Map<Long,POSTag> bitPosTags = new HashMap<Long,POSTag>();

	public void define(String name, Object value) {
		if (configValues.containsKey(name)) {
			//throw new ConfigException("Configuration " + name + " is defined twice.");
		}
		
		configValues.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type){
		return (T) configValues.get(key);
	}

    /**
     * 확률 기본값
     */
	public static final float DEFAULT_PROBABILITY = 50;

    /**
     * 연결 실패 기본값
     */
    public static final float MISS_PENALTY_SCORE = 100;

	/**
	 * The charactor types
	 */
	public enum CharType {
		
		LOWER("LOWER", 1l << 0), // 소문자 00001
		UPPER("UPPER", 1l << 1), // 대문자 00010
		
		ALPHA("ALPHA", LOWER.getBit() | UPPER.getBit()), // 영문 전체 00011
		
		DIGIT("DIGIT", 1l << 2), // 숫자 // 00100
		KOREAN("KOREAN", 1l << 3), // 국문 // 01000
		
		CHAR("CHAR", ALPHA.getBit() | DIGIT.getBit() | KOREAN.getBit()), // 문자 // 01111

		SPACE("SPACE", 1l << 4), // 공백 // 010000 
		COMMA("COMMA", 1l << 5), // 콤마 // 100000 
		
		ETC("ETC", 1l << 99), // 기타 특수기호
		
		;

		private String name;
		private long bit;

		CharType(String name, long bit) {
			this.name = name;
			this.bit = bit;
		}

		public String getName() {
			return name;
		}

		public long getBit() {
			return bit;
		}
	}
	
	public enum IrrRule {
		irrL("irrL"),  // '러' 불규칙 : 이르다, 누르다, 푸르다
		irrb("irrb"),  // 'ㅂ' 불규칙
		irrd("irrd"),  // 'ㄷ' 불규칙
		irrh("irrh"),  // 'ㅎ' 불규칙
		irrl("irrl"),  // '르' 불규칙 : 가르다, 고르다, 구르다, 기르다
		irrs("irrs"),  // 'ㅅ' 불규칙
		irru("irru"),  // '우' 불규칙 : 푸다
		;
		
		private String name;
		
		IrrRule(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	/**
	 * part of speech tag
	 * 
	 * 한글 품사 
	 * 9품사
	 * 체언 ( 명사, 대명사, 수사 )
	 * 용언 ( 동사, 형용사 )
	 * 관형사
	 * 부사
	 * 감탄사
	 * 조사
	 * 
	 */
	public enum POSTag {
		
		// 체언 ( 명사, 대명사, 수사 )
		na("na", 1l << 0), 	// 01 동작성보통명사
		nc("nc", 1l << 1), 	// 02 보통명사
		nd("nd", 1l << 2), 	// 03 의존명사
		ni("ni", 1l << 3), 	// 04 의문대명사
		nm("nm", 1l << 4), 	// 05 지시대명사
		nn("nn", 1l << 5), 	// 06 수사
		np("np", 1l << 6), 	// 07 인칭대명사
		nr("nr", 1l << 7), 	// 08 고유명사
		ns("ns", 1l << 8), 	// 09 상태성보통명사
		nu("nu", 1l << 9), 	// 10 단위성의존명사
		nb("nb", 1l << 10), // 11 숫자
		
		// 체언 대표
		n("n", na.getBit() | nc.getBit() | nd.getBit() | ni.getBit() | nm.getBit() | nn.getBit() | np.getBit() | nr.getBit() | ns.getBit() | nu.getBit() | nb.getBit()),
		
		// 용언
		vb("vb", 1l << 11), 	// 12 동사
		vi("vi", 1l << 12), 	// 13 의문형용사
		vj("vj", 1l << 13), 	// 14 형용사
		vx("vx", 1l << 14), 	// 15 보조용언
		vn("vn", 1l << 15), 	// 16 부정지정사
		
		// 용언 대표
		v("v", vb.getBit() | vi.getBit() | vj.getBit() | vx.getBit() | vn.getBit()),
				
		// 관형사
		di("di", 1l << 16), 	// 17 의문관형사
		dm("dm", 1l << 17), 	// 18 지시관형사
		dn("dn", 1l << 18), 	// 19 관형사
		du("du", 1l << 19), 	// 20 수관형사
		
		// 관형사 대표
		d("d", di.getBit() | dm.getBit() | dn.getBit() | du.getBit()),
		
		// 부사
		ac("ac", 1l << 20), 	// 21 접속부사
		ad("ad", 1l << 21), 	// 22 부사
		ai("ai", 1l << 22), 	// 23 의문부사
		am("am", 1l << 23), 	// 24 지시부사
		
		// 부사 대표 
		a("a", ac.getBit() | ad.getBit() | ai.getBit() | am.getBit()),
		
		// 조사
		pa("pa", 1l << 24), 	// 25 부사격조사
		pc("pc", 1l << 25), 	// 26 접속조사
		pd("pd", 1l << 26), 	// 27 관형격조사
		po("po", 1l << 27), 	// 28 목적격조사
		pp("pp", 1l << 28), 	// 29 서술격조사
		ps("ps", 1l << 29), 	// 30 주격조사
		pt("pt", 1l << 30), 	// 31 주제격조사
		pv("pv", 1l << 31), 	// 32 호격조사
		px("px", 1l << 32), 	// 33 보조사
		pq("pq", 1l << 33), 	// 34 인용격조사
		pm("pm", 1l << 34), 	// 35 보격조사
		
		papa("papa", pa.getBit()),
		papc("papc", pa.getBit()),
		papd("papd", pa.getBit()),
		papm("papm", pa.getBit()),
		papo("papo", pa.getBit()),
		paps("paps", pa.getBit()),
		papt("papt", pa.getBit()),
		papx("papx", pa.getBit()),
		
		pcpa("pcpa", pc.getBit()),
		pcpd("pcpd", pc.getBit()),
		pcpo("pcpo", pc.getBit()),
		pcps("pcps", pc.getBit()),
		pcpt("pcpt", pc.getBit()),
		pcpx("pcpx", pc.getBit()),
		
		pdpx("pdpx", pd.getBit()),
		
		popa("popa", po.getBit()),
		popo("popo", po.getBit()),
		popx("popx", po.getBit()),
		
		pqpt("pqpt", pq.getBit()),
		pqpx("pqpx", pq.getBit()),
		
		pspa("pspa", ps.getBit()),
		pspc("pspc", ps.getBit()),
		pspd("pspd", ps.getBit()),
		pspo("pspo", ps.getBit()),
		psps("psps", ps.getBit()),
		pspt("pspt", ps.getBit()),
		pspx("pspx", ps.getBit()),
		
		ptpa("ptpa", pt.getBit()),
		ptpd("ptpd", pt.getBit()),
		ptps("ptps", pt.getBit()),
		ptpt("ptpt", pt.getBit()),
		ptpx("ptpx", pt.getBit()),
		
		pvpo("pvpo", pv.getBit()),
		pvpv("pvpv", pv.getBit()),
		
		pxpa("pxpa", px.getBit()),
		pxpc("pxpc", px.getBit()),
		pxpd("pxpd", px.getBit()),
		pxpm("pxpm", px.getBit()),
		pxpo("pxpo", px.getBit()),
		pxps("pxps", px.getBit()),
		pxpt("pxpt", px.getBit()),
		pxpx("pxpx", px.getBit()),
		
		// 조사 대표
		p("p", pa.getBit() | pc.getBit() | pd.getBit() | po.getBit() | pp.getBit() | ps.getBit() | pt.getBit() | pv.getBit() | px.getBit() | pq.getBit() | pm.getBit()),
				
		// 어미
		ec("ec", 1l << 35), 	// 36 연결어미
		ed("ed", 1l << 36), 	// 37 관형사형전성어미
		ef("ef", 1l << 37), 	// 38 어말어미
		en("en", 1l << 38), 	// 39 명사형전성어미
		ep("ep", 1l << 39), 	// 40 선어말어미
		ex("ex", 1l << 40), 	// 41 보조적연결어미
		
		ecpa("ecpa", ec.getBit()),
		ecpc("ecpc", ec.getBit()),
		ecpd("ecpd", ec.getBit()),
		ecpm("ecpm", ec.getBit()),
		ecpo("ecpo", ec.getBit()),
		ecpq("ecpq", ec.getBit()),
		ecps("ecps", ec.getBit()),
		ecpt("ecpt", ec.getBit()),
		ecpx("ecpx", ec.getBit()),
		
		edpa("edpa", ed.getBit()),
		edpc("edpc", ed.getBit()),
		edpo("edpo", ed.getBit()),
		edpx("edpx", ed.getBit()),
		
		efpa("efpa", ef.getBit()),
		efpd("efpd", ef.getBit()),
		efpo("efpo", ef.getBit()),
		efps("efps", ef.getBit()),
		efpt("efpt", ef.getBit()),
		efpx("efpx", ef.getBit()),
		
		// 어미 대표
		e("e", ec.getBit() | ed.getBit() | ef.getBit() | en.getBit() | ep.getBit() | ex.getBit()),
		
		// 접두사 ? 없음..
//		XPN("XPN", 1l << 40), 	// 41 체언 접두사
//		XPV("XPV", 1l << 41), 	// 42 용언 접두사
		
		// 접두사 대표
//		XP("XP", XPN.getBit() | XPV.getBit()),
		
		// 접미사
		xa("xa", 1l << 41), 	// 42 부사파생접미사
		xj("xj", 1l << 42), 	// 43 형용사파생접미사
		xv("xv", 1l << 43), 	// 44 동사파생접미사
		xn("xn", 1l << 44), 	// 45 명사접미사
		
		// 접미사 대표
		x("x", xa.getBit() | xj.getBit() | xv.getBit() | xn.getBit()),

		// 감탄사
		it("it", 1l << 45), 	// 46 감탄사
		
		// 기호 
		sc("sc", 1l << 46), 	// 47 쉼표
		se("se", 1l << 47), 	// 48 줄임표
		sf("sf", 1l << 48), 	// 49 마침표
		sl("sl", 1l << 49), 	// 50 여는따옴표
		sr("sr", 1l << 50), 	// 51 닫는따옴표
		sd("sd", 1l << 51), 	// 52 이음표
		su("su", 1l << 52), 	// 53 단위
		sy("sy", 1l << 53), 	// 54 화폐단위
		so("so", 1l << 54), 	// 55 기타기호
		nh("nh", 1l << 55), 	// 56 한자
		ne("ne", 1l << 56), 	// 57 영어

		cp("cp", 1l << 57), 	// 58 복합어
		un("un", 1l << 58), 	// 59 미등록어
		
		fin("fin", 1l << 59), 	// 60 종료
		
//		쉼표	sc	comma
//		줄임표	se	ellipsis
//		마침표	sf	sentence period
//		여는따옴표	sl	left parenthesis
//		닫는따옴표	sr	right parenthesis
//		이음표	sd	dash
//		단위	su	unit
//		화폐단위	sy	currency
//		기타기호	so	other symbols
//		한자	nh	chinese characters
//		영어	ne	english words
//		복합어	cp	compound
		
		;
		

		private String name;
		private long bit;
		
		POSTag(String name, long bit) {
			this.name = name;
			this.bit = bit;
			
			bitPosTags.put(bit, this);
		}

		public String getName() {
			return name;
		}

		public long getBit() {
			return bit;
		}
	}
	
	/*
	https://github.com/dsindex/ckyfd/blob/master/wrapper/python/rouzeta.py
	
	ROUZETA_SEJONG_TAG_MAP = {
			'ac':'MAJ', # 접속부사	ac	conjunctive adverb	또는, 그러나, ...
			'ad':'MAG', # 부사	ad	adverb	매우, 과연, ...
			'ai':'MAG', # 의문부사	ai	interrogative adverb	어디, 언제, ...
			'am':'MAG', # 지시부사	am	demonstrative adverb	여기, 저기, ...
			'di':'MM',  # 의문관형사	di	interrogative adnoun	어느, 몇, ...
			'dm':'MM',  # 지시관형사	dm	demonstrative adnoun	이, 그, 저, ...
			'dn':'MM',  # 관형사	dn	adnoun	새, 헌, ...
			'du':'MM',  # 수관형사	du	numeral adnoun	한, 두, ...
			'ec':'EC',  # 연결어미	ec	conjunctive ending	~며, ~고, ...
			'ed':'ETM', # 관형사형전성어미	ed	adnominal ending	~는, ~ㄹ, ...
			'ef':'EF',  # 어말어미	ef	final ending	~다, ~는다, ...
			'en':'ETN', # 명사형전성어미	en	nominal ending	~ㅁ, ~기 (두 개뿐)
			'ep':'EP',  # 선어말어미	ep	prefinal ending	~았, ~겠, ...
			'ex':'EC',  # 보조적연결어미	ex	auxiliary ending	~아, ~고, ...
			'it':'IC',  # 감탄사	it	interjection	앗, 거참, ...
			'na':'NNG', # 동작성보통명사	na	active common noun	가맹, 가공, ...
			'nc':'NNG', # 보통명사	nc	common noun	가관, 가극, ...
			'nd':'NNB', # 의존명사	nd	dependent noun	겨를, 곳, ...
			'ni':'NP',  # 의문대명사	ni	interrogative pronoun	누구, 무엇, ...
			'nm':'NP',  # 지시대명사	nm	demonstrative pronoun	이, 이것, ...
			'nn':'NR',  # 수사	nn	numeral	공, 다섯, ...
			'np':'NP',  # 인칭대명사	np	personal pronoun	나, 너, ...
			'nr':'NNP', # 고유명사	nr	proper noun	YWCA, 홍길동, ...
			'ns':'NNG', # 상태성보통명사	ns	stative common noun	간결, 간절, ...
			'nu':'NNB', # 단위성의존명사	nu	unit dependent noun	그루, 군데, ...
			'nb':'SN',  # 숫자	nb	number	1, 2, ...
			'pa':'JKB', # 부사격조사	pa	number	~에, ~에서, ...
			'pc':'JC',  # 접속조사	pc	conjunctive particle	~나, ~와, ...
			'pd':'JKG', # 관형격조사	pd	adnominal particle	~의 (한 개)
			'po':'JKO', # 목적격조사	po	adnominal particle	~을, ~를, ~ㄹ
			'pp':'VCP', # 서술격조사	pp	predicative particle	~이~ (한 개) !! '사람이다'
			'ps':'JKS', # 주격조사	ps	subjective particle	~이, ~가, ...
			'pt':'JX',  # 주제격조사	pt	thematic particle	~은, ~는, (두 개)
			'pv':'JKV', # 호격조사	pv	vocative particle	~야, ~여, ~아, ...
			'px':'JX',  # 보조사	px	auxiliary particle	~ㄴ, ~만, ~치고, ...
			'pq':'JKQ', # 인용격조사	pq	quotative particle	~고, ~라고, ~라, ...
			'pm':'JKC', # 보격조사	pm	complementary particle	~이, ~가 (두 개)
			'vb':'VV',  # 동사	vb	verb	감추~, 같~, ...
			'vi':'VA',  # 의문형용사	vi	interrogative adjective	어떠하~, 어떻~, ...
			'vj':'VA',  # 형용사	vj	adjective	가깝~, 괜찮~, ...
			'vx':'VX',  # 보조용언	vx	auxiliary verb	나~, 두~, ...
			'vn':'VCN', # 부정지정사	vn	auxiliary verb	아니~ (하나)
			'xa':'EC',  # 부사파생접미사	xa	adverb derivational suffix	~게, ~이, ... !! '없이,없게'
			'xj':'XSA', # 형용사파생접미사	xj	adjective derivational suffix	같~, 높~, 답~, ...
			'xv':'XSV', # 동사파생접미사	xv	verb derivational suffix	~하, ~시키, ...
			'xn':'XSN', # 명사접미사	xn	noun suffix	~군, ~꾼, ...
			'sc':'SP',  # 쉼표	sc	comma	:, ,, ...
			'se':'SP',  # 줄임표	se	ellipsis	…
			'sf':'SF',  # 마침표	sf	sentence period	!, ., ?
			'sl':'SP',  # 여는따옴표	sl	left parenthesis	(, <, [, ...
			'sr':'SP',  # 닫는따옴표	sr	right parenthesis	), >, ], ...
			'sd':'SP',  # 이음표	sd	dash	-, ...
			'su':'SL',  # 단위	su	unit	Kg, Km, bps, ...
			'sy':'SW',  # 화폐단위	sy	currency	$, ￦, ...
			'so':'SP',  # 기타기호	so	other symbols	α, φ, ...
			'nh':'SH',  # 한자	nh	chinese characters	丁, 七, 万, ...
			'ne':'SL',  # 영어	ne	english words	computer, ...
		}
		*/
	
	/*
	public enum POSTag {
		// 체언 ( 명사, 대명사, 수사 )
		NNG("NNG", 1l << 0), 	// 01 일반 명사
		NNP("NNP", 1l << 1), 	// 02 고유 명사
		NNB("NNB", 1l << 2), 	// 03 의존 명사
		NNM("NNM", 1l << 3), 	// 04 단위 명사
		NR("NR", 1l << 4), 	// 05 수사
		NP("NP", 1l << 5), 	// 06 대명사
		
		// 보통 명사 + 고유명사
		NNA("NNA", NNG.getBit() | NNP.getBit()),
		
		// 명사 (명사 + 수사 + 명사 추정 미등록어)	
		NN("NN", NNA.getBit() | NNB.getBit() | NNM.getBit() | NR.getBit()),
		
		// 체언 대표
		N("N", NN.getBit() | NP.getBit()),
		
		// 용언
		VV("VV", 1l << 6), 	// 07 동사
		VA("VA", 1l << 7), 	// 08 형용사
		VXV("VXV", 1l << 8), 	// 09 보조 동사
		VXA("VXA", 1l << 9), 	// 10 보조 형용사
		VCP("VCP", 1l << 10), 	// 11 긍정 지정사
		VCN("VCN", 1l << 11), 	// 12 부정 지정사
		
		// 보조 용언 ( 사용 범위.. )
		VX("VX", VXV.getBit() | VXA.getBit()),
		
		// 서술격 조사 '이다'를 제외한 용언
		VP("VP", VV.getBit() | VA.getBit() | VX.getBit() | VCN.getBit() ),
		
		// 지정사
		VC("VC", VCN.getBit() | VCP.getBit() ),

		// 용언 대표
		V("V", VP.getBit() | VCP.getBit() ),
		
		// 관형사
		MDN("MDN", 1l << 12), 	// 13 수 관형사
		MDT("MDT", 1l << 13), 	// 14 기타 관형사
		
		// 관형사 대표
		MD("MD", MDN.getBit() | MDT.getBit()),
		
		// 부사
		MAG("MAG", 1l << 14), 	// 15 일반 부사
		MAC("MAC", 1l << 15), 	// 16 접속 부사
		
		// 부사 대표 
		MA("MA", MAG.getBit() | MAC.getBit()),
		
		// 수식언
		M("M", MD.getBit() | MA.getBit()),
		
		// 감탄사
		IC("IC", 1l << 16), 	// 17 감탄사
		
		// 조사
		JKS("JKS", 1l << 17), 	// 18 주격 조사
		JKC("JKC", 1l << 18), 	// 19 보격 조사
		JKG("JKG", 1l << 19), 	// 20 관형격 조사
		JKO("JKO", 1l << 20), 	// 21 목적격 조사
		JKM("JKM", 1l << 21), 	// 22 부사격 조사
		JKI("JKI", 1l << 22), 	// 23 호격 조사
		JKQ("JKQ", 1l << 23), 	// 24 인용격 조사
		JX("JX", 1l << 24), 	// 25 보조사
		JC("JC", 1l << 25), 	// 26 접속 조사
		
		// 격조사 대표
		JK("JK", JKS.getBit() | JKC.getBit() | JKG.getBit() | JKO.getBit() | JKM.getBit() | JKI.getBit() | JKQ.getBit() ),
		
		// 조사 대표
		J("J", JK.getBit() | JX.getBit() | JC.getBit()),
		
		// 어미
		EPH("EPH", 1l << 26), 	// 27 존칭 선어말 어미
		EPT("EPT", 1l << 27), 	// 28 시제 선어말 어미
		EPP("EPP", 1l << 28), 	// 29 공손 선어말 어미
		EFN("EFN", 1l << 29), 	// 30 기본 종결 어미
		EFQ("EFQ", 1l << 30), 	// 31 의문 종결 어미
		EFO("EFO", 1l << 31), 	// 32 명령 종결 어미
		EFA("EFA", 1l << 32), 	// 33 청유 종결 어미
		EFI("EFI", 1l << 33), 	// 34 감탄 종결 어미
		EFR("EFR", 1l << 34), 	// 35 존칭 종결 어미
		ECE("ECE", 1l << 35), 	// 36 대등 연결 어미
		ECD("ECD", 1l << 36), 	// 37 의존 연결 어미
		ECS("ECS", 1l << 37), 	// 38 보조 연결 어미
		ETN("ETN", 1l << 38), 	// 39 명사형 전성 어미
		ETD("ETD", 1l << 39), 	// 40 관형형 전성 어미
		
		// 선어말 어미 대표 
		EP("EP", EPH.getBit() | EPT.getBit() | EPP.getBit()),
		
		// 종결형 어말 어미 대표
		EF("EF", EFN.getBit() | EFQ.getBit() | EFO.getBit() | EFA.getBit() | EFI.getBit() | EFR.getBit()),
		
		// 연결형  어말 어미 대표
		EC("EC", ECE.getBit() | ECD.getBit() | ECS.getBit()),
		
		// 전성형 어말 어미 대표
		ET("ET", ETN.getBit() | ETD.getBit()),
		
		// 어말 어미 대표
		EM("EM", EF.getBit() | EC.getBit() | ET.getBit()),
		
		// 어미 대표
		E("E", EP.getBit() | EM.getBit()),
		
		// 접두사
		XPN("XPN", 1l << 40), 	// 41 체언 접두사
		XPV("XPV", 1l << 41), 	// 42 용언 접두사
		
		// 접두사 대표
		XP("XP", XPN.getBit() | XPV.getBit()),
		
		// 접미사
		XSN("XSN", 1l << 42), 	// 43 명사 파생접미사
		XSV("XSV", 1l << 43), 	// 44 동사 파생접미사
		XSA("XSA", 1l << 44), 	// 45 형용사 파생접미사
		XSM("XSM", 1l << 45), 	// 46 부사 파생접미사
		XSO("XSO", 1l << 46), 	// 47 기타 접미사
		XR("XR", 1l << 47), 	// 48 어근
		
		// 접미사 대표
		XS("XS", XSN.getBit() | XSV.getBit() | XSA.getBit() | XSM.getBit() | XSO.getBit())

		// 기호
		//public static final long	S	= SF | SP | SS | SE | SO | SW;
		
		
		// 기호
//		"SY", 	// 49 기호 일반
//		"SF", 	// 50 마침표물음표,느낌표
//		"SP", 	// 51 쉼표,가운뎃점,콜론,빗금
//		"SS", 	// 52 따옴표,괄호표,줄표
//		"SE", 	// 53 줄임표
//		"SO", 	// 54 붙임표(물결,숨김,빠짐)
//		"SW", 	// 55 기타기호 (논리수학기호,화폐기호)
//		// 분석 불능
//		"UN", 	// 56 명사추정범주
//		"UV", 	// 57 용언추정범주
//		"UE", 	// 58 분석불능범주
//		// 한글 이외
//		"OL", 	// 59 외국어
//		"OH", 	// 60 한자
//		"ON", 	// 61 숫자
//		"BOS", 	// 62 문장의 시작
//		"EMO", 	// 63 그림말 (Emoticon)
		;
		

		private String name;
		private long bit;
		
		POSTag(String name, long bit) {
			this.name = name;
			this.bit = bit;
			
			bitPosTags.put(bit, this);
		}

		public String getName() {
			return name;
		}

		public long getBit() {
			return bit;
		}
	}
	*/
	
	
	/**
	 * 조합 규칙룰 정의
	 * @author mac
	 */
	public enum AlterRules {
		IrrConjl,      //'르' 불규칙 (/irrl) 
		IrrConjYEO,    //'하다' + '어'   => '하여'    '하다' + '어'   => '해' 
		DropEU,        //'으' 탈락 현상 : 모아 : 모으 + 아
		InsertEU,      //매개모음 '으'의 삽입 현상
		DropL,         //'ㄹ' 탈락 현상 : 잘 아네 (알 + 네)
		DropS,         //'ㅅ' 불규칙 현상 (/irrs) : 그었다 (긋 + 었다)
		IrrConjD,      //'ㄷ' 불규칙 현상 (/irrd) : 깨달아 (깨닫 + 아)
		IrrConjB,      //'ㅂ' 불규칙 (/irrb) : 도우면 (돕 + 면)
		IrrConjL,      //'러' 불규칙 (/irrL) : 이르러 (이르 + 어)
		IrrEola,       //'거라' 불규칙/'너라' 불규칙
		IrrConjH,      //'ㅎ' 불규칙 활용
		ConjDiph,      //이중모음 법칙
		ConjEAE,       //어미가 '애'/'에'로 끝나는 용언 뒤에 '어'가 올 때 '어'의 탈락 현상
		ChangeNullCoda,//종성으로 시작하는 조합 
		ShortenYIPP,   //서술격 조사 '이' 탈락 현상
	}
}
