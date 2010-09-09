package Rakudo.Metamodel.KnowHOW;

import java.util.ArrayList;
import java.util.HashMap;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// We have a REPR especially for the KnowHOW, which is part of the
/// "bootstrap".
/// </summary>
public class KnowHOWREPR implements Representation
{
    /// <summary>
    /// This represents an instance created with this underlying
    /// representation. We use .Net data types for out attribute
    /// and method store.
    /// </summary>
    public class KnowHOWInstance implements RakudoObject
    // internal class KnowHOWInstance implements IRakudoObject // the C# version
    {
        // public SharedTable STable { get; set; } // the C# version
        private SharedTable STable;
        public SharedTable getSTable() { return STable; }
        public void setSTable(SharedTable st) { STable = st; }

        // public Serialization.SerializationContext SC { get; set; } # the C# version
        private SerializationContext SC;
        public SerializationContext getSC() { return SC; }
        public void setSC( SerializationContext sc ) { SC = sc; }

        public ArrayList<RakudoObject> Attributes;
        // public List<IRakudoObject> Attributes; // the C# version
        public HashMap<String, RakudoObject> Methods;
        // public Dictionary<string, IRakudoObject> Methods;
        public KnowHOWInstance(SharedTable STable)
        {
            this.STable = STable;
        }
    }

    /// <summary>
    /// Gets a type object pointing to the given HOW.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(RakudoObject HOW)
    {
        SharedTable STable = new SharedTable();
        // var STable = new SharedTable();
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
    public RakudoObject instance_of(RakudoObject WHAT)
    {
        KnowHOWInstance Object = new KnowHOWInstance(WHAT.getSTable());
        // var Object = new KnowHOWInstance(WHAT.STable);
        Object.Methods = new HashMap<String, RakudoObject>();
        Object.Attributes = new ArrayList<RakudoObject>();
        return Object;
    }

    /// <summary>
    /// Checks if the object is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns>boolean</returns>
    public boolean defined(RakudoObject obj)
    {
        return ((KnowHOWInstance)obj).Methods != null;
    }

    public RakudoObject get_attribute(RakudoObject object, RakudoObject classHandle, String Name)
    {
        throw new UnsupportedOperationException();
    }

    public RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
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
    public int hint_for(RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }
}

