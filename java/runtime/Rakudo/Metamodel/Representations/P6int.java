package Rakudo.Metamodel.Representations;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// A representation that we use for dealing with ints in their
/// boxed form.
/// </summary>
public final class P6int implements Representation
{
    /// <summary>
    /// This is how the boxed form of a P6int looks like.
    /// </summary>
    public final class Instance extends RakudoObject
    {
        public int Value;
        public boolean Undefined;
        public Instance(SharedTable STable)
        {
            this.setSTable(STable);
        }
    }

    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="MetaPackage"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject MetaPackage)
    {
        SharedTable STable = new SharedTable();
        STable.HOW = MetaPackage;
        STable.REPR = (Representation)this;
        Instance WHAT = new Instance(STable);
        WHAT.Undefined = true;
        STable.WHAT = (RakudoObject)WHAT;
        return WHAT;
    }

    /// <summary>
    /// Creates an instance of the type with the given type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        return new Instance(WHAT.getSTable());
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject Obj)
    {
        return !((Instance)Obj).Undefined;
    }

    public RakudoObject get_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public void bind_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public int hint_for(ThreadContext tc, RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(ThreadContext tc, RakudoObject Object, int Value)
    {
        ((Instance)Object).Value = Value;
    }

    public int get_int(ThreadContext tc, RakudoObject Object)
    {
        return ((Instance)Object).Value;
    }

    public void set_num(ThreadContext tc, RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(ThreadContext tc, RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}

