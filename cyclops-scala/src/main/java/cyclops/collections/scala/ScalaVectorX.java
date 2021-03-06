package cyclops.collections.scala;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops.scala.collections.HasScalaCollection;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPVectorX;
import com.aol.cyclops2.types.Unwrapable;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.VectorX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PVector;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import lombok.experimental.Wither;
import scala.collection.GenTraversableOnce;
import scala.collection.generic.CanBuildFrom;
import scala.collection.immutable.Vector;
import scala.collection.immutable.Vector$;
import scala.collection.immutable.VectorBuilder;
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScalaVectorX<T> extends AbstractList<T> implements PVector<T>, HasScalaCollection<T>, Unwrapable {

    public static <T> VectorX<T> vectorX(ReactiveSeq<T> stream){
        return fromStream(stream);
    }
    @Override
    public <R> R unwrap() {
        return (R)vector;
    }

    public LazyPVectorX<T> plusLoop(int max, IntFunction<T> value){
        
            Vector<T> toUse = vector;
            for(int i=0;i<max;i++){
                toUse = toUse.appendBack(value.apply(i));
            }
            return lazyVector(toUse);

    }
    public LazyPVectorX<T> plusLoop(Supplier<Optional<T>> supplier){
        Vector<T> toUse = vector;
        Optional<T> next =  supplier.get();
        while(next.isPresent()){
            toUse = toUse.appendBack(next.get());
            next = supplier.get();
        }
        return lazyVector(toUse);
    }

    
    /**
     * Create a LazyPVectorX from a Stream
     * 
     * @param stream to construct a LazyQueueX from
     * @return LazyPVectorX
     */
    public static <T> LazyPVectorX<T> fromStream(Stream<T> stream) {
        return new LazyPVectorX<T>(null, ReactiveSeq.fromStream(stream),toPVector(), Evaluation.LAZY);
    }

    /**
     * Create a LazyPVectorX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyPVectorX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyPVectorX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyPVectorX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a ListX
     * 
     * <pre>
     * {@code 
     *  LazyPVectorX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ListX generated by unfolder function
     */
    public static <U, T> LazyPVectorX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPVectorX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> LazyPVectorX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                      .limit(limit));
    }

    /**
     * Create a LazyPVectorX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T> LazyPVectorX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                      .limit(limit));
    }

    /**
     * <pre>
     * {@code 
     * PVector<Integer> q = JSPVector.<Integer>toPVector()
                                     .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PVector
     */
    public static <T> Reducer<PVector<T>> toPVector() {
        return Reducer.<PVector<T>> of(ScalaVectorX.emptyPVector(), (final PVector<T> a) -> b -> a.plusAll(b), (final T x) -> ScalaVectorX.singleton(x));
    }
    
    public static <T> VectorBuilder<T> builder(){
        return new VectorBuilder<T>();
    }
    public static <T> ScalaVectorX<T> fromVector(Vector<T> vector){
        return new ScalaVectorX<>(vector);
    }
    public static <T> LazyPVectorX<T> lazyVector(Vector<T> vector){
        return fromPVector(fromVector(vector), toPVector());
    }

    private static <T> LazyPVectorX<T> fromPVector(PVector<T> vec, Reducer<PVector<T>> pVectorReducer) {
        return new LazyPVectorX<T>(vec,null, pVectorReducer,Evaluation.LAZY);
    }

    public static <T> ScalaVectorX<T> emptyPVector(){
        return new ScalaVectorX<>(Vector$.MODULE$.empty());
    }
    public static <T> LazyPVectorX<T> empty(){
        return fromPVector(new ScalaVectorX<>(Vector$.MODULE$.empty()), toPVector());
    }
    public static <T> LazyPVectorX<T> singleton(T t){
        Vector<T> result = Vector$.MODULE$.empty();
        return fromPVector(new ScalaVectorX<>(result.appendFront(t)), toPVector());
    }
    public static <T> LazyPVectorX<T> of(T... t){
        VectorBuilder<T> vb = new VectorBuilder<T>();
        for(T next : t)
            vb.$plus$eq(next);
        Vector<T> vec = vb.result();
        return fromPVector(new ScalaVectorX<>(vec), toPVector());
    }
    public static <T> LazyPVectorX<T> PVector(Vector<T> q) {
        return fromPVector(new ScalaVectorX<T>(q), toPVector());
    }
    @SafeVarargs
    public static <T> LazyPVectorX<T> PVector(T... elements){
        return fromPVector(of(elements),toPVector());
    }
    @Wither
    final Vector<T> vector;

    @Override
    public ScalaVectorX<T> plus(T e) {
        return withVector(vector.appendBack(e));
    }

    @Override
    public ScalaVectorX<T> plusAll(Collection<? extends T> list) {
        Vector<T> vec = vector;
        if(list instanceof ScalaVectorX){
            final CanBuildFrom<Vector<?>, T, Vector<T>> builder =
                    Vector.<T>canBuildFrom();
            final CanBuildFrom<Vector<T>, T, Vector<T>> builder2 = (CanBuildFrom)builder;
            Vector<T> toUse = ((ScalaVectorX)list).vector;
            vec = vec.$plus$plus(toUse, builder2);
        }
        else {
            for(T next :  list){
                vec = vec.appendBack(next);
            }
        }
        
        
        
        return withVector(vec);
     }
 

    @Override
    public ScalaVectorX<T> with(int i, T e) {
        if(i<0 || i>size())
            throw new IndexOutOfBoundsException("Index " + i + " is out of bounds - size : " + size());
        return withVector(vector.updateAt(i,e));
    }

    @Override
    public ScalaVectorX<T> plus(int i, T e) {
        if(i<0 || i>size())
            throw new IndexOutOfBoundsException("Index " + i + " is out of bounds - size : " + size());
        if(i==0)
            return withVector(vector.appendFront(e));
        if(i==size())
            return withVector(vector.appendBack(e));
        val frontBack = vector.splitAt(i);
        final CanBuildFrom<Vector<?>, T, Vector<T>> builder =
                Vector.<T>canBuildFrom();
        final CanBuildFrom<Vector<T>, T, Vector<T>> builder2 = (CanBuildFrom)builder;
         val front = frontBack._1.appendBack(e);
        Vector<T> result = (Vector<T>) front.$plus$plus(frontBack._2,builder2);
             
        return withVector(result);
       
    }

    @Override
    public ScalaVectorX<T> plusAll(int i, Collection<? extends T> list) {

        val frontBack = vector.splitAt(i);
        final CanBuildFrom<Vector<?>, T, Vector<T>> builder =
                Vector.<T>canBuildFrom();
        final CanBuildFrom<Vector<T>, T, Vector<T>> builder2 = (CanBuildFrom)builder;
        
        Vector<T> front = frontBack._1;
        if(list instanceof ScalaVectorX){
            Vector<T> toUse = ((ScalaVectorX)list).vector;
            front = front.$plus$plus(toUse, builder2);
        }
        else {
            for(T next : list){
                front = front.appendBack(next);
            }
        }
        
        Vector<T> result = (Vector<T>) front.$plus$plus(frontBack._2,builder2);
             
        return withVector(result);
    }

    @Override
    public PVector<T> minus(Object e) {
        return fromPVector(this,toPVector()).filter(i->!Objects.equals(i,e));
    }

    @Override
    public PVector<T> minusAll(Collection<?> list) {
        return (PVector<T>)fromPVector(this,toPVector()).removeAllI((Iterable<T>)list);
    }
    
    public ScalaVectorX<T> tail(){
        return withVector(vector.tail());
    }
    public T head(){
        return vector.head();
    }

    @Override
    public ScalaVectorX<T> minus(int i) {
        
        if(i<0 || i>size())
            throw new IndexOutOfBoundsException("Index " + i + " is out of bounds - size : " + size());
        if(i==0)
            return withVector(vector.drop(1));
        if(i==size()-1)
            return withVector(vector.dropRight(1));
        val frontBack = vector.splitAt(i);
        final CanBuildFrom<Vector<?>, T, Vector<T>> builder =
                Vector.<T>canBuildFrom();
        final CanBuildFrom<Vector<T>, T, Vector<T>> builder2 = (CanBuildFrom)builder;
         val front = frontBack._1;
         
        Vector<T> result = (Vector<T>) front.$plus$plus(frontBack._2.drop(1),builder2);
             
        return withVector(result);
    }

    @Override
    public ScalaVectorX<T> subList(int start, int end) {
        
        return withVector(vector.drop(start).take(end-start));
    }

    @Override
    public T get(int index) {
        return vector.apply(index);
    }

    @Override
    public int size() {
        return vector.size();
    }

    @Override
    public GenTraversableOnce<T> traversable() {
        return vector;
    }

    @Override
    public CanBuildFrom canBuildFrom() {
        return Vector.canBuildFrom();
    }

    public static <T> VectorX<T> copyFromCollection(CollectionX<T> vec) {

        return ScalaVectorX.<T>empty()
                .plusAll(vec);

    }
}
