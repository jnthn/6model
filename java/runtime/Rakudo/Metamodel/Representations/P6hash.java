package Rakudo.Metamodel.Representations;

import java.util.HashMap;
import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// A representation that uses a hash of hash representation to store
/// the attributes. No real restriction on the sort of class it can be
/// used with. Also dead easy to implement. :-)
/// </summary>
public final class P6hash implements Representation
{
    /// <summary>
    /// This class represents our instances. It's inner workings are
    /// entirely private to this representation, which is the only
    /// thing that knows how it looks on the inside.
    /// XXX Once we do this production level, we really need to
    /// consider concurrency in accesses to the Dictionary. But
    /// this is OK for a prototype. -- jnthn
    /// </summary>
    private final class Instance extends RakudoObject
    {
        public HashMap<RakudoObject, HashMap<String, RakudoObject>> Storage;
        public Instance(SharedTable sTable)
        {
            this.setSTable(sTable);
        }
    }

    /// <summary>
    /// Creates a type object that references the given HOW and
    /// this REPR; note we just use the singleton instance for
    /// all of them, since the REPR stores nothing distinct.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject metaPackage)
    {
        SharedTable sTable = new SharedTable();
        sTable.HOW = metaPackage;
        sTable.REPR = this;
        sTable.WHAT = new Instance(sTable);
        return sTable.WHAT;
    }

    /// <summary>
    /// Allocates and returns a new object based upon the type object
    /// supplied.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        Instance Object = new Instance(WHAT.getSTable());
        Object.Storage = new HashMap<RakudoObject, HashMap<String, RakudoObject>>();
        return Object;
    }

    /// <summary>
    /// Checks if the object is defined, which boils down to "is
    /// this a type object", which in trun means "did we allocate
    /// any storage".
    /// </summary>
    /// <param name="Object"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject Object)
    {
        return ((Instance)Object).Storage != null;
    }

    /// <summary>
    /// Gets the attribute with the given value.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject get_attribute(ThreadContext tc, RakudoObject Object, RakudoObject classHandle, String name)
    {
        // If no storage ever allocated, trivially no value. Otherwise,
        // return what we find.
        Instance I = (Instance)Object;
        if (I.Storage == null || !I.Storage.containsKey(classHandle))
            return null;
        HashMap<String, RakudoObject> classStore = I.Storage.get(classHandle);
        return classStore.containsKey(name) ? classStore.get(name) : null;
    }

    /// <summary>
    /// This representation doesn't use hints, so this just delegates
    /// straight off to the hint-less version.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        return get_attribute(tc, Object, ClassHandle, Name);
    }

    /// <summary>
    /// Binds an attribute to the given value.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Value"></param>
    public void bind_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        // If no storage at all, allocate some.
        Instance I = (Instance)Object;
        if (I.Storage == null)
            I.Storage = new HashMap<RakudoObject, HashMap<String, RakudoObject>>();
        if (!I.Storage.containsKey(ClassHandle))
            I.Storage.put(ClassHandle, new HashMap<String, RakudoObject>());
        
        // Now stick in the name slot for the class storage, creating if it
        // needed.
        HashMap<String, RakudoObject> ClassStore = I.Storage.get(ClassHandle);
//      if (ClassStore.ContainsKey(Name)) // redundant on HashMap
//          ClassStore[Name] = Value;
//      else
            ClassStore.put(Name, Value);
    }

    /// <summary>
    /// This representation doesn't do hints, so this delegates straight
    /// off to the hint-less version.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <param name="Value"></param>
    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        bind_attribute(tc, Object, ClassHandle, Name, Value);
    }

    /// <summary>
    /// No hints for P6Hash.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
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
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}

