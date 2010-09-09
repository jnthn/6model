package Rakudo.Metamodel.Representations;

import java.util.HashMap;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;

/// <summary>
/// This is a first cut implementation of the P6opaque representation.
/// Eventually it's going to need to handle native types too, and we
/// want to get an instance down to a single object allocation if we
/// can. Alas, that's Quite Tricky to write, so for now we just do
/// something easy, but at least with the hints.
/// </summary>
public final class P6opaque implements Representation
//public sealed class P6opaque : Representation
{
    /// <summary>
    /// This stores a mapping of classes/names to slots in the event
    /// we need to do a lookup.
    /// </summary>
    protected HashMap<RakudoObject, HashMap<String, Integer>>
    SlotAllocation = new HashMap<RakudoObject, HashMap<String, Integer>>();
    protected int Slots = 0;

    /// <summary>
    /// This is what an instance of a P6opaque looks like. The SlotStorage
    /// is an array where we'll store most attributes, so we can look them
    /// up by index. There's also an unallocated spill-over store for any
    /// attributes that are added through augment, or in MI cases.
    /// </summary>
    private final  class Instance implements RakudoObject
//  private sealed class Instance : RakudoObject
    {
        public RakudoObject[] SlotStorage;
        public HashMap<RakudoObject, HashMap<String, RakudoObject>> SpillStorage;
        public Instance(SharedTable STable)
        {
            this.STable = STable;
        }
    }

    /// <summary>
    /// Creates a type object that references the given HOW and sets up
    /// the STable with a new REPR instance too.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public          RakudoObject type_object_for(RakudoObject MetaPackage)
//  public override RakudoObject type_object_for(RakudoObject MetaPackage)
    {
        SharedTable STable = new SharedTable();
        STable.HOW = MetaPackage;
        STable.REPR = new P6opaque();
        STable.WHAT = new Instance(STable);
        return STable.WHAT;
    }

    /// <summary>
    /// Allocates and returns a new object based upon the type object
    /// supplied.
    /// </summary>
    /// <param name="HOW"></param>
    /// <returns></returns>
    public          RakudoObject instance_of(RakudoObject what)
//  public override RakudoObject instance_of(RakudoObject WHAT)
    {
        Instance obj = new Instance(what.STable);
        obj.SlotStorage = new RakudoObject[Slots];
        return obj;
    }

    /// <summary>
    /// Checks if the object is defined, which boils down to "is
    /// this a type object", which in trun means "did we allocate
    /// any storage".
    /// </summary>
    /// <param name="Object"></param>
    /// <returns></returns>
    public          boolean defined(RakudoObject Object)
//  public override bool    defined(RakudoObject Object)
    {
        return ((Instance)Object).SlotStorage != null;
    }

    public          RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name)
//  public override RakudoObject get_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        // XXX
        throw new NoSuchMethodException("get_attribute not yet implemented");
    }

    /// <summary>
    /// Gets the attribute, using the hint if possible.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public          RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
//  public override RakudoObject get_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        Instance I = (Instance)Object;
        if (Hint < I.SlotStorage.Length)
        {
            return I.SlotStorage[Hint];
        }
        else
        {
            if (I.SpillStorage != null && I.SpillStorage.ContainsKey(ClassHandle))
            {
                var ClassStore = I.SpillStorage[ClassHandle];
                if (ClassStore.ContainsKey(Name))
                    return ClassStore[Name];
            }
            return null;
        }
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Value"></param>
    public          void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
//  public override void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        // XXX
        throw new NotImplementedException();
    }

    /// <summary>
    /// Bind the attribute, using the hint if possible.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <param name="Value"></param>
    public          void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
//  public override void bind_attribute_with_hint(RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        var I = (Instance)Object;
        if (Hint < I.SlotStorage.Length)
        {
            I.SlotStorage[Hint] = Value;
        }
        else
        {
            if (I.SpillStorage == null)
                I.SpillStorage = new HashMap<RakudoObject, HashMap<String, RakudoObject>>();
            if (!I.SpillStorage.ContainsKey(ClassHandle))
                I.SpillStorage.Add(ClassHandle, new HashMap<String, RakudoObject>());
            var ClassStore = I.SpillStorage[ClassHandle];
            if (ClassStore.ContainsKey(Name))
                ClassStore[Name] = Value;
            else
                ClassStore.Add(Name, Value);
        }
    }

    /// <summary>
    /// Checks if we have a hint for the given class and name, and if
    /// so returns it.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public          int hint_for(RakudoObject ClassHandle, String Name)
//  public override int hint_for(RakudoObject ClassHandle, String Name)
    {
        if (SlotAllocation.ContainsKey(ClassHandle) && SlotAllocation[ClassHandle].ContainsKey(Name))
            return SlotAllocation[ClassHandle][Name];
        else
            return Hints.NO_HINT;
    }

    public          void set_int(RakudoObject Object, int Value)
//  public override void set_int(RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public          int get_int(RakudoObject Object)
//  public override int get_int(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
    }

    public          void set_num(RakudoObject Object, double Value)
//  public override void set_num(RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public          double get_num(RakudoObject Object)
//  public override double get_num(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public          void set_str(RakudoObject Object, String Value)
//  public override void set_str(RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native String");
    }

    public          String get_str(RakudoObject Object)
//  public override String get_str(RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native String");
    }
}

