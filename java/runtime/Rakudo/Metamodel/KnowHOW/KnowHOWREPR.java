package Rakudo.Metamodel.KnowHOW;

import java.util.*;
import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.IRakudoObject;
import Rakudo.Metamodel.IRepresentation;
import Rakudo.Metamodel.SharedTable;
// One could be lazy and import Rakudo.Metamodel.*, but that would be
// less informative.
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// We have a REPR especially for the KnowHOW, which is part of the
/// "bootstrap".
/// </summary>
public class KnowHOWREPR implements IRepresentation
{
    /// <summary>
    /// This represents an instance created with this underlying
    /// representation. We use .Net data types for out attribute
    /// and method store.
    /// </summary>
    public class KnowHOWInstance implements IRakudoObject
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

        public ArrayList<IRakudoObject> Attributes;
        // public List<IRakudoObject> Attributes; // the C# version
        public HashMap<String, IRakudoObject> Methods;
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
    public IRakudoObject type_object_for(IRakudoObject HOW)
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
    public IRakudoObject instance_of(IRakudoObject WHAT)
    {
        KnowHOWInstance Object = new KnowHOWInstance(WHAT.getSTable());
        // var Object = new KnowHOWInstance(WHAT.STable);
        Object.Methods = new HashMap<String, IRakudoObject>();
        Object.Attributes = new ArrayList<IRakudoObject>();
        return Object;
    }

    /// <summary>
    /// Checks if the object is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(IRakudoObject Obj)
    {
        return ((KnowHOWInstance)Obj).Methods != null;
    }

    public IRakudoObject get_attribute(IRakudoObject Object, IRakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException();
    }

    public IRakudoObject get_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute(IRakudoObject Object, IRakudoObject ClassHandle, String Name, IRakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    public void bind_attribute_with_hint(IRakudoObject Object, IRakudoObject ClassHandle, String Name, int Hint, IRakudoObject Value)
    {
        throw new UnsupportedOperationException();
    }

    /// <summary>
    /// We have attribute access hints for within the KnowHOW REPR, which
    /// we just manually map to the indexes.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public int hint_for(IRakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }
}

