package com.aol.cyclops.functionaljava.hkt.typeclesses.instances;

import static com.aol.cyclops.functionaljava.hkt.ListKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.companion.functionaljava.Lists;
import com.aol.cyclops.functionaljava.hkt.ListKind;
import cyclops.monads.FJWitness;
import cyclops.monads.FJWitness.list;
import org.junit.Test;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;

import fj.data.List;

public class ListsTest {

    @Test
    public void unit(){


        ListKind<String> list = Lists.Instances.unit()
                                     .unit("hello")
                                     .convert(ListKind::narrowK);
        
        assertThat(list,equalTo(List.list("hello")));
    }
    @Test
    public void functor(){
        
        ListKind<Integer> list = Lists.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Lists.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(ListKind::narrowK);
        
        assertThat(list,equalTo(List.list("hello".length())));
    }
    @Test
    public void apSimple(){
        Lists.Instances.zippingApplicative()
            .ap(widen(List.list(l1(this::multiplyByTwo))),widen(List.list(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        ListKind<Fn1<Integer,Integer>> listFn = Lists.Instances.unit().unit(Lambda.λ((Integer i) ->i*2)).convert(ListKind::narrowK);


        Lists.Instances.zippingApplicative().ap(listFn,
                Lists.Instances.functor().map((String v) ->v.length(),ListKind.widen(List.list("h"))))
                                .convert(ListKind::narrow);

        List<Integer> list = Lists.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Lists.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> Lists.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(ListKind::narrow);
        
        assertThat(list,equalTo(List.list("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       ListKind<Integer> list  = Lists.Instances.monad()
                                      .flatMap(i->widen(List.range(0,i)), widen(List.list(1,2,3)))
                                      .convert(ListKind::narrowK);
    }
    @Test
    public void monad(){
        
        ListKind<Integer> list = Lists.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Lists.Instances.monad().flatMap((String v) -> Lists.Instances.unit().unit(v.length()), h))
                                     .convert(ListKind::narrowK);
        
        assertThat(list,equalTo(List.list("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        ListKind<String> list = Lists.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Lists.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(ListKind::narrowK);
        
        assertThat(list,equalTo(List.list("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        ListKind<String> list = Lists.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Lists.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(ListKind::narrowK);
        
        assertThat(list,equalTo(List.list()));
    }
    
    @Test
    public void monadPlus(){
        ListKind<Integer> list = Lists.Instances.<Integer>monadPlus()
                                      .plus(ListKind.widen(List.list()), ListKind.widen(List.list(10)))
                                      .convert(ListKind::narrowK);
        assertThat(list,equalTo(List.list(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<ListKind<Integer>> m = Monoid.of(ListKind.widen(List.list()), (a, b)->a.isEmpty() ? b : a);
        ListKind<Integer> list = Lists.Instances.<Integer>monadPlusK(m)
                                      .plus(ListKind.widen(List.list(5)), ListKind.widen(List.list(10)))
                                      .convert(ListKind::narrowK);
        assertThat(list,equalTo(List.list(5)));
    }
    @Test
    public void  foldLeft(){
        int sum  = Lists.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, ListKind.widen(List.list(1,2,3,4)));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = Lists.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, ListKind.widen(List.list(1,2,3,4)));
        
        assertThat(sum,equalTo(10));
    }
    
    @Test
    public void traverse(){
       Maybe<Higher<list, Integer>> res = Lists.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), ListKind.list(1,2,3))
                                                         .convert(Maybe::narrowK);
            
       assertThat(res,equalTo(Maybe.just(List.list(6,4,2))));
    }
    
}
