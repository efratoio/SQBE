package tau.cs.db.utils;

import com.infomancers.collections.yield.Yielder;

import java.util.List;

/**
 * Created by efrat on 19/12/16.
 */
public class PermutationIterator extends Yielder<List> {

    java.util.List<Integer> arr;
    public PermutationIterator(java.util.List<Integer> arr) {
        this.arr=arr;
    }




    @Override
    protected void yieldNextCore() {

    }
}
