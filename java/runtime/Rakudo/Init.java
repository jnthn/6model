package Rakudo;

import Rakudo.Metamodel.KnowHOW.KnowHOWBootstrapper;
import Rakudo.Metamodel.KnowHOW.KnowHOWREPR;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Metamodel.Representations.P6hash;
import Rakudo.Metamodel.Representations.P6int;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.Representations.P6num;
import Rakudo.Metamodel.Representations.P6opaque;
import Rakudo.Metamodel.Representations.P6str;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Runtime.CaptureHelper;
import Rakudo.Runtime.CodeObjectUtility;
import Rakudo.Runtime.Context;
import Rakudo.Runtime.ExecutionDomain;
import Rakudo.Runtime.Lexpad;
import Rakudo.Runtime.Ops;
import Rakudo.Runtime.ThreadContext;

/// <summary>
/// Does initialization of the Rakudo library.
/// </summary>
public class Init
//public static class Init
{
    /// <summary>
    /// Have we already registered the representations?
    /// </summary>
    private static boolean REPRS_Registered = false;

    /// <summary>
    /// Handles the various bits of initialization that are needed.
    /// Probably needs some don't-dupe-this work.
    /// </summary>
    public static ThreadContext Initialize(String SettingName)
    {
        // Bootstrap the meta-model.
        RegisterRepresentations();
        RakudoObject KnowHOW = KnowHOWBootstrapper.Bootstrap();

        // See if we're to load a setting or use the fake bootstrapping one.
        Context settingContext;
        if (SettingName == null)
        {
            settingContext = BootstrapSetting(KnowHOW);
        }
        else
        {
            settingContext = LoadSetting(SettingName, KnowHOW);
        }

        // Cache native capture and LLCode type object.
        CaptureHelper.CaptureTypeObject = settingContext.LexPad.GetByName("capture");
        CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)settingContext.LexPad.GetByName("LLCode");

        // Create an execution domain and a thread context for it.
        ExecutionDomain execDom = new ExecutionDomain();
        ThreadContext   thread  = new ThreadContext();
        thread.Domain = execDom;
        thread.CurrentContext = settingContext;
        thread.DefaultBoolBoxType = settingContext.LexPad.GetByName("NQPInt");
        thread.DefaultIntBoxType  = settingContext.LexPad.GetByName("NQPInt");
        thread.DefaultNumBoxType  = settingContext.LexPad.GetByName("NQPNum");
        thread.DefaultStrBoxType  = settingContext.LexPad.GetByName("NQPStr");

        return thread;
    }
    
    /// <summary>
    /// Registers all of the built-in representations.
    /// </summary>
    private static void RegisterRepresentations()
    {
        if (!REPRS_Registered)
        {
            REPRRegistry.register_REPR("KnowHOWREPR", new KnowHOWREPR());
            REPRRegistry.register_REPR("P6opaque", new P6opaque());
            REPRRegistry.register_REPR("P6hash", new P6hash());
            REPRRegistry.register_REPR("P6int", new P6int());
            REPRRegistry.register_REPR("P6num", new P6num());
            REPRRegistry.register_REPR("P6str", new P6str());
            REPRRegistry.register_REPR("P6capture", new P6capture());
            REPRRegistry.register_REPR("RakudoCodeRef", new RakudoCodeRef());
            REPRRegistry.register_REPR("P6list", new P6list());
            REPRS_Registered = true;
        }
    }

    /// <summary>
    /// Sets up the bootstrapping setting that we use to compile the
    /// real setting.
    /// </summary>
    /// <param name="KnowHOW"></param>
    /// <returns></returns>
    private static Context BootstrapSetting(RakudoObject KnowHOW)
    {
        Context settingContext = new Context();
        settingContext.LexPad = new Lexpad(new String[]
            { "KnowHOW", "capture", "NQPInt", "NQPNum", "NQPStr", "LLCode", "list" });
        RakudoCodeRef.IFunc_Body funcBody = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject self, RakudoObject capture) {
                RakudoObject nqpList = Ops.get_lex(tc, "NQPList");
                P6list.Instance list = (P6list.Instance)nqpList.getSTable().REPR.instance_of(tc, nqpList);
                P6capture.Instance NativeCapture = (P6capture.Instance)capture;
                for (RakudoObject obj : NativeCapture.Positionals)
                    list.Storage.add(obj);
                return list;
            }
        };
        settingContext.LexPad.Storage = new RakudoObject[]
            {
                KnowHOW,
                REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6int").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6num").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6str").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null,KnowHOW.getSTable().REPR.instance_of(null,KnowHOW)),
                CodeObjectUtility.WrapNativeMethod(funcBody)
            };
        return settingContext;
    }

    /// <summary>
    /// Loads the setting with the given name.
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="KnowHOW"></param>
    /// <returns></returns>
    public static Context LoadSetting(String Name, RakudoObject KnowHOW)
    {
        // Load the assembly.
// TODO var settingAssembly = AppDomain.CurrentDomain.Load(Name);

        // Find the setting type and its LoadSetting method.
// TODO var Class = settingAssembly.GetType("NQPSetting");
// TODO var Method = Class.GetMethod("LoadSetting", BindingFlags.NonPublic | BindingFlags.Static);
        
        // Run it to get the context we want.
        // TODO Context settingContext = (Context)Method.Invoke(null, new object[] { });
        Context settingContext = null; // TODO remove

        // Fudge a few more things in.
        // XXX Should be able to toss all of these but KnowHOW.
        settingContext.LexPad.Extend(new String[]
            { "KnowHOW", "print", "say", "capture", "LLCode" });
        settingContext.LexPad.SetByName("KnowHOW", KnowHOW);

        RakudoCodeRef.IFunc_Body funcPrint = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject self, RakudoObject capture)
            {
                RakudoObject value = CaptureHelper.GetPositional(capture, 0);
                RakudoObject strMeth = self.getSTable().FindMethod.FindMethod(tc, value, "Str", 0);
                RakudoObject strVal = strMeth.getSTable().Invoke.Invoke(tc, strMeth, capture);
                System.out.print(Ops.unbox_str(null, strVal));
                return CaptureHelper.Nil();
            }
        };
        settingContext.LexPad.SetByName("print", CodeObjectUtility.WrapNativeMethod(funcPrint));

        RakudoCodeRef.IFunc_Body funcSay = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject self, RakudoObject capture)
            {
                RakudoObject value = CaptureHelper.GetPositional(capture, 0);
                RakudoObject strMeth = self.getSTable().FindMethod.FindMethod(tc, value, "Str", 0);
                RakudoObject strVal = strMeth.getSTable().Invoke.Invoke(tc, strMeth, capture);
                System.out.println(Ops.unbox_str(null, strVal));
                return CaptureHelper.Nil();
            }
        };
        settingContext.LexPad.SetByName("say", CodeObjectUtility.WrapNativeMethod(funcSay));

        settingContext.LexPad.SetByName("capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null,null));
        settingContext.LexPad.SetByName("LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null,KnowHOW.getSTable().REPR.instance_of(null,KnowHOW)));
        
        return settingContext;
    }
}

