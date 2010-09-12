package Rakudo.Metamodel.Representations;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Metamodel.Hints;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// A representation that we use (for now) for native captures.
/// </summary>
public final class P6capture implements Representation
{
    /// <summary>
    /// This is how a Capture looks.
    /// </summary>
    public final class Instance implements RakudoObject
    {
        public RakudoObject[] Positionals;
        public HashMap<String, RakudoObject> Nameds;
        public SharedTable getSTable() {
            return new SharedTable(); // TODO
        }
        public void setSTable(SharedTable st) {
        }
        public SerializationContext getSC() {
            return new SerializationContext(); // TODO
        }
        public void setSC(SerializationContext sc) {
        }
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
    public RakudoObject type_object_for(RakudoObject MetaPackage)
    {
        SharedTable STable = new SharedTable();
        STable.HOW = MetaPackage;
        STable.REPR = this;
        STable.WHAT = new Instance(STable);
        return STable.WHAT;
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
    public boolean defined(RakudoObject O)
    {
        Instance Obj = (Instance)O;
        return Obj.Positionals != null || Obj.Nameds != null;
    }

    public RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException("Native captures cannot store additional attributes.");
    }

    public RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException("Native captures cannot store additional attributes.");
    }

    public void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException("Native captures cannot store additional attributes.");
    }

    public void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException("Native captures cannot store additional attributes.");
    }

    public int hint_for(RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
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

