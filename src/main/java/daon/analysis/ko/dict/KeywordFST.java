package daon.analysis.ko.dict;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.Arc;

import daon.analysis.ko.util.Utils;

/**
 * Thin wrapper around an FST with root-arc caching for Korean.
 * <p>
 * The latter offers additional performance at the cost of more RAM.
 */
public final class KeywordFST {

	//한글 시작 문자
	private final static int start = Utils.KOR_START;
	//한글 종료 문자
	private final static int end = Utils.KOR_END;
	
	private final FST<Long> fst;

	// depending upon fasterButMoreRam, we cache root arcs for either
	// korean (0xAC00-0xD7A3) type="KOREAN"; // 44032, 55203 = 11171
	private final int cacheCeiling;
	private final FST.Arc<Long> rootCache[];

	public final Long NO_OUTPUT;

	public KeywordFST(FST<Long> fst) throws IOException {
		this.fst = fst;
		this.cacheCeiling = end; // 한글 캐싱.
		NO_OUTPUT = fst.outputs.getNoOutput();
		rootCache = cacheRootArcs();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private FST.Arc<Long>[] cacheRootArcs() throws IOException {
		FST.Arc<Long> rootCache[] = new FST.Arc[1 + (end - start)];
		FST.Arc<Long> firstArc = new FST.Arc<>();
		fst.getFirstArc(firstArc);
		FST.Arc<Long> arc = new FST.Arc<>();
		final FST.BytesReader fstReader = fst.getBytesReader();
		// TODO: jump to 3040, readNextRealArc to ceiling? (just be careful we
		// don't add bugs)
		for (int i = 0; i < rootCache.length; i++) {
			if (fst.findTargetArc(start + i, firstArc, arc, fstReader) != null) {
				rootCache[i] = new FST.Arc<Long>().copyFrom(arc);
			}
		}
		return rootCache;
	}

	public FST.Arc<Long> findTargetArc(int ch, FST.Arc<Long> follow, FST.Arc<Long> arc, boolean useCache,
			FST.BytesReader fstReader) throws IOException {
		if (useCache && ch >= start && ch <= end) {
			assert ch != FST.END_LABEL;
			final Arc<Long> result = rootCache[ch - start];
			if (result == null) {
				return null;
			} else {
//				System.out.println("use cache");
				arc.copyFrom(result);
				return arc;
			}
		} else {
			return fst.findTargetArc(ch, follow, arc, fstReader);
		}
	}

	public Arc<Long> getFirstArc(FST.Arc<Long> arc) {
		return fst.getFirstArc(arc);
	}

	public FST.BytesReader getBytesReader() {
		return fst.getBytesReader();
	}

	/**
	 * @lucene.internal for testing only
	 */
	FST<Long> getInternalFST() {
		return fst;
	}
}
