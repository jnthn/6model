package Rakudo.Metamodel;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Runtime.ThreadContext;

public interface IFindMethod {
    public RakudoObject FindMethod(
        ThreadContext tc, RakudoObject ro, String s, int i);
}
