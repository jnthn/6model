using System;
using System.Collections.Generic;
using System.Linq;
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

            // Create an execution domain and a thread context for it
            // and return that.
            var ExecDom = new ExecutionDomain();
            ExecDom.DefaultStrBoxType = SettingContext.LexPad["Str"];
            var Thread = new ThreadContext();
            Thread.Domain = ExecDom;
            Thread.CurrentContext = SettingContext;
            return Thread;
        }
        
        /// <summary>
        /// Registers all of the built-in representations.
        /// </summary>
        private static void RegisterRepresentations()
        {
            REPRRegistry.register_REPR("KnowHOWREPR", new KnowHOWREPR());
            REPRRegistry.register_REPR("P6opaque", new P6opaque());
            REPRRegistry.register_REPR("P6hash", new P6hash());
            REPRRegistry.register_REPR("P6int", new P6native<int>());
            REPRRegistry.register_REPR("P6num", new P6native<double>());
            REPRRegistry.register_REPR("P6str", new P6str());
            REPRRegistry.register_REPR("P6capture", new P6capture());
            REPRRegistry.register_REPR("RakudoCodeRef", new RakudoCodeRef());
        }

        /// <summary>
        /// Sets up the bootstrapping setting that we use to compile the
        /// real setting.
        /// </summary>
        /// <param name="KnowHOW"></param>
        /// <returns></returns>
        private static Context BootstrapSetting(IRakudoObject KnowHOW)
        {
            var SettingContext = new Context();
            SettingContext.LexPad = new Dictionary<string, IRakudoObject>()
                {
                    { "KnowHOW", KnowHOW },
                    { "print", CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                        {
                            Console.Write(CaptureHelper.GetPositionalAs<string>(C, 0));
                            return CaptureHelper.Nil();
                        })
                    },
                    { "say", CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                        {
                            Console.WriteLine(CaptureHelper.GetPositionalAs<string>(C, 0));
                            return CaptureHelper.Nil();
                        })
                    },
                    { "capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null) },
                    { "Str", REPRRegistry.get_REPR_by_name("P6str").type_object_for(null) },
                    { "Int", REPRRegistry.get_REPR_by_name("P6int").type_object_for(null) },
                    { "Num", REPRRegistry.get_REPR_by_name("P6num").type_object_for(null) },
                    { "LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null) },
                };
            return SettingContext;
        }

        /// <summary>
        /// Loads the setting with the given name.
        /// </summary>
        /// <param name="Name"></param>
        /// <param name="KnowHOW"></param>
        /// <returns></returns>
        public static Context LoadSetting(string Name, IRakudoObject KnowHOW)
        {
            throw new NotImplementedException("Setting loading is not yet implemented.");
        }
    }
}
