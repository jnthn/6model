package Rakudo.Metamodel.Representations;

import java.util.HashMap;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Serialization.SerializationContext;

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
    private final class Instance implements RakudoObject
    {
        // RakudoObject required implementation
        private SharedTable _SharedTable;
        private SerializationContext _SC;
        public SharedTable getSTable() {return _SharedTable;}
        public void setSTable( SharedTable st ){ _SharedTable = st;}
        public SerializationContext getSC(){return _SC;}
        public void setSC( SerializationContext sc ){ _SC = sc;}

        public RakudoObject[] SlotStorage;
        public HashMap<RakudoObject, HashMap<String, RakudoObject>> SpillStorage;
        public Instance(SharedTable sTable)
        {
            this.setSTable(sTable);
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
        SharedTable sTable = new SharedTable();
        sTable.HOW = MetaPackage;
        sTable.REPR = new P6opaque();
        sTable.WHAT = new Instance(sTable);
        return sTable.WHAT;
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
        Instance obj = new Instance(what.getSTable());
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
        // XXX TODO - no exception here because the interface does not declare one
        System.err.println("get_attribute not yet implemented");
        System.exit(1);
        return null;
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
        if (Hint < I.SlotStorage.length)
        {
            return I.SlotStorage[Hint];
        }
        else
        {
            if (I.SpillStorage != null && I.SpillStorage.containsKey(ClassHandle))
            {
                HashMap<String,RakudoObject> ClassStore = I.SpillStorage.get(ClassHandle);
                if (ClassStore.containsKey(Name))
                    return ClassStore.get(Name);
            }
            return null;
        }
    }

    /// <summary>
    /// 
    /// </summary>
    /// <param name="Object"></par    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Value"></param>
    public void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
        // throws NoSuchMethodException
//  public override void bind_attribute(RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        // XXX TODO
        // throw new NoSuchMethodException();
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
    {
        Instance I = (Instance)Object;
        if (Hint < I.SlotStorage.length)
        {
            I.SlotStorage[Hint] = Value;
        }
        else
        {
            if (I.SpillStorage == null)
                I.SpillStorage = new HashMap<RakudoObject, HashMap<String, RakudoObject>>();
            if (!I.SpillStorage.containsKey(ClassHandle))
                I.SpillStorage.put(ClassHandle, new HashMap<String, RakudoObject>());
            HashMap<String, RakudoObject> ClassStore = I.SpillStorage.get(ClassHandle);
//          if (ClassStore.ContainsKey(Name)) // redundant on a HashMap
//              ClassStore.put(Name, Value);
//          else
                ClassStore.put(Name, Value);
        }
    }

    /// <summary>
    /// Checks if we have a hint for the given class and name, and if
    /// so returns it.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public int hint_for(RakudoObject ClassHandle, String Name)
//  public override int hint_for(RakudoObject ClassHandle, String Name)
    {
        if (SlotAllocation.containsKey(ClassHandle) && SlotAllocation.get(ClassHandle).containsKey(Name))
            return SlotAllocation.get(ClassHandle).get(Name);
            // return SlotAllocation[ClassHandle][Name];
        else
            return Hints.NO_HINT;
    }

    public void set_int(RakudoObject Object, int Value)
//  public override void set_int(RakudoObject Object, int Value)
    {
        System.err.println("This type of representation cannot box a native int");
        System.exit(1);
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

