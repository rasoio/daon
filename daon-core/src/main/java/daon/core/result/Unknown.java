package daon.core.result;

import java.util.*;

/**
 * Created by mac on 2017. 8. 3..
 */
public class Unknown{

    private LinkedList<Position> list = new LinkedList<>();

    public Unknown() {}

    public Position getLast(){
        return list.getLast();
    }

    //array add
    public void add(int offset) {

        Position position = new Position(offset, 1);

        list.add(position);
    }

    public boolean isExist(){
        return list.size() > 0;
    }

    //sublist to size
    public List<Position> getList(){

        return list;
    }


    public static class Position{
        private int offset;
        private int length;

        public Position(int offset, int length) {
            this.offset = offset;
            this.length = length;
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
    }
}
