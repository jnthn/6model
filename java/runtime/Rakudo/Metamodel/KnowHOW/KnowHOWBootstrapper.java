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
public class KnowHOWBootstrapper  // public static in the C# version
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
        KnowHOWMeths.put("new_type", CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // We first create a new HOW instance.
                RakudoObject knowHOWTypeObj = CaptureHelper.GetPositional(capture, 0);
                RakudoObject HOW = knowHOWTypeObj.getSTable().REPR.instance_of(tc, knowHOWTypeObj.getSTable().WHAT);

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
        }));
        KnowHOWMeths.put("add_attribute", CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                RakudoObject Attr = CaptureHelper.GetPositional(capture, 2);
                HOW.Attributes.add(Attr);
                return CaptureHelper.Nil();
            }
        }));
        KnowHOWMeths.put("add_method", CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                String name = CaptureHelper.GetPositionalAsString(capture, 2);
                RakudoObject method = CaptureHelper.GetPositional(capture, 3);
                HOW.Methods.put(name, method);
                return CaptureHelper.Nil();
            }
        }));
        KnowHOWMeths.put("find_method", CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // We go to some effort to be really fast in here, 'cus it's a
                // hot path for dynamic dispatches.
                RakudoObject[] Positionals = ((P6capture.Instance)capture).Positionals;
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)Positionals[0];
                if (HOW.Methods.containsKey(Ops.unbox_str(tc, Positionals[2])))
                    return HOW.Methods.get(Ops.unbox_str(tc, Positionals[2]));
                else {
                    // throw new NoSuchMethodException("No such method " + Ops.unbox_str(tc, Positionals[1]));
                    throw new UnsupportedOperationException("No such method " + Ops.unbox_str(tc, Positionals[1]));
                }
            }
        }));
        KnowHOWMeths.put("compose", CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                RakudoObject obj = CaptureHelper.GetPositional(capture, 1);
                return obj;
            }
        }));
        KnowHOWMeths.put("attributes", CodeObjectUtility.WrapNativeMethod(new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // Safe to just return a P6list instance that points at
                // the same thing we hold internally, since a list is
                // immutable.
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                RakudoObject result = tc.DefaultListType.getSTable().REPR.instance_of(tc, tc.DefaultListType);
                ((P6list.Instance)result).Storage = HOW.Attributes;
                return result;
            }
        }));
        KnowHOWMeths.put("methods", CodeObjectUtility.WrapNativeMethod(new RakudoCodeRef.IFunc_Body() { // an anonymous class where C# uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // Return the methods list.
                KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)CaptureHelper.GetPositional(capture, 0);
                RakudoObject result = tc.DefaultListType.getSTable().REPR.instance_of(tc, tc.DefaultListType);
                ((P6list.Instance)result).Storage.addAll(HOW.Methods.values());
                return result;
            }
        }));
        KnowHOWMeths.put("parents", CodeObjectUtility.WrapNativeMethod(new RakudoCodeRef.IFunc_Body() { // an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject ignored, RakudoObject capture)
            {
                // A pure prototype never has any parents, so return an empty list.
                return tc.DefaultListType.getSTable().REPR.instance_of(tc, tc.DefaultListType);
            }
        }));

        // We create a KnowHOW instance that can describe itself.
        // This means .HOW.HOW.HOW.HOW etc will always return that,
        // which closes the model up.
        KnowHOWREPR.KnowHOWInstance KnowHOWHOW = (KnowHOWREPR.KnowHOWInstance)REPR.instance_of(null,KnowHOW);
        for ( String key : KnowHOWMeths.keySet() )
            KnowHOWHOW.Methods.put(key, KnowHOWMeths.get(key));

        // We need to clone the STable.
        SharedTable sTableCopy = new SharedTable();
        sTableCopy.HOW = KnowHOWHOW;
        sTableCopy.WHAT = KnowHOW.getSTable().WHAT;
        sTableCopy.REPR = KnowHOW.getSTable().REPR;
        KnowHOWHOW.setSTable(sTableCopy);

        // And put a fake FindMethod in there that just looks in the
        // dictionary.
        KnowHOWHOW.getSTable().FindMethod = new IFindMethod()
        { // an anonymous class
            public RakudoObject FindMethod(ThreadContext tc, RakudoObject obj, String name, int hint)
            {
                HashMap<String, RakudoObject> mTable = ((KnowHOWREPR.KnowHOWInstance)obj).Methods;
                if (mTable.containsKey(name))
                    return mTable.get(name);
                else {
                    throw new UnsupportedOperationException("No such method " + name);
                }
            }
        };

        // Set this as the KnowHOW's HOW.
        KnowHOW.getSTable().HOW = KnowHOWHOW;

        // And we should be done.
        return KnowHOW;
    }

    /// <summary>
    /// Sets up the KnowHOWAttribute object/class, which actually is a
    /// KnowHOW.
    /// </summary>
    /// <returns></returns>
    public static RakudoObject SetupKnowHOWAttribute(RakudoObject knowHOW)
    {
        // Create a new HOW instance.
        KnowHOWREPR.KnowHOWInstance HOW = (KnowHOWREPR.KnowHOWInstance)knowHOW.getSTable().REPR.instance_of(null, knowHOW);

        // We base the attribute on P6str, since we just want to store an
        // attribute name for now.
        RakudoObject knowHOWAttribute = REPRRegistry.get_REPR_by_name("P6str").type_object_for(null, HOW);

        // Add methods new and Str.
        HOW.Methods.put("new", CodeObjectUtility.WrapNativeMethod(new RakudoCodeRef.IFunc_Body() {  // the C# version uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject obj, RakudoObject capture)
            {
                RakudoObject WHAT = CaptureHelper.GetPositional(capture, 0).getSTable().WHAT;
                String name = Ops.unbox_str(tc, CaptureHelper.GetNamed(capture, "name"));
                return Ops.box_str(tc, name, WHAT);
            }
        }));
        HOW.Methods.put("name", CodeObjectUtility.WrapNativeMethod(new RakudoCodeRef.IFunc_Body() { // the C# version uses a lambda
            public RakudoObject Invoke(ThreadContext tc, RakudoObject obj, RakudoObject capture)
            {
                RakudoObject self = CaptureHelper.GetPositional(capture, 0);
                return Ops.box_str(tc, Ops.unbox_str(tc, self), tc.DefaultStrBoxType);
            }
        }));

        return knowHOWAttribute;
    }
}

