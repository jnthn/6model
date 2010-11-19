using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Runtime;

namespace Rakudo.Metamodel.Representations
{
    /// <summary>
    /// This is a first cut implementation of the P6opaque representation.
    /// Eventually it's going to need to handle native types too, and we
    /// want to get an instance down to a single object allocation if we
    /// can. Alas, that's Quite Tricky to write, so for now we just do
    /// something easy, but at least with the hints.
    /// </summary>
    public sealed class P6opaque : Representation
    {
        /// <summary>
        /// This stores a mapping of classes/names to slots in the event
        /// we need to do a lookup.
        /// </summary>
        internal Dictionary<RakudoObject, Dictionary<string, int>> SlotAllocation;
        internal int Slots = 0;

        /// <summary>
        /// This is what an instance of a P6opaque looks like. The SlotStorage
        /// is an array where we'll store most attributes, so we can look them
        /// up by index. There's also an unallocated spill-over store for any
        /// attributes that are added through augment, or in MI cases.
        /// </summary>
        private sealed class Instance : RakudoObject
        {
            public RakudoObject[] SlotStorage;
            public Dictionary<RakudoObject, Dictionary<string, RakudoObject>> SpillStorage;
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
        public override RakudoObject type_object_for(ThreadContext TC, RakudoObject MetaPackage)
        {
            var STable = new SharedTable();
            STable.HOW = MetaPackage;
            STable.REPR = new P6opaque();
            STable.WHAT = new Instance(STable);
            return STable.WHAT;
        }

        /// <summary>
        /// Allocates and returns a new object based upon the type object
        /// supplied. Also, computes the slot allocation if we didn't do
        /// that yet.
        /// </summary>
        /// <param name="HOW"></param>
        /// <returns></returns>
        public override RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
        {
            if (SlotAllocation == null)
                ComputeSlotAllocation(TC, WHAT);
            var Object = new Instance(WHAT.STable);
            Object.SlotStorage = new RakudoObject[Slots];
            return Object;
        }

        /// <summary>
        /// Checks if the object is defined, which boils down to "is
        /// this a type object", which in trun means "did we allocate
        /// any storage".
        /// </summary>
        /// <param name="Object"></param>
        /// <returns></returns>
        public override bool defined(ThreadContext TC, RakudoObject Object)
        {
            return ((Instance)Object).SlotStorage != null;
        }

        /// <summary>
        /// Gets an attribute.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public override RakudoObject get_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name)
        {
            var I = (Instance)Object;

            // Try the slot allocation first.
            Dictionary<string, int> ClassAllocation;
            int Position;
            if (SlotAllocation != null && SlotAllocation.TryGetValue(ClassHandle, out ClassAllocation))
                if (ClassAllocation.TryGetValue(Name, out Position))
                    return I.SlotStorage[Position];

            // Fall back to the spill storage.
            if (I.SpillStorage != null && I.SpillStorage.ContainsKey(ClassHandle))
            {
                var ClassStore = I.SpillStorage[ClassHandle];
                if (ClassStore.ContainsKey(Name))
                    return ClassStore[Name];
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
        public override RakudoObject get_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint)
        {
            var I = (Instance)Object;
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
        public override void bind_attribute(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, RakudoObject Value)
        {
            var I = (Instance)Object;

            // Try the slot allocation first.
            Dictionary<string, int> ClassAllocation;
            int Position;
            if (SlotAllocation != null && SlotAllocation.TryGetValue(ClassHandle, out ClassAllocation))
                if (ClassAllocation.TryGetValue(Name, out Position))
                {
                    I.SlotStorage[Position] = Value;
                    return;
                }

            // Fall back to the spill storage.
            if (I.SpillStorage == null)
                I.SpillStorage = new Dictionary<RakudoObject, Dictionary<string, RakudoObject>>();
            if (!I.SpillStorage.ContainsKey(ClassHandle))
                I.SpillStorage.Add(ClassHandle, new Dictionary<string, RakudoObject>());
            var ClassStore = I.SpillStorage[ClassHandle];
            if (ClassStore.ContainsKey(Name))
                ClassStore[Name] = Value;
            else
                ClassStore.Add(Name, Value);
        }

        /// <summary>
        /// Bind the attribute, using the hint if possible.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="ClassHandle"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <param name="Value"></param>
        public override void bind_attribute_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject ClassHandle, string Name, int Hint, RakudoObject Value)
        {
            var I = (Instance)Object;
            if (Hint < I.SlotStorage.Length)
            {
                I.SlotStorage[Hint] = Value;
            }
            else if ((Hint = hint_for(TC, ClassHandle, Name)) != Hints.NO_HINT && Hint < I.SlotStorage.Length)
            {
                I.SlotStorage[Hint] = Value;
            }
            else
            {
                if (I.SpillStorage == null)
                    I.SpillStorage = new Dictionary<RakudoObject, Dictionary<string, RakudoObject>>();
                if (!I.SpillStorage.ContainsKey(ClassHandle))
                    I.SpillStorage.Add(ClassHandle, new Dictionary<string, RakudoObject>());
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
        public override int hint_for(ThreadContext TC, RakudoObject ClassHandle, string Name)
        {
            if (SlotAllocation.ContainsKey(ClassHandle) && SlotAllocation[ClassHandle].ContainsKey(Name))
                return SlotAllocation[ClassHandle][Name];
            else
                return Hints.NO_HINT;
        }

        public override void set_int(ThreadContext TC, RakudoObject Object, int Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native int");
        }

        public override int get_int(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native int");
        }

        public override void set_num(ThreadContext TC, RakudoObject Object, double Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native num");
        }

        public override double get_num(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native num");
        }

        public override void set_str(ThreadContext TC, RakudoObject Object, string Value)
        {
            throw new InvalidOperationException("This type of representation cannot box a native string");
        }

        public override string get_str(ThreadContext TC, RakudoObject Object)
        {
            throw new InvalidOperationException("This type of representation cannot unbox to a native string");
        }

        /// <summary>
        /// Computes the slot allocation for this representation.
        /// </summary>
        /// <param name="WHAT"></param>
        private void ComputeSlotAllocation(ThreadContext TC, RakudoObject WHAT)
        {
            // Allocate slot mapping table.
            SlotAllocation = new Dictionary<RakudoObject, Dictionary<string, int>>();

            // Walk through the parents list.
            var CurrentClass = WHAT;
            var CurrentSlot = 0;
            while (CurrentClass != null)
            {
                // Get attributes and iterate over them.
                var HOW = CurrentClass.STable.HOW;
                var AttributesMeth = HOW.STable.FindMethod(TC, HOW, "attributes", Hints.NO_HINT);
                var Attributes = AttributesMeth.STable.Invoke(TC, AttributesMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { HOW, CurrentClass },
                    new Dictionary<string, RakudoObject>() { { "local", Ops.box_int(TC, 1, TC.DefaultBoolBoxType) } }));
                var AttributesElemsMeth = Attributes.STable.FindMethod(TC, Attributes, "elems", Hints.NO_HINT);
                var AttributesElems = Ops.unbox_int(TC, AttributesElemsMeth.STable.Invoke(TC, AttributesElemsMeth,
                    CaptureHelper.FormWith(new RakudoObject[] { Attributes })));
                var AttrAtPosMeth = Attributes.STable.FindMethod(TC, Attributes, "at_pos", Hints.NO_HINT);
                for (int i = 0; i < AttributesElems; i++)
                {
                    // Get the attribute, then get its name.
                    var Attr = AttrAtPosMeth.STable.Invoke(TC, AttrAtPosMeth, CaptureHelper.FormWith(
                        new RakudoObject[] { Attributes, Ops.box_int(TC, i, Ops.get_lex(TC, "NQPInt")) }));
                    var NameMeth = Attr.STable.FindMethod(TC, Attr, "name", Hints.NO_HINT);
                    var Name = Ops.unbox_str(TC, NameMeth.STable.Invoke(TC, NameMeth, CaptureHelper.FormWith(
                        new RakudoObject[] { Attr })));

                    // Allocate a slot.
                    if (!SlotAllocation.ContainsKey(CurrentClass))
                        SlotAllocation.Add(CurrentClass, new Dictionary<string, int>());
                    SlotAllocation[CurrentClass].Add(Name, CurrentSlot);
                    CurrentSlot++;
                }

                // Find the next parent(s).
                var ParentsMeth = HOW.STable.FindMethod(TC, HOW, "parents", Hints.NO_HINT);
                var Parents = ParentsMeth.STable.Invoke(TC, ParentsMeth, CaptureHelper.FormWith(
                    new RakudoObject[] { HOW, CurrentClass },
                    new Dictionary<string,RakudoObject>() { { "local", Ops.box_int(TC, 1, TC.DefaultBoolBoxType) } }));

                // Check how many parents we have.
                var ParentElemsMeth = Parents.STable.FindMethod(TC, Parents, "elems", Hints.NO_HINT);
                var ParentElems = Ops.unbox_int(TC, ParentElemsMeth.STable.Invoke(TC, ParentElemsMeth,
                    CaptureHelper.FormWith(new RakudoObject[] { Parents })));
                if (ParentElems == 0)
                {
                    // We're done. \o/
                    Slots = CurrentSlot;
                    break;
                }
                else if (ParentElems > 1)
                {
                    // Multiple inheritnace, so we can't compute this hierarchy.
                    SlotAllocation = new Dictionary<RakudoObject, Dictionary<string, int>>();
                    return;
                }
                else
                {
                    // Just one. Get next parent.
                    var AtPosMeth = Parents.STable.FindMethod(TC, Parents, "at_pos", Hints.NO_HINT);
                    CurrentClass = AtPosMeth.STable.Invoke(TC, AtPosMeth, CaptureHelper.FormWith(
                        new RakudoObject[] { Parents, Ops.box_int(TC, 0, TC.DefaultIntBoxType) }));
                }
            }
        }
    }
}
