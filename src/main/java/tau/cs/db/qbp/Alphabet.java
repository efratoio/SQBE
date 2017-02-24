package tau.cs.db.qbp;

import java.util.Iterator;

/**
 * Created by efrat on 12/12/16.
 */
class Alphabet implements Iterable<String>{

    private char start;
    private char end;

    public Alphabet() {
        this.start= 'a';
        this.end= 'z';
    }

    @Override
    public Iterator<String> iterator() {
        return new AlphabetIterator(start, end);
    }

    class AlphabetIterator implements Iterator<String>{

        private String current;
        private String end;

        private AlphabetIterator(char start, char end) {
            this.current=String.valueOf(--start);
            this.end=String.valueOf(end);
        }

        @Override
        public boolean hasNext() {
            return (current.charAt(0) < end.charAt(0));
        }

        @Override
        public String next() {
            char nextChar = current.charAt(0);
            return this.current=String.valueOf(++nextChar);
        }
    }


}