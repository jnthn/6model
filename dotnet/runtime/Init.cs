using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;
using Rakudo.Metamodel.KnowHOW;
using Rakudo.Runtime;

namespace Rakudo
{
    /// <summary>
    /// Does initialization of the Rakudo library.
    /// </summary>
    public static class Init
    {
        /// <summary>
        /// Have we already registered the representations?
        /// </summary>
        private static bool REPRS_Registered = false;

        /// <summary>
        /// Handles the various bits of initialization that are needed.
        /// Probably needs some don't-dupe-this work.
        /// </summary>
        public static ThreadContext Initialize(string SettingName)
        {
            // Bootstrap the meta-model.
            RegisterRepresentations();
            var KnowHOW = KnowHOWBootstrapper.Bootstrap();

            // See if we're to load a setting or use the fake bootstrapping one.
            Context SettingContext;
            if (SettingName == null)
            {
                SettingContext = BootstrapSetting(KnowHOW);
            }
            else
            {
                SettingContext = LoadSetting(SettingName, KnowHOW);
            }

            // Cache native capture and LLCode type object.
            CaptureHelper.CaptureTypeObject = SettingContext.LexPad["capture"];
            CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)SettingContext.LexPad["LLCode"];

            // Create an execution domain and a thread context for it.
            var ExecDom = new ExecutionDomain();
            var Thread = new ThreadContext();
            Thread.Domain = ExecDom;
            Thread.CurrentContext = SettingContext;
            Thread.DefaultBoolBoxType = SettingContext.LexPad["NQPInt"];
            Thread.DefaultIntBoxType = SettingContext.LexPad["NQPInt"];
            Thread.DefaultNumBoxType = SettingContext.LexPad["NQPNum"];
            Thread.DefaultStrBoxType = SettingContext.LexPad["NQPStr"];

            // LLCode type object should get a HOW.
            SetupLLCodeHOW(Thread, (RakudoCodeRef.Instance)SettingContext.LexPad["LLCode"]);

            return Thread;
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
                REPRS_Registered = true;
            }
        }

        /// <summary>
        /// Adds to the low level code object's HOW.
        /// </summary>
        /// <param name="KnowHOW"></param>
        /// <param name="instance"></param>
        private static void SetupLLCodeHOW(ThreadContext TC, RakudoCodeRef.Instance LLCode)
        {
            var HOW = LLCode.STable.HOW;
            var Meth = HOW.STable.FindMethod(TC, HOW, "add_method", Hints.NO_HINT);
            Meth.STable.Invoke(TC, Meth, CaptureHelper.FormWith(new RakudoObject[] {
                HOW, LLCode,
                Runtime.Ops.box_str(TC, "!add_dispatchee", TC.DefaultStrBoxType),
                CodeObjectUtility.WrapNativeMethod((TC_unused, self, c) =>
                    {
                        var Instance = CaptureHelper.GetPositional(c, 0) as RakudoCodeRef.Instance;
                        var Dispatchee = CaptureHelper.GetPositional(c, 1) as RakudoCodeRef.Instance;
                        if (Instance.Dispatchees == null)
                            Instance.Dispatchees = new List<RakudoCodeRef.Instance>();
                        Instance.Dispatchees.Add(Dispatchee);
                        return CaptureHelper.Nil();
                    })
            }));
        }

        /// <summary>
        /// Sets up the bootstrapping setting that we use to compile the
        /// real setting.
        /// </summary>
        /// <param name="KnowHOW"></param>
        /// <returns></returns>
        private static Context BootstrapSetting(RakudoObject KnowHOW)
        {
            var SettingContext = new Context();
            SettingContext.LexPad = new Dictionary<string, RakudoObject>()
                {
                    { "KnowHOW", KnowHOW },
                    { "print", CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                        {
                            var Value = CaptureHelper.GetPositional(C, 0);
                            var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                            var StrVal = StrMeth.STable.Invoke(TC, StrMeth, C);
                            Console.Write(Ops.unbox_str(TC, StrVal));
                            return CaptureHelper.Nil();
                        })
                    },
                    { "say", CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                        {
                            var Value = CaptureHelper.GetPositional(C, 0);
                            var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                            var StrVal = StrMeth.STable.Invoke(TC, StrMeth, C);
                            Console.WriteLine(Ops.unbox_str(TC, StrVal));
                            return CaptureHelper.Nil();
                        })
                    },
                    { "capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null) },
                    { "NQPInt", REPRRegistry.get_REPR_by_name("P6int").type_object_for(null) },
                    { "NQPNum", REPRRegistry.get_REPR_by_name("P6num").type_object_for(null) },
                    { "NQPStr", REPRRegistry.get_REPR_by_name("P6str").type_object_for(null) },
                    { "LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(KnowHOW.STable.REPR.instance_of(KnowHOW)) }
                };
            return SettingContext;
        }

        /// <summary>
        /// Loads the setting with the given name.
        /// </summary>
        /// <param name="Name"></param>
        /// <param name="KnowHOW"></param>
        /// <returns></returns>
        public static Context LoadSetting(string Name, RakudoObject KnowHOW)
        {
            // Load the assembly.
            var SettingAssembly = AppDomain.CurrentDomain.Load(Name);

            // Find the setting type and its LoadSetting method.
            var Class = SettingAssembly.GetType("NQPSetting");
            var Method = Class.GetMethod("LoadSetting", BindingFlags.NonPublic | BindingFlags.Static);
            
            // Run it to get the context we want.
            var SettingContext = (Context)Method.Invoke(null, new object[] { });

            // Fudge a few more things in.
            // XXX Should be able to toss all of thse but KnowHOW.
            SettingContext.LexPad.Add("KnowHOW", KnowHOW);
            SettingContext.LexPad.Add("print",
                CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        var Value = CaptureHelper.GetPositional(C, 0);
                        var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                        var StrVal = StrMeth.STable.Invoke(TC, StrMeth, C);
                        Console.Write(Ops.unbox_str(null, StrVal));
                        return CaptureHelper.Nil();
                    }));
            SettingContext.LexPad.Add("say",
                CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        var Value = CaptureHelper.GetPositional(C, 0);
                        var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                        var StrVal = StrMeth.STable.Invoke(TC, StrMeth, C);
                        Console.WriteLine(Ops.unbox_str(null, StrVal));
                        return CaptureHelper.Nil();
                    }));
            SettingContext.LexPad.Add("capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null));
            SettingContext.LexPad.Add("LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(KnowHOW.STable.REPR.instance_of(KnowHOW)));
            
            return SettingContext;
        }
    }
}
