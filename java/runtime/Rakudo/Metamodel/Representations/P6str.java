package Rakudo.Metamodel.Representations;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;

/// <summary>
/// A representation that we use (for now) for dealing with
/// Strings.
/// </summary>
public final class P6str implements Representation
{
    /// <summary>
    /// This is how the boxed form of a P6str looks.
    /// </summary>
    public final class Instance extends RakudoObject // internal in the C# version
    {
        public String Value;
        public Instance(SharedTable sharedTable)
        {
            this.setSTable(sharedTable);
        }
    }

    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="MetaPackage"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject metaPackage)
    {
        SharedTable sharedTable = new SharedTable();
        sharedTable.HOW = metaPackage;
        sharedTable.REPR = this;
        sharedTable.WHAT = new Instance(sharedTable);
        return sharedTable.WHAT;
    }

    /// <summary>
    /// Creates an instance of the type with the given type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        Instance object = new Instance(WHAT.getSTable());
        object.Value = "";
        return object;
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject Obj)
    {
        return ((Instance)Obj).Value != null;
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
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
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
        ((Instance)Object).Value = Value;
    }

    public String get_str(ThreadContext tc, RakudoObject Object)
    {
        return ((Instance)Object).Value;
    }
}

