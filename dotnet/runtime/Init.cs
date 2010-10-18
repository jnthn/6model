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
            CaptureHelper.CaptureTypeObject = SettingContext.LexPad.GetByName("capture");
            CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)SettingContext.LexPad.GetByName("LLCode");

            // Create an execution domain and a thread context for it.
            var ExecDom = new ExecutionDomain();
            var Thread = new ThreadContext();
            Thread.Domain = ExecDom;
            Thread.CurrentContext = SettingContext;
            Thread.DefaultBoolBoxType = SettingContext.LexPad.GetByName("NQPInt");
            Thread.DefaultIntBoxType = SettingContext.LexPad.GetByName("NQPInt");
            Thread.DefaultNumBoxType = SettingContext.LexPad.GetByName("NQPNum");
            Thread.DefaultStrBoxType = SettingContext.LexPad.GetByName("NQPStr");
            Thread.DefaultListType = SettingContext.LexPad.GetByName("NQPList");

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
            var SettingContext = new Context();
            SettingContext.LexPad = new Lexpad(new string[]
                { "KnowHOW", "capture", "NQPInt", "NQPNum", "NQPStr", "NQPList", "LLCode", "list" });
            SettingContext.LexPad.Storage = new RakudoObject[]
                {
                    KnowHOW,
                    REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null, null),
                    REPRRegistry.get_REPR_by_name("P6int").type_object_for(null, null),
                    REPRRegistry.get_REPR_by_name("P6num").type_object_for(null, null),
                    REPRRegistry.get_REPR_by_name("P6str").type_object_for(null, null),
                    REPRRegistry.get_REPR_by_name("P6list").type_object_for(null, null),
                    REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null, KnowHOW.STable.REPR.instance_of(null, KnowHOW)),
                    CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                        {
                            var NQPList = Ops.get_lex(TC, "NQPList");
                            var List = NQPList.STable.REPR.instance_of(TC, NQPList) as P6list.Instance;
                            var NativeCapture = C as P6capture.Instance;
                            foreach (var Obj in NativeCapture.Positionals)
                                List.Storage.Add(Obj);
                            return List;
                        })
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
            SettingContext.LexPad.Extend(new string[]
                { "KnowHOW", "print", "say", "capture", "LLCode" });
            SettingContext.LexPad.SetByName("KnowHOW", KnowHOW);
            SettingContext.LexPad.SetByName("print",
                CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        for (int i = 0; i < CaptureHelper.NumPositionals(C); i++)
                        {
                            var Value = CaptureHelper.GetPositional(C, i);
                            var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                            var StrVal = StrMeth.STable.Invoke(TC, StrMeth,
                                CaptureHelper.FormWith( new RakudoObject[] { Value }));
                            Console.Write(Ops.unbox_str(null, StrVal));
                        }
                        return CaptureHelper.Nil();
                    }));
            SettingContext.LexPad.SetByName("say",
                CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        for (int i = 0; i < CaptureHelper.NumPositionals(C); i++)
                        {
                            var Value = CaptureHelper.GetPositional(C, i);
                            var StrMeth = self.STable.FindMethod(TC, Value, "Str", 0);
                            var StrVal = StrMeth.STable.Invoke(TC, StrMeth,
                                CaptureHelper.FormWith( new RakudoObject[] { Value }));
                            Console.Write(Ops.unbox_str(null, StrVal));
                        }
                        Console.WriteLine();
                        return CaptureHelper.Nil();
                    }));
            SettingContext.LexPad.SetByName("capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null, null));
            SettingContext.LexPad.SetByName("LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null, KnowHOW.STable.REPR.instance_of(null, KnowHOW)));
            
            return SettingContext;
        }
    }
}
