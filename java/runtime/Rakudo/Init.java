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
    public static ThreadContext Initialize(String settingName)
    {
        // Bootstrap the meta-model.
        RegisterRepresentations();
        RakudoObject knowHOW = KnowHOWBootstrapper.Bootstrap();

        // Either load a named setting or use the fake bootstrapping one.
        Context settingContext =
            (settingName != null) ? LoadSetting(settingName, knowHOW)
                                  : BootstrapSetting(knowHOW);

        // Cache native capture and LLCode type object.
        CaptureHelper.CaptureTypeObject = settingContext.LexPad.GetByName("capture");
        CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)settingContext.LexPad.GetByName("LLCode");

        // Create an execution domain and a thread context for it.
        ExecutionDomain executionDomain  = new ExecutionDomain();
        ThreadContext threadContext      = new ThreadContext();
        threadContext.Domain             = executionDomain;
        threadContext.CurrentContext     = settingContext;
        threadContext.DefaultBoolBoxType = settingContext.LexPad.GetByName("NQPInt");
        threadContext.DefaultIntBoxType  = settingContext.LexPad.GetByName("NQPInt");
        threadContext.DefaultNumBoxType  = settingContext.LexPad.GetByName("NQPNum");
        threadContext.DefaultStrBoxType  = settingContext.LexPad.GetByName("NQPStr");
        return threadContext;
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
    public static Context LoadSetting(String name, RakudoObject knowHOW)
    {
        // Load the assembly.
        // var settingAssembly = AppDomain.CurrentDomain.Load(Name);
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class<?> classNQPSetting; // grrr, a wildcard type :-(
        try {
            classNQPSetting = loader.loadClass(name);
        }
        catch (ClassNotFoundException ex) {
            classNQPSetting = null;
            System.err.println("Class " + name + " not found: " + ex.getMessage());
            System.exit(1);
        }

        // Find the setting type and its LoadSetting method.
        // var Class = settingAssembly.GetType("NQPSetting");
        // var Method = Class.GetMethod("LoadSetting", BindingFlags.NonPublic | BindingFlags.Static);
        String s = new String();
        Class stringClass = s.getClass();
        java.lang.reflect.Method methodLoadSetting;
        try {
            methodLoadSetting = classNQPSetting.getMethod("LoadSetting", stringClass);
        }
        catch ( NoSuchMethodException  ex) {
            methodLoadSetting = null;
            System.err.println("Method LoadSetting not found: " + ex.getMessage());
            System.exit(1);
        }

        // Run it to get the context we want.
        // Context settingContext = (Context)Method.Invoke(null, new Object[] { });
        Context settingContext = null;
        try {
            settingContext = (Context)methodLoadSetting.invoke( null, s );
        }
        catch (IllegalAccessException ex) {
            System.err.println("Illegal access: " + ex.getMessage());
            System.exit(1);
        }
        catch (java.lang.reflect.InvocationTargetException ex) {
            System.err.println("Invocation target exception: " + ex.getMessage());
            System.exit(1);
        }

        // Fudge a few more things in.
        // XXX Should be able to toss all of these but KnowHOW.
        settingContext.LexPad.Extend(new String[]
            { "KnowHOW", "print", "say", "capture", "LLCode" });

        settingContext.LexPad.SetByName("KnowHOW", knowHOW);

        RakudoCodeRef.IFunc_Body funcPrint = new RakudoCodeRef.IFunc_Body()
        { // create an anonymous class
            public RakudoObject Invoke(ThreadContext tc, RakudoObject objSelf, RakudoObject objCapture)
            {
                RakudoObject objParam = CaptureHelper.GetPositional(objCapture, 0);
                RakudoObject objMethodStr = objSelf.getSTable().FindMethod.FindMethod(tc, objParam, "Str", 0);
                RakudoObject objParamStr = objMethodStr.getSTable().Invoke.Invoke(tc, objMethodStr, objCapture);
                System.out.print(Ops.unbox_str(null, objParamStr));
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
        settingContext.LexPad.SetByName("LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null, knowHOW.getSTable().REPR.instance_of(null, knowHOW)));

        return settingContext;
    }
}

