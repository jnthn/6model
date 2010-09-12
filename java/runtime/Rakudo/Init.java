package Rakudo;

import Rakudo.Runtime.ExecutionDomain;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Runtime.Lexpad;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Runtime.Context;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Runtime.CaptureHelper;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Metamodel.KnowHOW.KnowHOWREPR;
import Rakudo.Metamodel.KnowHOW.KnowHOWBootstrapper;

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
// TODO CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)SettingContext.LexPad.GetByName("LLCode");

        // Create an execution domain and a thread context for it.
        ExecutionDomain execDom = new ExecutionDomain();
        ThreadContext   thread  = new ThreadContext();
        thread.Domain = execDom;
        thread.CurrentContext = settingContext;
// TODO thread.DefaultBoolBoxType = settingContext.LexPad.GetByName("NQPInt");
// TODO thread.DefaultIntBoxType  = settingContext.LexPad.GetByName("NQPInt");
// TODO thread.DefaultNumBoxType  = settingContext.LexPad.GetByName("NQPNum");
// TODO thread.DefaultStrBoxType  = settingContext.LexPad.GetByName("NQPStr");

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
// TODO     REPRRegistry.register_REPR("P6opaque", new P6opaque());
// TODO     REPRRegistry.register_REPR("P6hash", new P6hash());
// TODO     REPRRegistry.register_REPR("P6int", new P6int());
// TODO     REPRRegistry.register_REPR("P6num", new P6num());
// TODO     REPRRegistry.register_REPR("P6str", new P6str());
            REPRRegistry.register_REPR("P6capture", new P6capture());
            REPRRegistry.register_REPR("RakudoCodeRef", new RakudoCodeRef());
// TODO     REPRRegistry.register_REPR("P6list", new P6list());
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
        settingContext.LexPad.Storage = new RakudoObject[]
            {
                KnowHOW,
                REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null),
                REPRRegistry.get_REPR_by_name("P6int").type_object_for(null),
                REPRRegistry.get_REPR_by_name("P6num").type_object_for(null),
                REPRRegistry.get_REPR_by_name("P6str").type_object_for(null),
                REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(KnowHOW.getSTable().REPR.instance_of(KnowHOW)),
/* TODO
                CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        var NQPList = Ops.get_lex(TC, "NQPList");
                        var List = NQPList.STable.REPR.instance_of(NQPList) as P6list.Instance;
                        var NativeCapture = C as P6capture.Instance;
                        foreach (var Obj in NativeCapture.Positionals)
                            List.Storage.Add(Obj);
                        return List;
                    })
*/
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
/* TODO
        settingContext.LexPad.SetByName("print",
            CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                {
                    var Value = CaptureHelper.GetPositional(C, 0);
                    var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                    var StrVal = StrMeth.STable.Invoke(TC, StrMeth, C);
                    Console.Write(Ops.unbox_str(null, StrVal));
                    return CaptureHelper.Nil();
                }));
*/
/* TODO
        settingContext.LexPad.SetByName("say",
            CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                {
                    var Value = CaptureHelper.GetPositional(C, 0);
                    var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                    var StrVal = StrMeth.STable.Invoke(TC, StrMeth, C);
                    Console.WriteLine(Ops.unbox_str(null, StrVal));
                    return CaptureHelper.Nil();
                }));
*/
        settingContext.LexPad.SetByName("capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null));
        settingContext.LexPad.SetByName("LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(KnowHOW.getSTable().REPR.instance_of(KnowHOW)));
        
        return settingContext;
    }
}

