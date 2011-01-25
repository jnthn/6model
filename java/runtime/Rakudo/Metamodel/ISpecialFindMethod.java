package Rakudo.Metamodel;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Runtime.ThreadContext;

public interface ISpecialFindMethod {
    public RakudoObject SpecialFindMethod(
        ThreadContext tc, RakudoObject ro, String s, int i);
}
