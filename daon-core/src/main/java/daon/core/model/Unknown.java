package daon.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 2017. 8. 3..
 */
public class Unknown{


    private int offset = -1;
    private int length;

    private boolean ing = false;
    private boolean start = false;

    private List<Position> list = new ArrayList<>();

    public Unknown() {}

    public void add(int findOffset) {

        //continue
        if(offset + length == findOffset){
            length ++;
            ing = true;
        }
        //restart
        else{

            if(ing) {
                //create and restart
                Position position = new Position(offset, length);
                list.add(position);

                offset = findOffset;
                length = 1;

                ing = false;
                start = true;
            }else{
                //init
                offset = findOffset;
                length = 1;

                ing = true;
                start = true;
            }

        }
    }

    public boolean isExist(){
        return offset > -1;
    }

    public List<Position> getList(){
        //flush
        if(start || ing){
            Position position = new Position(offset, length);
            list.add(position);
        }

        return list;
    }


    public class Position{
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
