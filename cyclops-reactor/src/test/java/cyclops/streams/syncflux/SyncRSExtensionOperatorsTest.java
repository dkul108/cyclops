package cyclops.streams.syncflux;


import cyclops.collections.mutable.ListX;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.companion.reactor.Fluxs;
import cyclops.control.Maybe;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.stream.Streamable;
import org.hamcrest.Matchers;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SyncRSExtensionOperatorsTest {
	protected <U> ReactiveSeq<U> of(U... array){

		return Fluxs.reactiveSeq(Flux.just(array));
	}
	@Test
	public void flatMapStreamFilterSimple(){
		assertThat(of(1).flatMap(i->of(i).filter(Objects::nonNull))
						.collect(Collectors.toList()),
				Matchers.equalTo(Arrays.asList(1)));
	}
    @Test
    public void combine(){
        assertThat(of(1,1,2,3)
                   .combine((a, b)->a.equals(b), Semigroups.intSum)
                   .toListX(),equalTo(ListX.of(4,3)));
                   
    }
	@Test
	public void subStream(){
		List<Integer> list = of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(of(of(1, 2, 3),
        		of(1, 3, 2), of(2, 1, 3), of(2, 3, 1), of(3, 1, 2), of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(of()
							.onEmptySwitch(()->of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(of(4,5,6)
							.onEmptySwitch(()->of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(of(1,2,3,4,5).elapsed().noneMatch(t->t.v2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t.v2 <= System.currentTimeMillis()));
		

	}
	@Test
	public void elementAt0(){
		assertThat(of(1).elementAt(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(of(1,2,3,4,5).elementAt(2).v1,equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(of(1,2,3,4,5).elementAt(2).v2.toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		of(1).elementAt(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		of().elementAt(0);
	}
	@Test
	public void get0(){
		assertTrue(of(1).get(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(of(1,2,3,4,5).get(2).get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(of(1).get(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(of().get(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(of(1).singleUnsafe(),equalTo(1));
	}
	@Test(expected=NoSuchElementException.class)
	public void singleEmpty(){
		of().singleUnsafe();
	}
	@Test(expected=NoSuchElementException.class)
	public void single2(){
		of(1,2).singleUnsafe();
	}
	@Test
	public void singleOptionalTest(){
		assertThat(of(1).single().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(of(1,2).single().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = Spouts.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			
		}
		return i;
	}
	@Test
	public void testSkipLast(){
		assertThat(of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(of()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLast1(){
		assertThat(of(1,2,3,4,5)
				.limitLast(1)
				.collect(Collectors.toList()),equalTo(Arrays.asList(5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(of()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(Spouts.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(Spouts.<Integer>of()
				.endsWith(Stream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).stream().toList();
		
		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void streamable(){
		Streamable<Integer> repeat = of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();
		
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	
	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.lazyStreamableSynchronized();
		
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	@Test
	public void splitBy(){
		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v1.toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollectionSynchronized();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c-> AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c-> AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = Streams.stream(it);
		tail.forEach(System.out::println);
	}
	@Test
	public void testOfType() {

		

		assertThat(of(1, "a", 2, "b", 3, "c").ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(of(1, "a", 2, "b", 3,"c").ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b","c")));

		assertThat(of(1, "a", 2, "b", 3, "c")

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3,"c"));

	}

	@Test
	public void testCastPast() {
		of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	
	@Test
	public void flatMapCompletableFuture(){
		assertThat(of(1,2,3).flatMapAnyM(i-> AnyM.fromArray(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapMaybe(){
		assertThat(of(1,2,3).flatMapI(Maybe::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {
		
		assertThat(of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		of(1,2,3).cast(String.class).collect(Collectors.toList());
	}
	@Test
	public void xMatch(){
		assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
	
}
