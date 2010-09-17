package Rakudo.Metamodel.KnowHOW;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import Rakudo.Metamodel.IFindMethod;
import Rakudo.Metamodel.KnowHOW.KnowHOWREPR;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.CaptureHelper;
import Rakudo.Runtime.CodeObjectUtility;
import Rakudo.Runtime.Ops;
import Rakudo.Runtime.ThreadContext;

/// <summary>
/// Contains the logic that bootstraps KnowHOW, the foundation
/// for implementing the various other bits of the object model.
/// Works in conjunction with KnowHOWREPR.
/// </summary>
public class KnowHOWBootstrapper
// public static class KnowHOWBootstrapper // the C# version
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
        Representation REPR = REPRRegistry.get_REPR_by_name("KnowHOWREPR");
        RakudoObject KnowHOW = REPR.type_object_for(null,null);

        // We'll set up a dictionary of our various methods to go into
        // KnowHOW's HOW, since we'll want to work with them a bit.
        HashMap<String, RakudoObject> KnowHOWMeths = new HashMap<String, RakudoObject>();

        // The new_type method
        RakudoCodeRef.IFunc_Body func_new_type = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // We first create a new HOW instance.
                RakudoObject KnowHOWTypeObj = CaptureHelper.GetPositional(capture, 0);
                RakudoObject HOW = KnowHOWTypeObj.getSTable().REPR.instance_of(tc, KnowHOWTypeObj.getSTable().WHAT);

                // Now create a new type object to go with it of the
                // desired REPR; we default to P6opaque (note that the
                // KnowHOW repr only knows how to store a table of
                // methods and attributes, it can't be used for an
                // instance object that actually wants to store some
                // instance data).
                RakudoObject REPRName = CaptureHelper.GetNamed(capture, "repr");
                if (REPRName != null)
                {
                    // Look up the REPR.
                    Representation REPRToUse = REPRRegistry.get_REPR_by_name(Ops.unbox_str(null, REPRName));
                    return REPRToUse.type_object_for(null, HOW);
                }
                else
                {
                    // Just go with the P6opaque REPR.
                    return REPRRegistry.get_REPR_by_name("P6opaque").type_object_for(tc, HOW);
                }
            }
        };
        KnowHOWMeths.put("new_type", CodeObjectUtility.WrapNativeMethod(func_new_type));

        // The add_attribute method
        RakudoCodeRef.IFunc_Body func_add_attribute = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                RakudoObject Attr = CaptureHelper.GetPositional(capture, 2);
                HOW.Attributes.add(Attr);
                return CaptureHelper.Nil();
            }
        };
        KnowHOWMeths.put("add_attribute", CodeObjectUtility.WrapNativeMethod(func_add_attribute));

        // The add_method method
        RakudoCodeRef.IFunc_Body func_add_method = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                String Name = CaptureHelper.GetPositionalAsString(capture, 2);
                RakudoObject Method = CaptureHelper.GetPositional(capture, 3);
                HOW.Methods.put(Name, Method);
                return CaptureHelper.Nil();
            }
        };
        KnowHOWMeths.put("add_method", CodeObjectUtility.WrapNativeMethod(func_add_method));

        // The find_method method
        RakudoCodeRef.IFunc_Body func_find_method = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // We go to some effort to be really fast in here, 'cus it's a
                // hot path for dynamic dispatches.
                RakudoObject[] Positionals = ((P6capture.Instance)capture).Positionals;
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)Positionals[0];
                if (HOW.Methods.containsKey(Ops.unbox_str(tc, Positionals[1])))
                    return HOW.Methods.get(Ops.unbox_str(tc, Positionals[1]));
                else {
                    // throw new NoSuchMethodException("No such method " + Ops.unbox_str(tc, Positionals[1]));
                    System.err.println("No such method " + Ops.unbox_str(tc, Positionals[1]));
                    System.exit(1);
                    return null;
                }
            }
        };
        KnowHOWMeths.put("find_method", CodeObjectUtility.WrapNativeMethod(func_find_method));

        // The compose method
        RakudoCodeRef.IFunc_Body func_compose = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                RakudoObject Obj = CaptureHelper.GetPositional(capture, 1);
                return Obj;
            }
        };
        KnowHOWMeths.put("compose", CodeObjectUtility.WrapNativeMethod(func_compose));

        // The attributes method
        RakudoCodeRef.IFunc_Body func_attributes = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                RakudoObject Result = tc.DefaultListType.getSTable().REPR.instance_of(tc, tc.DefaultListType);
                ((P6list.Instance)Result).Storage = HOW.Attributes;
                return Result;
            }
        };
        KnowHOWMeths.put("attributes", CodeObjectUtility.WrapNativeMethod(func_attributes));

        // The methods method
        RakudoCodeRef.IFunc_Body func_methods = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                RakudoObject Result = tc.DefaultListType.getSTable().REPR.instance_of(tc, tc.DefaultListType);
                ((P6list.Instance)Result).Storage.addAll(HOW.Methods.values());
                return Result;
            }
        };
        KnowHOWMeths.put("methods", CodeObjectUtility.WrapNativeMethod(func_methods));

        // The parents method
        RakudoCodeRef.IFunc_Body func_parents = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // A pure prototype never has any parents, so return an empty list.
                return tc.DefaultListType.getSTable().REPR.instance_of(tc, tc.DefaultListType);
            }
        };
        KnowHOWMeths.put("parents", CodeObjectUtility.WrapNativeMethod(func_parents));

        // We create a KnowHOW instance that can describe itself.
        // This means .HOW.HOW.HOW.HOW etc will always return that,
        // which closes the model up.
        KnowHOWREPR.KnowHOWInstance KnowHOWHOW = (KnowHOWREPR.KnowHOWInstance)REPR.instance_of(null,KnowHOW);
        for ( String key : KnowHOWMeths.keySet() )
            KnowHOWHOW.Methods.put(key, KnowHOWMeths.get(key));

        // We need to clone the STable.
        SharedTable STableCopy = new SharedTable();
        STableCopy.HOW = KnowHOWHOW;
        STableCopy.WHAT = KnowHOW.getSTable().WHAT;
        STableCopy.REPR = KnowHOW.getSTable().REPR;
        KnowHOWHOW.setSTable(STableCopy);

        // And put a fake FindMethod in there that just looks in the
        // dictionary.
        IFindMethod func_FindMethod = new IFindMethod()
        { // create an anonymous class
            public RakudoObject FindMethod(ThreadContext tc, RakudoObject obj, String name, int hint)
            {
                HashMap<String, RakudoObject> MTable = ((KnowHOWREPR.KnowHOWInstance)obj).Methods;
                if (MTable.containsKey(name))
                    return MTable.get(name);
                else {
                    // throw new InvalidOperationException("No such method " + Name);
                    System.err.println("No such method " + name);
                    System.exit(1);
                    return null;
                }
            }
        };
        KnowHOWHOW.getSTable().FindMethod = func_FindMethod;

        // Set this as the KnowHOW's HOW.
        KnowHOW.getSTable().HOW = KnowHOWHOW;

        // And we should be done.
        return KnowHOW;
    }
}

