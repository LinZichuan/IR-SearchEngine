import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.Similarity;


public class MySimilarity extends Similarity{
	@Override  
    public float computeNorm(String field, FieldInvertState state) {  
      final int numTerms;  
      if (discountOverlaps)  
        numTerms = state.getLength() - state.getNumOverlap();  
      else  
        numTerms = state.getLength();  
      return (state.getBoost() * lengthNorm(field, numTerms));  
    }  
     
    @Override  
    public float queryNorm(float sumOfSquaredWeights) {  
      return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
      //return 1.0f;
    }  
   
    //term freq 表示 term 在一个document的出现次数,这里设置为1.0f表示不考滤这个因素影响  
    @Override  
    public float tf(float freq) {  
      return (float)Math.sqrt(freq); /** Implemented as <code>sqrt(freq)</code>. */
      //return 1.0f;  
    }  
          
    //这里表示匹配的 term　与 term之间的距离因素,同样也不应该受影响  
    @Override  
    public float sloppyFreq(int distance) {  
      return (float)1 / (distance + 1) ; /** Implemented as <code>1 / (distance + 1)</code>. */  
      //return 1.0f;  
    }  
     
    //这里表示匹配的docuemnt在全部document的影响因素,同理也不考滤  
    @Override  
    public float idf(int docFreq, int numDocs) {  
      //return (float)Math.log((double)numDocs/(docFreq+1) + 1); /** Implemented as <code>log(numDocs/(docFreq+1)) + 1</code>. */  
      return 1.0f;
    }  
          
    //这里表示每一个Document中所有匹配的关键字与当前关键字的匹配比例因素影响,同理也不考滤.  
    @Override  
    public float coord(int overlap, int maxOverlap) {
      return (float)overlap / (float)maxOverlap; /** Implemented as <code>overlap / maxOverlap</code>. */  
      //return 1.0f;  
    }  
  
    // Default false  
    protected boolean discountOverlaps;  
  
    /** Determines whether overlap tokens (Tokens with 
     *  0 position increment) are ignored when computing 
     *  norm.  By default this is false, meaning overlap 
     *  tokens are counted just like non-overlap tokens. 
     * 
     *  <p><b>WARNING</b>: This API is new and experimental, and may suddenly 
     *  change.</p> 
     * 
     *  @see #computeNorm 
     */  
    public void setDiscountOverlaps(boolean v) {  
      discountOverlaps = v;  
    }  
  
    /**@see #setDiscountOverlaps */  
    public boolean getDiscountOverlaps() {  
      return discountOverlaps;  
    }
}
