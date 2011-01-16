package Rakudo;

import Rakudo.Metamodel.KnowHOW.KnowHOWBootstrapper;
import Rakudo.Metamodel.KnowHOW.KnowHOWREPR;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Metamodel.Representations.P6hash;
import Rakudo.Metamodel.Representations.P6int;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.Representations.P6mapping;
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
public class Init  // public static in the C# version
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
        RakudoObject knowHOWAttribute = KnowHOWBootstrapper.SetupKnowHOWAttribute(knowHOW);

        // Either load a named setting or use the fake bootstrapping one.
        Context settingContext =
            // Comment out the next line to always use the fake Setting.
            (settingName != null) ? LoadSetting(settingName, knowHOW, knowHOWAttribute) :
                                    BootstrapSetting(knowHOW, knowHOWAttribute);

        // Cache native capture and LLCode type object.
        CaptureHelper.CaptureTypeObject = settingContext.LexPad.GetByName("capture");
        CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)settingContext.LexPad.GetByName("NQPCode");

        // Create an execution domain and a thread context for it.
        ExecutionDomain executionDomain  = new ExecutionDomain();
        executionDomain.Setting          = settingContext;
        ThreadContext threadContext      = new ThreadContext();
        threadContext.Domain             = executionDomain;
        threadContext.CurrentContext     = settingContext;
        threadContext.DefaultBoolBoxType = settingContext.LexPad.GetByName("NQPInt");
        threadContext.DefaultIntBoxType  = settingContext.LexPad.GetByName("NQPInt");
        threadContext.DefaultNumBoxType  = settingContext.LexPad.GetByName("NQPNum");
        threadContext.DefaultStrBoxType  = settingContext.LexPad.GetByName("NQPStr");
        threadContext.DefaultListType    = settingContext.LexPad.GetByName("NQPList");

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
            REPRRegistry.register_REPR("P6mapping", new P6mapping());
            REPRS_Registered = true;
        }
    }

    /// <summary>
    /// Sets up the bootstrapping setting that we use to compile the
    /// real setting.
    /// </summary>
    /// <param name="KnowHOW"></param>
    /// <returns></returns>
    private static Context BootstrapSetting(RakudoObject knowHOW, RakudoObject knowHOWAttribute)
    {
        System.err.println( "calling new Context from Init" );
        Context settingContext = new Context();
        settingContext.LexPad = new Lexpad(new String[]
            { "KnowHOW", "KnowHOWAttribute", "capture", "NQPInt", "NQPNum", "NQPStr", "NQPList", "NQPCode", "list" });
        settingContext.LexPad.Storage = new RakudoObject[]
            {
                knowHOW,
                knowHOWAttribute,
                REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6int").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6num").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6str").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("P6list").type_object_for(null,null),
                REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null,knowHOW.getSTable().REPR.instance_of(null,knowHOW)),
                CodeObjectUtility.WrapNativeMethod(new RakudoCodeRef.IFunc_Body()
                    { // an anonymous class instead of the lambda in the C# version
                        public RakudoObject Invoke(ThreadContext tc, RakudoObject self, RakudoObject capture) {
                            RakudoObject nqpList = Ops.get_lex(tc, "NQPList");
                            P6list.Instance list = (P6list.Instance)(nqpList.getSTable().REPR.instance_of(tc, nqpList));
                            P6capture.Instance nativeCapture = (P6capture.Instance)capture;
                            for (RakudoObject obj : nativeCapture.Positionals)
                                list.Storage.add(obj);
                            return list;
                        }
                    })
            };
        return settingContext;
    }

    /// <summary>
    /// Loads the setting with the given name.
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="KnowHOW"></param>
    /// <returns></returns>
    public static Context LoadSetting(String settingName, RakudoObject knowHOW, RakudoObject knowHOWAttribute)
    {
        // Load the assembly.
        System.err.println("Init.LoadSetting begin loading " + settingName );
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class<?> classNQPSetting = null; // grrr, a wildcard type :-(
        try {
            classNQPSetting = loader.loadClass(settingName);
        }
        catch (ClassNotFoundException ex) {
            System.err.println("Class " + settingName + " not found: " + ex.getMessage());
            System.exit(1);
        }
        catch ( Exception ex ) {
            System.err.println("loadClass(\"" + settingName + "\") exception: " + ex.getMessage());
            System.exit(1);
        }

        // TODO: remove
        if ( classNQPSetting == null ) {
            System.err.println("classNQPSetting is null");
            System.exit(1);
        }

        // Find the setting type and its LoadSetting method.
        java.lang.reflect.Method methodLoadSetting = null;
        try {
            methodLoadSetting = classNQPSetting.getMethod("LoadSetting");
        }
        catch ( NoSuchMethodException  ex) {
            System.err.println("Method LoadSetting not found: " + ex.getMessage());
            System.exit(1);
        }
        catch ( Exception ex ) {
            System.err.println("getMethod(\"LoadSetting\") exception: " + ex.getMessage());
            System.exit(1);
        }

        // TODO: remove
        if ( methodLoadSetting == null ) {
            System.err.println("methodLoadSetting is null");
            System.exit(1);
        }
        else {
            System.err.println("methodLoadSetting is ok: " + methodLoadSetting );
        }

        // Run it to get the context we want.
        Context settingContext = null;
        try {
            settingContext = (Context)methodLoadSetting.invoke( null );
        }
        catch (IllegalAccessException ex) {
            System.err.println("Illegal access: " + ex.getMessage());
            System.exit(1);
        }
        catch (java.lang.reflect.InvocationTargetException ex) {
            System.err.println("Invocation target exception: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        // Fudge a few more things in.
        // XXX Should be able to toss all of these but KnowHOW.
        settingContext.LexPad.Extend(new String[]
            { "KnowHOW", "KnowHOWAttribute", "print", "say", "capture", "LLCode" });

        settingContext.LexPad.SetByName("KnowHOW", knowHOW);
        settingContext.LexPad.SetByName("KnowHOWAttribute", knowHOWAttribute);
        settingContext.LexPad.SetByName("print",
            CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body()
                { // an anonymous class where C# has a => (lambda)
                    public RakudoObject Invoke(ThreadContext tc, RakudoObject self, RakudoObject capture)
                    {
                        for (int i = 0; i < CaptureHelper.NumPositionals(capture); i++) {
                            RakudoObject value = CaptureHelper.GetPositional(capture, i);
                            RakudoObject strMeth = self.getSTable().FindMethod.FindMethod(tc, value, "Str", 0);
                            RakudoObject strVal = strMeth.getSTable().Invoke.Invoke(tc, strMeth,
                                CaptureHelper.FormWith( new RakudoObject[] { value } ));
                            System.out.print(Ops.unbox_str(null, strVal));
                        }
                        return CaptureHelper.Nil();
                    }
                }
        ));
        settingContext.LexPad.SetByName("say",
            CodeObjectUtility.WrapNativeMethod( new RakudoCodeRef.IFunc_Body()
                { // an anonymous class where C# has a => (lambda)
                    public RakudoObject Invoke(ThreadContext tc, RakudoObject self, RakudoObject capture)
                    {
                        for (int i = 0; i < CaptureHelper.NumPositionals(capture); i++) {
                            RakudoObject value = CaptureHelper.GetPositional(capture, i);
                            RakudoObject strMeth = self.getSTable().FindMethod.FindMethod(tc, value, "Str", 0);
                            RakudoObject strVal = strMeth.getSTable().Invoke.Invoke(tc, strMeth,
                                CaptureHelper.FormWith( new RakudoObject[] { value } ));
                            System.out.print(Ops.unbox_str(null, strVal));
                        }
                        System.out.println();
                        return CaptureHelper.Nil();
                    }
                }
        ));
        settingContext.LexPad.SetByName("capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null,null));

        return settingContext;
    }
}

