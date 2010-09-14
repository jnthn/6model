package Rakudo.Metamodel.Representations;

import java.util.HashMap;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.CaptureHelper;
import Rakudo.Runtime.Ops;
import Rakudo.Runtime.ThreadContext;
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
    protected HashMap<RakudoObject, HashMap<String, Integer>> SlotAllocation = new HashMap<RakudoObject, HashMap<String, Integer>>();
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
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject MetaPackage)
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
    public RakudoObject instance_of(ThreadContext tc, RakudoObject what)
    {
        if (SlotAllocation == null)
            ComputeSlotAllocation(null, what);
        Instance object = new Instance(what.getSTable());
        object.SlotStorage = new RakudoObject[Slots];
        return object;
    }

    /// <summary>
    /// Checks if the object is defined, which boils down to "is
    /// this a type object", which in trun means "did we allocate
    /// any storage".
    /// </summary>
    /// <param name="Object"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject object)
    {
        return ((Instance)object).SlotStorage != null;
    }

    /// <summary>
    /// Gets an attribute.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject get_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name)
    {
        Instance I = (Instance)object;

        // Try the slot allocation first.
        if (SlotAllocation != null && SlotAllocation.containsKey(classHandle)) {
// TODO if (SlotAllocation != null && SlotAllocation.TryGetValue(classHandle, out classAllocation))
            HashMap<String, Integer> classAllocation;
            classAllocation = SlotAllocation.get(classHandle);
            if (classAllocation.containsKey(name)) {
                int position;
                position = classAllocation.get(name);
// TODO     if (classAllocation.TryGetValue(name, out position))
                return I.SlotStorage[position];
            }
        }
        // Fall back to the spill storage.
        if (I.SpillStorage != null && I.SpillStorage.containsKey(classHandle))
        {
            HashMap<String,RakudoObject> classStore = I.SpillStorage.get(classHandle);
            if (classStore.containsKey(name))
                return classStore.get(name);
        }

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
    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, int hint)
    {
        Instance I = (Instance)object;
        if (hint < I.SlotStorage.length)
        {
            return I.SlotStorage[hint];
        }
        else
        {
            if (I.SpillStorage != null && I.SpillStorage.containsKey(classHandle))
            {
                HashMap<String,RakudoObject> classStore = I.SpillStorage.get(classHandle);
                if (classStore.containsKey(name))
                    return classStore.get(name);
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
    public void bind_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, RakudoObject value)
    {
        Instance I = (Instance)object;

        // Try the slot allocation first.
        if (SlotAllocation != null && SlotAllocation.containsKey(classHandle)) {
// TODO if (SlotAllocation != null && SlotAllocation.TryGetValue(classHandle, out classAllocation))
            HashMap<String, Integer> classAllocation = SlotAllocation.get(classHandle);
            if (classAllocation.containsKey(name))
// TODO     if (classAllocation.TryGetValue(name, out position))
            {
                int position;
                position = classAllocation.get(name);
                I.SlotStorage[position] = value;
                return;
            }
        }

        // Fall back to the spill storage.
        if (I.SpillStorage == null)
            I.SpillStorage = new HashMap<RakudoObject, HashMap<String, RakudoObject>>();
        if (!I.SpillStorage.containsKey(classHandle))
            I.SpillStorage.put(classHandle, new HashMap<String, RakudoObject>());
        HashMap<String,RakudoObject> classStore = I.SpillStorage.get(classHandle);
//      if (classStore.ContainsKey(Name)) // redundant on a HashMap
//          classStore[Name] = Value;
//      else
            classStore.put(name, value);
    }

    /// <summary>
    /// Bind the attribute, using the hint if possible.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <param name="Value"></param>
    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, int hint, RakudoObject value)
    {
        Instance I = (Instance)object;
        if (hint < I.SlotStorage.length)
        {
            I.SlotStorage[hint] = value;
        }
        else if ((hint = hint_for(tc, classHandle, name)) != Hints.NO_HINT && hint < I.SlotStorage.length)
        {
            I.SlotStorage[hint] = value;
        }
        else
        {
            if (I.SpillStorage == null)
                I.SpillStorage = new HashMap<RakudoObject, HashMap<String, RakudoObject>>();
            if (!I.SpillStorage.containsKey(classHandle))
                I.SpillStorage.put(classHandle, new HashMap<String, RakudoObject>());
            HashMap<String, RakudoObject> classStore = I.SpillStorage.get(classHandle);
//          if (ClassStore.ContainsKey(Name)) // redundant on a HashMap
//              ClassStore.put(Name, Value);
//          else
                classStore.put(name, value);
        }
    }

    /// <summary>
    /// Checks if we have a hint for the given class and name, and if
    /// so returns it.
    /// </summary>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public int hint_for(ThreadContext tc, RakudoObject ClassHandle, String Name)
    {
        if (SlotAllocation.containsKey(ClassHandle) && SlotAllocation.get(ClassHandle).containsKey(Name))
            return SlotAllocation.get(ClassHandle).get(Name); // C# SlotAllocation[ClassHandle][Name];
        else
            return Hints.NO_HINT;
    }

    public void set_int(ThreadContext tc, RakudoObject Object, int Value)
    {
        System.err.println("This type of representation cannot box a native int");
        System.exit(1);
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
        throw new UnsupportedOperationException("This type of representation cannot box a native String");
    }

    public          String get_str(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native String");
    }
    private void ComputeSlotAllocation(ThreadContext TC, RakudoObject WHAT)
    {
        RakudoObject HOW = WHAT.getSTable().HOW;

        // Allocate slot mapping table.
        SlotAllocation = new HashMap<RakudoObject, HashMap<String, Integer>>();

        // Walk through the parents list.
        RakudoObject currentClass = WHAT;
        int CurrentSlot = 0;
        while (currentClass != null)
        {
            // Get attributes and iterate over them.
            RakudoObject AttributesMeth = HOW.getSTable().FindMethod.FindMethod(TC, HOW, "attributes", Hints.NO_HINT);
            HashMap<String, RakudoObject> localBoxInt1 = new HashMap<String, RakudoObject>();
            localBoxInt1.put("local", Ops.box_int(TC, 1, TC.DefaultBoolBoxType));
            RakudoObject Attributes = AttributesMeth.getSTable().Invoke.Invoke(TC, AttributesMeth, CaptureHelper.FormWith(
                new RakudoObject[] { HOW, WHAT },
//              new HashMap<String, RakudoObject>() { { "local", Ops.box_int(TC, 1, TC.DefaultBoolBoxType) } }));
                localBoxInt1));
            RakudoObject AttributesElemsMeth = Attributes.getSTable().FindMethod.FindMethod(TC, Attributes, "elems", Hints.NO_HINT);
            int AttributesElems = Ops.unbox_int(TC, AttributesElemsMeth.getSTable().Invoke.Invoke(TC, AttributesElemsMeth,
                CaptureHelper.FormWith(new RakudoObject[] { Attributes })));
            RakudoObject AttrAtPosMeth = Attributes.getSTable().FindMethod.FindMethod(TC, Attributes, "at_pos", Hints.NO_HINT);
            for (int i = 0; i < AttributesElems; i++)
            {
                // Get the attribute, then get its name.
                RakudoObject attr = AttrAtPosMeth.getSTable().Invoke.Invoke(TC, AttrAtPosMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { Attributes, Ops.box_int(TC, i, TC.DefaultIntBoxType) }));
                RakudoObject nameMeth = attr.getSTable().FindMethod.FindMethod(TC, attr, "name", Hints.NO_HINT);
                String Name = Ops.unbox_str(TC, attr.getSTable().Invoke.Invoke(TC, nameMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { attr })));

                // Allocate a slot.
                if (!SlotAllocation.containsKey(currentClass))
                    SlotAllocation.put(currentClass, new HashMap<String, Integer>());
                SlotAllocation.get(currentClass).put(Name, CurrentSlot);
                CurrentSlot++;
            }

            // Find the next parent(s).
            RakudoObject ParentsMeth = HOW.getSTable().FindMethod.FindMethod(TC, HOW, "parents", Hints.NO_HINT);
            HashMap<String,RakudoObject> localBoxInt2 = new HashMap<String,RakudoObject>();
            localBoxInt2.put("local", Ops.box_int(TC, 1, TC.DefaultBoolBoxType));
            RakudoObject Parents = ParentsMeth.getSTable().Invoke.Invoke(TC, ParentsMeth, CaptureHelper.FormWith(
                new RakudoObject[] { HOW, WHAT },
//              new HashMap<String,RakudoObject>() { { "local", Ops.box_int(TC, 1, TC.DefaultBoolBoxType) } }));
                localBoxInt2));

            // Check how many parents we have.
            RakudoObject ParentElemsMeth = Parents.getSTable().FindMethod.FindMethod(TC, Parents, "elems", Hints.NO_HINT);
            int ParentElems = Ops.unbox_int(TC, ParentElemsMeth.getSTable().Invoke.Invoke(TC, ParentElemsMeth,
                CaptureHelper.FormWith(new RakudoObject[] { Parents })));
            if (ParentElems == 0)
            {
                // We're done. \o/
                break;
            }
            else if (ParentElems > 1)
            {
                // Multiple inheritance, so we can't compute this hierarchy.
                SlotAllocation = new HashMap<RakudoObject, HashMap<String, Integer>>();
                return;
            }
            else
            {
                // Just one. Get next parent.
                RakudoObject AtPosMeth = Parents.getSTable().FindMethod.FindMethod(TC, Parents, "at_pos", Hints.NO_HINT);
                currentClass = AtPosMeth.getSTable().Invoke.Invoke(TC, AtPosMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { Parents, Ops.box_int(TC, 0, TC.DefaultIntBoxType) }));
            }
        }
    }
}

