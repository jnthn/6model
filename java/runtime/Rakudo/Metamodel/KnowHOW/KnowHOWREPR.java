package Rakudo.Metamodel.KnowHOW;
import java.util.ArrayList;
import java.util.HashMap;
import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;
/// <summary>
/// We have a REPR especially for the KnowHOW, which is part of the "bootstrap".
/// </summary>
public final class KnowHOWREPR implements Representation
{
    /// <summary>
    /// This represents an instance created with this underlying
    /// representation. We use .Net data types for out attribute
    /// and method store.
    /// </summary>
    public class KnowHOWInstance extends RakudoObject // C# has internal
    {
        public ArrayList<RakudoObject> Attributes;
        public HashMap<String, RakudoObject> Methods;
        public RakudoObject Name;
        public KnowHOWInstance(SharedTable sharedTable)
        {
            this.setSTable(sharedTable);
        }
    }

    /// <summary>
    /// Gets a type object pointing to the given HOW.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject HOW)
    {
        SharedTable STable = new SharedTable();
        STable.HOW = HOW;
        STable.REPR = this;
        STable.WHAT = new KnowHOWInstance(STable);
        return STable.WHAT;
    }

    /// <summary>
    /// Create an instance of the given object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        KnowHOWInstance Object = new KnowHOWInstance(WHAT.getSTable());
        Object.Methods = new HashMap<String, RakudoObject>();
        Object.Attributes = new ArrayList<RakudoObject>();
        return Object;
    }

    /// <summary>
    /// Checks if the object is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns>boolean</returns>
    public boolean defined(ThreadContext tc, RakudoObject obj)
    {
        return ((KnowHOWInstance)obj).Methods != null;
    }

    public RakudoObject get_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String Name)
    {
        throw new UnsupportedOperationException();
    }

    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    /// <summary>
    /// We have attribute access hints for within the KnowHOW REPR, which
    /// we just manually map to the indexes.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns>int</returns>
    public int hint_for(ThreadContext tc, RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(ThreadContext TC, RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(ThreadContext TC, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
    }

    public void set_num(ThreadContext TC, RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(ThreadContext TC, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(ThreadContext TC, RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(ThreadContext TC, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}

