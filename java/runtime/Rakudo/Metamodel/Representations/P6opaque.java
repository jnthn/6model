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
public final class P6opaque implements Representation // C# has sealed
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
    private final class Instance extends RakudoObject
    {
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
            ComputeSlotAllocation(tc, what);
        Instance object = new Instance(what.getSTable());
        object.SlotStorage = new RakudoObject[Slots];
        return object;
    }

    /// <summary>
    /// Checks if the object is defined, which boils down to "is
    /// this a type object", which in trun means "did we allocate
    /// any storage".
    /// </summary>
    /// <param name="object"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject object)
    {
        return ((Instance)object).SlotStorage != null;
    }

    /// <summary>
    /// Gets an attribute.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public RakudoObject get_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name)
    {
        Instance I = (Instance)object;

        // Try the slot allocation first.
        if (SlotAllocation != null && SlotAllocation.containsKey(classHandle)) {
            HashMap<String, Integer> classAllocation = SlotAllocation.get(classHandle);
            if (classAllocation.containsKey(name)) {
                return I.SlotStorage[classAllocation.get(name)];
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
    /// <param name="object"></param>
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
    /// <param name="object"></par    /// <param name="ClassHandle"></param>
    /// <param name="Name"></param>
    /// <param name="Value"></param>
    public void bind_attribute(ThreadContext tc, RakudoObject object, RakudoObject classHandle, String name, RakudoObject value)
    {
        Instance I = (Instance)object;

        // Try the slot allocation first.
        if (SlotAllocation != null && SlotAllocation.containsKey(classHandle)) {
            HashMap<String, Integer> classAllocation = SlotAllocation.get(classHandle);
            if (classAllocation.containsKey(name))
            {
                int position = classAllocation.get(name);
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
    /// <param name="object"></param>
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
    public int hint_for(ThreadContext tc, RakudoObject classHandle, String name)
    {
        if (SlotAllocation.containsKey(classHandle) && SlotAllocation.get(classHandle).containsKey(name))
            return SlotAllocation.get(classHandle).get(name);
        else
            return Hints.NO_HINT;
    }

    public void set_int(ThreadContext tc, RakudoObject object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
    }

    public void set_num(ThreadContext tc, RakudoObject object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(ThreadContext tc, RakudoObject object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native String");
    }

    public String get_str(ThreadContext tc, RakudoObject object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native String");
    }
    private void ComputeSlotAllocation(ThreadContext tc, RakudoObject WHAT)
    {
        // Allocate slot mapping table.
        SlotAllocation = new HashMap<RakudoObject, HashMap<String, Integer>>();

        // Walk through the parents list.
        RakudoObject currentClass = WHAT;
        int currentSlot = 0;
        while (currentClass != null)
        {
            // Get attributes and iterate over them.
            RakudoObject HOW = currentClass.getSTable().HOW;
            RakudoObject attributesMeth = HOW.getSTable().FindMethod.FindMethod(tc, HOW, "attributes", Hints.NO_HINT);
            HashMap<String, RakudoObject> localBoxInt1 = new HashMap<String, RakudoObject>();
            localBoxInt1.put("local", Ops.box_int(tc, 1, tc.DefaultBoolBoxType));
            RakudoObject attributes = attributesMeth.getSTable().Invoke.Invoke(tc, attributesMeth, CaptureHelper.FormWith(
                new RakudoObject[] { HOW, currentClass },
                localBoxInt1)); // new HashMap<String, RakudoObject>() { { "local", Ops.box_int(tc, 1, tc.DefaultBoolBoxType) } }));
            RakudoObject attributesElemsMeth = attributes.getSTable().FindMethod.FindMethod(tc, attributes, "elems", Hints.NO_HINT);
            int attributesElems = Ops.unbox_int(tc, attributesElemsMeth.getSTable().Invoke.Invoke(tc, attributesElemsMeth,
                CaptureHelper.FormWith(new RakudoObject[] { attributes })));
            RakudoObject attrAtPosMeth = attributes.getSTable().FindMethod.FindMethod(tc, attributes, "at_pos", Hints.NO_HINT);
            for (int i = 0; i < attributesElems; i++)
            {
                // Get the attribute, then get its name.
                RakudoObject attr = attrAtPosMeth.getSTable().Invoke.Invoke(tc, attrAtPosMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { attributes, Ops.box_int(tc, i, tc.DefaultIntBoxType) }));
                RakudoObject nameMeth = attr.getSTable().FindMethod.FindMethod(tc, attr, "name", Hints.NO_HINT);
                String name = Ops.unbox_str(tc, attr.getSTable().Invoke.Invoke(tc, nameMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { attr })));

                // Allocate a slot.
                if (!SlotAllocation.containsKey(currentClass))
                    SlotAllocation.put(currentClass, new HashMap<String, Integer>());
                SlotAllocation.get(currentClass).put(name, currentSlot);
                currentSlot++;
            }

            // Find the next parent(s).
            RakudoObject parentsMeth = HOW.getSTable().FindMethod.FindMethod(tc, HOW, "parents", Hints.NO_HINT);
            HashMap<String,RakudoObject> localBoxInt2 = new HashMap<String,RakudoObject>();
            localBoxInt2.put("local", Ops.box_int(tc, 1, tc.DefaultBoolBoxType));
            RakudoObject parents = parentsMeth.getSTable().Invoke.Invoke(tc, parentsMeth, CaptureHelper.FormWith(
                new RakudoObject[] { HOW, currentClass },
                localBoxInt2)); // new HashMap<String,RakudoObject>() { { "local", Ops.box_int(tc, 1, tc.DefaultBoolBoxType) } }));
            // Check how many parents we have.
            RakudoObject parentElemsMeth = parents.getSTable().FindMethod.FindMethod(tc, parents, "elems", Hints.NO_HINT);
            int parentElems = Ops.unbox_int(tc, parentElemsMeth.getSTable().Invoke.Invoke(tc, parentElemsMeth,
                CaptureHelper.FormWith(new RakudoObject[] { parents })));
            if (parentElems == 0)
            {
                // We're done. \o/
                Slots = currentSlot;
                break;
            }
            else if (parentElems > 1)
            {
                // Multiple inheritance, so we can't compute this hierarchy.
                SlotAllocation = new HashMap<RakudoObject, HashMap<String, Integer>>();
                return;
            }
            else
            {
                // Just one. Get next parent.
                RakudoObject atPosMeth = parents.getSTable().FindMethod.FindMethod(tc, parents, "at_pos", Hints.NO_HINT);
                currentClass = atPosMeth.getSTable().Invoke.Invoke(tc, atPosMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { parents, Ops.box_int(tc, 0, tc.DefaultIntBoxType) }));
            }
        }
    }
}

