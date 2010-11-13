using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel.Representations;
using Rakudo.Runtime;

namespace Rakudo.Metamodel.KnowHOW
{
    /// <summary>
    /// Contains the logic that bootstraps KnowHOW, the foundation
    /// for implementing the various other bits of the object model.
    /// Works in conjunction with KnowHOWREPR.
    /// </summary>
    public static class KnowHOWBootstrapper
    {
        /// <summary>
        /// Bootstraps the KnowHOW. This is were things "bottom out" in the
        /// meta-model so it's a tad loopy. Basically, we create a KnowHOW
        /// type object. We then create an instance from that and add a
        /// bunch of methods to it. However, we also give it a special
        /// STable with FindMethod overridden in it to go looking right
        /// into the methods dictionary.
        /// </summary>
        /// <returns></returns>
        public static RakudoObject Bootstrap()
        {
            // Create our KnowHOW type object. Note we don't have a HOW
            // just yet, so pass in null.
            var REPR = REPRRegistry.get_REPR_by_name("KnowHOWREPR");
            var KnowHOW = REPR.type_object_for(null, null);

            // We'll set up a dictionary of our various methods to go into
            // KnowHOW's HOW, since we'll want to work with them a bit.
            var KnowHOWMeths = new Dictionary<string, RakudoObject>();
            KnowHOWMeths.Add("new_type", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
                {
                    // We first create a new HOW instance.
                    var KnowHOWTypeObj = CaptureHelper.GetPositional(Cap, 0);
                    var HOW = KnowHOWTypeObj.STable.REPR.instance_of(TC, KnowHOWTypeObj.STable.WHAT);

                    // Now create a new type object to go with it of the
                    // desired REPR; we default to P6opaque (note that the
                    // KnowHOW repr only knows how to store a table of
                    // methods and attributes, it can't be used for an
                    // instance object that actually wants to store some
                    // instance data).
                    var REPRName = CaptureHelper.GetNamed(Cap, "repr");
                    if (REPRName != null)
                    {
                        // Look up the REPR.
                        var REPRToUse = REPRRegistry.get_REPR_by_name(Ops.unbox_str(null, REPRName));
                        return REPRToUse.type_object_for(null, HOW);
                    }
                    else
                    {
                        // Just go with the P6opaque REPR.
                        return REPRRegistry.get_REPR_by_name("P6opaque").type_object_for(TC, HOW);
                    }
                }));
            KnowHOWMeths.Add("add_attribute", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
                {
                    var HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(Cap, 0);
                    var Attr = CaptureHelper.GetPositional(Cap, 2);
                    HOW.Attributes.Add(Attr);
                    return CaptureHelper.Nil();
                }));
            KnowHOWMeths.Add("add_method", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
                {
                    var HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(Cap, 0);
                    var Name = CaptureHelper.GetPositionalAsString(Cap, 2);
                    var Method = CaptureHelper.GetPositional(Cap, 3);
                    HOW.Methods.Add(Name, Method);
                    return CaptureHelper.Nil();
                }));
            KnowHOWMeths.Add("find_method", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
            {
                // We go to some effort to be really fast in here, 'cus it's a
                // hot path for dynamic dispatches.
                var Positionals = (Cap as P6capture.Instance).Positionals;
                var HOW = Positionals[0] as KnowHOWREPR.KnowHOWInstance;
                RakudoObject Method;
                if (HOW.Methods.TryGetValue(Ops.unbox_str(TC, Positionals[2]), out Method))
                    return Method;
                else
                    throw new InvalidOperationException("No such method " + Ops.unbox_str(TC, Positionals[2]));
            }));
            KnowHOWMeths.Add("compose", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
                {
                    var Obj = CaptureHelper.GetPositional(Cap, 1);
                    return Obj;
                }));
            KnowHOWMeths.Add("attributes", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
            {
                // Safe to just return a P6list instance that points at
                // the same thing we hold internally, since a list is
                // immutable.
                var HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(Cap, 0);
                var Result = TC.DefaultListType.STable.REPR.instance_of(TC, TC.DefaultListType);
                ((P6list.Instance)Result).Storage = HOW.Attributes;
                return Result;
            }));
            KnowHOWMeths.Add("methods", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
            {
                // Return the methods list.
                var HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(Cap, 0);
                var Result = TC.DefaultListType.STable.REPR.instance_of(TC, TC.DefaultListType);
                ((P6list.Instance)Result).Storage.AddRange(HOW.Methods.Values);
                return Result;
            }));
            KnowHOWMeths.Add("parents", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
            {
                // A pure prototype never has any parents, so return an empty list.
                return TC.DefaultListType.STable.REPR.instance_of(TC, TC.DefaultListType);
            }));
            KnowHOWMeths.Add("type_check", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
            {
                // Can only match against ourselves.
                var self = CaptureHelper.GetPositional(Cap, 1);
                var check = CaptureHelper.GetPositional(Cap, 2);
                return Ops.box_int(TC, self.STable.WHAT == check.STable.WHAT ? 1 : 0, TC.DefaultBoolBoxType);
            }));

            // We create a KnowHOW instance that can describe itself. This
            // means .HOW.HOW.HOW.HOW etc will always return that, which
            // closes the model up.
            var KnowHOWHOW = (KnowHOWREPR.KnowHOWInstance)REPR.instance_of(null, KnowHOW);
            foreach (var Method in KnowHOWMeths)
                KnowHOWHOW.Methods.Add(Method.Key, Method.Value);

            // We need to clone the STable.
            var STableCopy = new SharedTable();
            STableCopy.HOW = KnowHOWHOW;
            STableCopy.WHAT = KnowHOW.STable.WHAT;
            STableCopy.REPR = KnowHOW.STable.REPR;
            KnowHOWHOW.STable = STableCopy;

            // And put a fake FindMethod in there that just looks in the
            // dictionary.
            KnowHOWHOW.STable.SpecialFindMethod = (TC, Obj, Name, Hint) =>
                {
                    var MTable = ((KnowHOWREPR.KnowHOWInstance)Obj).Methods;
                    if (MTable.ContainsKey(Name))
                        return MTable[Name];
                    else
                        throw new InvalidOperationException("No such method " + Name);
                };
            
            // Set this as the KnowHOW's HOW.
            KnowHOW.STable.HOW = KnowHOWHOW;

            // And we should be done.
            return KnowHOW;
        }

        /// <summary>
        /// Sets up the KnowHOWAttribute object/class, which actually is a
        /// KnowHOW.
        /// </summary>
        /// <returns></returns>
        public static RakudoObject SetupKnowHOWAttribute(RakudoObject KnowHOW)
        {
            // Create a new HOW instance.
            var HOW = KnowHOW.STable.REPR.instance_of(null, KnowHOW) as KnowHOWREPR.KnowHOWInstance;

            // We base the attribute on P6str, since we just want to store an
            // attribute name for now.
            var KnowHOWAttribute = REPRRegistry.get_REPR_by_name("P6str").type_object_for(null, HOW);

            // Add methods new and Str.
            HOW.Methods.Add("new", CodeObjectUtility.WrapNativeMethod((TC, Code, Cap) =>
                {
                    var WHAT = CaptureHelper.GetPositional(Cap, 0).STable.WHAT;
                    var Name = Ops.unbox_str(TC, CaptureHelper.GetNamed(Cap, "name"));
                    return Ops.box_str(TC, Name, WHAT);
                }));
            HOW.Methods.Add("name", CodeObjectUtility.WrapNativeMethod((TC, Code, Cap) =>
                {
                    var self = CaptureHelper.GetPositional(Cap, 0);
                    return Ops.box_str(TC, Ops.unbox_str(TC, self), TC.DefaultStrBoxType);
                }));

            return KnowHOWAttribute;
        }
    }
}
