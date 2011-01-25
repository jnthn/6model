package Rakudo.Metamodel.Representations;
import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// This is a very first cut at a mapping representation. Just stores
/// hash mappings of one string to object at the moment. Not really
/// efficient, and no compact struct support, but gets us started.
/// </summary>
public class P6mapping implements Representation
{
    public class Instance extends RakudoObject
    {
        public HashMap<String,RakudoObject> Storage;
        public Instance(SharedTable sharedTable)
        {
            this.setSTable(sharedTable);
        }
    }

    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="HOW"></param>
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
    public  RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        Instance object = new Instance(WHAT.getSTable());
        object.Storage = new HashMap<String,RakudoObject>();
        return object;
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject object)
    {
        return ((Instance)object).Storage != null;
    }

    public RakudoObject get_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name)
    {
        throw new UnsupportedOperationException();
    }

    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, int hint)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, RakudoObject value)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, int hint, RakudoObject value)
    {
        throw new UnsupportedOperationException();
    }

    public int hint_for(ThreadContext tc, RakudoObject classHandle, String name)
    {
        throw new UnsupportedOperationException();
    }

    public void set_int(ThreadContext tc, RakudoObject object, int value)
    {
        throw new UnsupportedOperationException();
    }

    public int get_int(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException();
    }

    public void set_num(ThreadContext tc, RakudoObject object, double value)
    {
        throw new UnsupportedOperationException();
    }

    public double get_num(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException();
    }

    public void set_str(ThreadContext tc, RakudoObject object, String value)
    {
        throw new UnsupportedOperationException();
    }

    public String get_str(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException();
    }
}

