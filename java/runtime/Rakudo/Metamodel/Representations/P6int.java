package Rakudo.Metamodel.Representations;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
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
    public final class Instance implements RakudoObject
    {
        // RakudoObject required implementation
        private SharedTable _SharedTable;
        private SerializationContext _SC;
        public SharedTable getSTable() {return _SharedTable;}
        public void setSTable( SharedTable st ){ _SharedTable = st;}
        public SerializationContext getSC(){return _SC;}
        public void setSC( SerializationContext sc ){ _SC = sc;}

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
    public RakudoObject type_object_for(RakudoObject MetaPackage)
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
    public RakudoObject instance_of(RakudoObject WHAT)
    {
        return new Instance(WHAT.getSTable());
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(RakudoObject Obj)
    {
        return !((Instance)Obj).Undefined;
    }

    public RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException("Boxed native types cannot store additional attributes.");
    }

    public int hint_for(RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(RakudoObject Object, int Value)
    {
        ((Instance)Object).Value = Value;
    }

    public int get_int(RakudoObject Object)
    {
        return ((Instance)Object).Value;
    }

    public void set_num(RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}

