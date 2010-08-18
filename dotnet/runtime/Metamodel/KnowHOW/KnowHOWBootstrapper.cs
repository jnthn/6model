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
            var KnowHOW = REPR.type_object_for(null);

            // We'll set up a dictionary of our various methods to go into
            // KnowHOW's HOW, since we'll want to work with them a bit.
            var KnowHOWMeths = new Dictionary<string, RakudoObject>();
            KnowHOWMeths.Add("new_type", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
                {
                    // We first create a new HOW instance.
                    var KnowHOWTypeObj = CaptureHelper.GetPositional(Cap, 0);
                    var HOW = KnowHOWTypeObj.STable.REPR.instance_of(KnowHOWTypeObj.STable.WHAT);

                    // Now create a new type object to go with it of the
                    // desired REPR; we default to KnowHOW.
                    var REPRName = CaptureHelper.GetNamed(Cap, "repr");
                    if (REPRName != null)
                    {
                        // Look up the REPR.
                        var REPRToUse = REPRRegistry.get_REPR_by_name(Ops.unbox<string>(REPRName));
                        return REPRToUse.type_object_for(HOW);
                    }
                    else
                    {
                        // Just go with the KnowHOW REPR.
                        return KnowHOWTypeObj.STable.REPR.type_object_for(HOW);
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
                    var Name = CaptureHelper.GetPositionalAs<string>(Cap, 2);
                    var Method = CaptureHelper.GetPositional(Cap, 3);
                    HOW.Methods.Add(Name, Method);
                    return CaptureHelper.Nil();
                }));
            KnowHOWMeths.Add("find_method", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
            {
                var HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(Cap, 0);
                var Name = CaptureHelper.GetPositionalAs<string>(Cap, 1);
                if (HOW.Methods.ContainsKey(Name))
                    return HOW.Methods[Name];
                else
                    throw new InvalidOperationException("No such method " + Name);
            }));
            KnowHOWMeths.Add("compose", CodeObjectUtility.WrapNativeMethod((TC, Ignored, Cap) =>
                {
                    var Obj = CaptureHelper.GetPositional(Cap, 1);
                    return Obj;
                }));

            // We create a KnowHOW instance that can describe itself. This
            // means .HOW.HOW.HOW.HOW etc will always return that, which
            // closes the model up.
            var KnowHOWHOW = (KnowHOWREPR.KnowHOWInstance)REPR.instance_of(KnowHOW);
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
            KnowHOWHOW.STable.FindMethod = (TC, Obj, Name, Hint) =>
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
    }
}
