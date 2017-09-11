package daon.core.model;

import daon.core.config.MatchType;

import java.util.Arrays;

/**
 * 분석 결과
 */
public class Node {

    private Node prev;
    private Node next;

    // 같은 end idx 의 node
    private Node endNext;

    private Node beginNext;


    private boolean isFirst;
    private boolean isMatchAll;

    /**
     * 누적 cost
     */
    private int backtraceCost;

    /**
     * 분석 결과 offset 위치 정보
     */
    private int offset;

    /**
     * word 길이
     */
    private int length;

    /**
     * 표층어
     */
    private String surface;

    /**
     * 분석 결과
     */
    private Keyword[] keywords;

    private Keyword first;

    private Keyword last;

    /**
     * 현재 노드 cost
     */
    private int cost;

    private MatchType type;

    public Node(int offset, int length, String surface, int cost, MatchType type, Keyword... keywords) {
        this.offset = offset;
        this.length = length;
        this.surface = surface;
        this.cost = cost;
        this.type = type;
        this.keywords = keywords;

        int size = keywords.length;

        //keyword 가 없으면 error
        first = keywords[0];
        last = keywords[size - 1];

    }

    public Node(int offset, int length, String surface, MatchType type) {
        this.offset = offset;
        this.length = length;
        this.surface = surface;
        this.type = type;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public Keyword getFirst() {
        return first;
    }

    public Keyword getLast() {
        return last;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isMatchAll() {
        return isMatchAll;
    }

    public void setMatchAll(boolean matchAll) {
        isMatchAll = matchAll;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getBacktraceCost() {
        return backtraceCost;
    }

    public void setBacktraceCost(int backtraceCost) {
        this.backtraceCost = backtraceCost;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getEndNext() {

        //전체 매칭 된 경우 전체 매칭 된 것만 사용함.
        if(isMatchAll){
            if(endNext != null && endNext.isMatchAll){
                return endNext;
            }else{
                return null;
            }
        }

        return endNext;
    }

    public void setEndNext(Node endNext) {
        this.endNext = endNext;
    }

    public Node getBeginNext() {

        //전체 매칭 된 경우 전체 매칭 된 것만 사용함.
        if(isMatchAll){
            if(beginNext != null && beginNext.isMatchAll){
                return beginNext;
            }else{
                return null;
            }
        }

        return beginNext;
    }

    public void setBeginNext(Node beginNext) {
        this.beginNext = beginNext;
    }

    public Keyword[] getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "{" +
                "offset=" + offset +
                ", length=" + length +
                ", surface=" + surface +
                ", keywords=" + Arrays.toString(keywords) +
                ", type=" + type +
                '}';
    }
}
