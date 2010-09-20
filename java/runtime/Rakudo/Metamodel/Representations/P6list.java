package Rakudo.Metamodel.Representations;

import java.util.ArrayList;
//import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// This is a very first cut at a list representation. Essentially,
/// it just knows how to store a list of objects at the moment. At
/// some point we need to define the way that it will handle compact
/// arrays.
/// </summary>
public class P6list implements Representation
{
    public class Instance extends RakudoObject
    {
        /// <summary>
        /// Just use a .Net List at the moment, but an array would
        /// be more efficient in the long run (though give us more
        /// stuff to implement ourselves).
        /// </summary>
        public ArrayList<RakudoObject> Storage;
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
        object.Storage = new ArrayList<RakudoObject>();
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

    public  RakudoObject get_attribute(ThreadContext TC, RakudoObject object, RakudoObject classHandle, String name)
    {
        throw new UnsupportedOperationException();
    }

    public  RakudoObject get_attribute_with_hint(ThreadContext TC, RakudoObject object, RakudoObject classHandle, String name, int hint)
    {
        throw new UnsupportedOperationException();
    }

    public  void bind_attribute(ThreadContext TC, RakudoObject object, RakudoObject classHandle, String name, RakudoObject value)
    {
        throw new UnsupportedOperationException();
    }

    public  void bind_attribute_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, int hint, RakudoObject value)
    {
        throw new UnsupportedOperationException();
    }

    public  int hint_for(ThreadContext tc, RakudoObject classHandle, String name)
    {
        throw new UnsupportedOperationException();
    }

    public  void set_int(ThreadContext tc, RakudoObject object, int value)
    {
        throw new UnsupportedOperationException();
    }

    public  int get_int(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException();
    }

    public  void set_num(ThreadContext tc, RakudoObject object, double value)
    {
        throw new UnsupportedOperationException();
    }

    public  double get_num(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException();
    }

    public  void set_str(ThreadContext tc, RakudoObject object, String value)
    {
        throw new UnsupportedOperationException();
    }

    public  String get_str(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException();
    }
}

