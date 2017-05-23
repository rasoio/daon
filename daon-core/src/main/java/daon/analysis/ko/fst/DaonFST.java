package daon.analysis.ko.fst;

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

import daon.analysis.ko.util.Utils;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.*;
import org.apache.lucene.util.fst.FST.Arc;
import org.apache.lucene.util.fst.PairOutputs.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Thin wrapper around an FST with root-arc caching for Korean.
 * <p>
 * The latter offers additional performance at the cost of more RAM.
 */
public final class DaonFST<T> implements Serializable{

    //한글 시작 문자
    private final static int start = Utils.KOR_START;
    //한글 종료 문자
    private final static int end = Utils.KOR_END;

    private final FST<T> fst;

    // depending upon fasterButMoreRam, we cache root arcs for either
    // korean (0xAC00-0xD7A3) type="KOREAN"; // 44032, 55203 = 11171
    private final Arc<T> rootCache[];

    public final T NO_OUTPUT;

    public DaonFST(FST<T> fst) throws IOException {
        this.fst = fst;
        NO_OUTPUT = fst.outputs.getNoOutput();
        rootCache = cacheRootArcs();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Arc<T>[] cacheRootArcs() throws IOException {
        Arc<T> rootCache[] = new Arc[1 + (end - start)];
        Arc<T> firstArc = new Arc<>();
        fst.getFirstArc(firstArc);
        Arc<T> arc = new Arc<>();
        final FST.BytesReader fstReader = fst.getBytesReader();
        // TODO: jump to 3040, readNextRealArc to ceiling? (just be careful we
        // don't add bugs)
        for (int i = 0; i < rootCache.length; i++) {
            if (fst.findTargetArc(start + i, firstArc, arc, fstReader) != null) {
                rootCache[i] = new Arc<T>().copyFrom(arc);
            }
        }
        return rootCache;
    }

    public Arc<T> findTargetArc(int ch, Arc<T> follow, Arc<T> arc, boolean useCache,
                                          FST.BytesReader fstReader) throws IOException {
        if (useCache && ch >= start && ch <= end) {
            assert ch != FST.END_LABEL;
            final Arc<T> result = rootCache[ch - start];
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

    public Arc<T> getFirstArc(Arc<T> arc) {
        return fst.getFirstArc(arc);
    }

    public FST.BytesReader getBytesReader() {
        return fst.getBytesReader();
    }

    public Outputs<T> getOutputs() {
        return fst.outputs;
    }

    public FST<T> getInternalFST() {
        return fst;
    }

    
    public List<PairOutputs.Pair<Long,IntsRef>> asList(T outputs){

        return ((ListOfOutputs)getOutputs()).asList(outputs);
    }
}
